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
        scorers = (0..2).map { adquire() }.toList(),
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

        // Calculate how many resources we are left with
        val newResources = this.currentResources - allocation
            .reduce { acc, it -> acc + it }

        // Verify we don't go into the negatives in any of them
        if (newResources.any { it.value < 0 })
            throw IllegalArgumentException("Can't allocate more resources than there are on stock.")

        // Calculate scores and handle giveaways
        var newScore = this.score
        val newScorers = this.scorers.map { scorer ->
            if (scorer.givenAway) {
                scorer
            } else {
                // Calculate score
                val (scorerWithScore, score) = scorer.score(this.get_parameters())
                newScore += score

                // Try to give away
                val (scorerAfterGiveaway, giveawayScore) = scorerWithScore.try_giveaway(this.get_parameters())
                newScore += giveawayScore

                scorerAfterGiveaway
            }
        }

        // Process each scorer to create new instances with updated state
        val finalScorers = newScorers.mapIndexed { index, scorer ->
            if (scorer.givenAway) {
                // Keep given away scorers as-is
                scorer
            } else {
                // Apply allocation or ticking based on the resource
                scorer.allocate_or_tick(this.get_parameters(), allocation[index])
            }
        }

        return this.copy(
            score = newScore,
            elapsedTurns = this.elapsedTurns + 1,
            scorers = finalScorers.try_adquire_new(this.get_parameters()),
            currentResources = newResources
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

    // Try to adquire a new scorer and return the new scorers list
    private fun List<ScorerInstance>.try_adquire_new(parameters: GameParameters): List<ScorerInstance> {
        val newScorer = try_adquire(parameters)

        if (newScorer != null)
            return this + newScorer
        return this
    }

    private operator fun Map<ResourceType, Int>.plus(other: Map<ResourceType, Int>): Map<ResourceType, Int> =
        this.keys
            .map { Pair(it, this[it]!! + other.getOrDefault(it, 0)) }
            .associate { it }

    private operator fun Map<ResourceType, Int>.minus(other: Map<ResourceType, Int>): Map<ResourceType, Int> =
        this.keys
            .map { Pair(it, this[it]!! - other.getOrDefault(it, 0)) }
            .associate { it }
}