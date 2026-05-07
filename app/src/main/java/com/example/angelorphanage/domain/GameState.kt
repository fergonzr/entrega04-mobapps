package com.example.angelorphanage.domain



/**
 * The immutable state of the game
 */

data class GameState(
    val score: Int,
    val elapsedTurns: Int,
    val scorers: List<ScorerInstance>,
    val currentResources: Map<ResourceType, Int>,
    val level: Int,
    val powerups: Set<Powerup>,
    val powerUpsUnlocked: Boolean,
    val baseParameters: GameParameters = BaseGameParameters(),
    val finished: Boolean,
) {
    constructor(): this(
        score = 0,
        elapsedTurns = 0,
        scorers = (0 until newpets_per_level(1)).map { adquire() }.toList(),
        currentResources = ResourceType.entries.associateWith { it.initialValue },
        powerups = setOf(),
        powerUpsUnlocked = false,
        level = 1,
        finished = false,
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

        val leveledUp = newScorers.total_givenaway_pets() >= givenaway_pet_level_target(this.level)
        val newLevel = level + if (leveledUp) 1 else 0
        val finished = newLevel >= maxGameLevel

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
            level = newLevel,
            scorers =
                if (finished) newScorers
                else if (leveledUp) finalScorers.adquire_new_pets_for_level(newLevel)
                else finalScorers,
            currentResources = newResources
                .map { it.key to (it.value + it.key.generate(this.get_parameters())) }
                .associate { it },
            powerUpsUnlocked = this.powerUpsUnlocked || leveledUp,
            finished = finished
        )
    }

    fun buy_powerup(powerUp: Powerup): GameState {
        if (this.score < powerUp.type.costPerlevel)
            throw InsufficientScoreException()

        val newScore = this.score - powerUp.type.costPerlevel

        val newPowerup =
        if (this.powerups.contains(powerUp))
            this.powerups.first {it == powerUp} + powerUp
        else
            powerUp

        return this.copy(
            powerups = setOf(
                *this.powerups.toTypedArray(), newPowerup),
            score = newScore
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

    // Adquire new scorers when leveling up and return the updated scorers list
    private fun List<ScorerInstance>.adquire_new_pets_for_level(level: Int): List<ScorerInstance> {
        val newScorers = (0 until newpets_per_level(level)).map { adquire() }
        return this + newScorers
    }

    private fun List<ScorerInstance>.total_givenaway_pets(): Int =
        this.count { it.givenAway }

    private operator fun Map<ResourceType, Int>.plus(other: Map<ResourceType, Int>): Map<ResourceType, Int> =
        this.keys
            .map { Pair(it, this[it]!! + other.getOrDefault(it, 0)) }
            .associate { it }

    private operator fun Map<ResourceType, Int>.minus(other: Map<ResourceType, Int>): Map<ResourceType, Int> =
        this.keys
            .map { Pair(it, this[it]!! - other.getOrDefault(it, 0)) }
            .associate { it }
}