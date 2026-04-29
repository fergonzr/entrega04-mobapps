package com.example.angelorphanage.domain

/**
 * The immutable state of the game
 */

data class GameState(
    val score: Int,
    val elapsedTurns: Int,
    val scorers: List<ScorerInstance>,
    val currentResources: Map<ResourceType, Int>,
    val powerups: Set<Powerup>,
    val baseParameters: GameParameters = BaseGameParameters()
) {
    constructor(): this(
        score = 0,
        elapsedTurns = 0,
        scorers = (0..3).map { adquire() }.toList(),
        currentResources = ResourceType.entries.associateWith { it.initialValue },
        powerups = setOf()
    )

    /**
     * Run the game for a turn, allocating the resources accordingly
     * allocation: the list of how much resources to allocate to each
     * scorer, mapped by index.
     */
    fun run(allocation: List<Map<ResourceType, Int>>): GameState {
        if (allocation.size > this.scorers.size)
            throw IllegalArgumentException("Can't allocate to more than ${this.scorers.size} scorers")

        // Allocate resources
        for ((index, scorerAllocation) in allocation.withIndex()){
            // Do nothing if the scorer has been given away
            if (this.scorers[index].givenAway)
                continue

            if (scorerAllocation.isNotEmpty())
                this.scorers[index].allocate(scorerAllocation)
            else
                this.scorers[index].tickdown(this.get_parameters())
        }

        // Make our scorers score
        var newScore = this.score + this.scorers
            .filter { !it.givenAway }
            .sumOf { it.score(this.get_parameters()) }

        // Try to give away our scorers
        newScore += this.scorers
            .sumOf { it.try_giveaway(this.get_parameters()) }


        return this.copy(
            score = newScore,
            elapsedTurns = this.elapsedTurns + 1,
            scorers = this.try_adquire_new(),
            currentResources = this.currentResources
                .map { it.key to (it.value + it.key.generate(this.get_parameters())) }
                .associate { it }
        )
    }

    fun buy_powerup(powerUp: Powerup): GameState {
        val newPowerup =
        if (this.powerups.contains(powerUp))
            this.powerups.first {it == powerUp} + powerUp
        else
            powerUp

        return this.copy(
            powerups = setOf(
                *this.powerups.toTypedArray(), newPowerup)
        )
    }

    // Apply all the powerups to the base parameters
    private fun get_parameters(): GameParameters {
        if (this.powerups.isEmpty())
            return this.baseParameters

        return this.powerups
            .sortedBy { it.type.order }
            .reduce { acc, powerup ->  acc + powerup }
            .apply(this.baseParameters)
    }

    // Try to adquire a new scorer and return how the new scorers list
    // would look like
    private fun try_adquire_new(): List<ScorerInstance> {
        val newScorer = try_adquire(this.get_parameters())

        if (newScorer != null)
            return this.scorers + newScorer
        return this.scorers
    }
}