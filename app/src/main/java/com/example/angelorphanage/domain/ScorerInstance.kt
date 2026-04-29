package com.example.angelorphanage.domain

import kotlin.math.max
import kotlin.math.min

/**
 * An immutable scorer holding data in the game.
 */

data class ScorerInstance(
    val name: String,
    val type: ScorerType,
    val givenAway: Boolean,
    val meters: Map<ResourceType, Int>,
    val lastGeneratedScore: Int
) {
    constructor(type: ScorerType) : this(
        name = type.generate_name(),
        type = type,
        givenAway = false,
        meters = ResourceType.entries.associateWith { 0 },
        lastGeneratedScore = 0
    )

    /**
     * Create a new ScorerInstance with tickdown applied
     */
    fun tickdown(parameters: GameParameters): ScorerInstance {
        val newMeters = this.type
            .tickdown_meters(parameters, this.meters)
            .map { it.key to min(
                    (this.meters.getOrDefault(it.key, 0) - it.value),
                    this.type.meterLimits[it.key]!!.first
                )
            }
            .associate { it }

        return this.copy(meters = newMeters)
    }

    /**
     * Create a new ScorerInstance with score calculated
     */
    fun score(parameters: GameParameters): Pair<ScorerInstance, Int> {
        val newScore = this.type.score(parameters, this.meters)
        return this.copy(lastGeneratedScore = newScore) to newScore
    }

    /**
     * Try to give away the scorer and return the new instance and score if successful
     */
    fun try_giveaway(parameters: GameParameters): Pair<ScorerInstance, Int> {
        if (!this.givenAway) {
            val shouldGiveAway = this.type.try_giveaway(parameters, this.meters)
            val newGivenAway = shouldGiveAway
            val reward = this.type.giveawayReward * if (newGivenAway) 1 else 0
            return this.copy(givenAway = newGivenAway) to reward
        }
        return this to 0
    }

    /**
     * Create a new ScorerInstance with resources allocated
     */
    fun allocate(resources: Map<ResourceType, Int>): ScorerInstance {
        val newMeters = ResourceType.entries.associateWith { resType ->
            // The amount of resources to be allocated is hard-capped by the type's upper meter limit
            max(
                this.meters[resType]!! + resources.getOrDefault(resType, 0),
                this.type.meterLimits[resType]!!.second
            )
        }
        return this.copy(meters = newMeters)
    }
}
