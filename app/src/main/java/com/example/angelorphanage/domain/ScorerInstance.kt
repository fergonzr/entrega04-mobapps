package com.example.angelorphanage.domain

import kotlin.math.max
import kotlin.math.min

/**
 * A scorer holding data in the game.
 * This is deliberately a mutable data structure.
 */

class ScorerInstance {
    val name: String
    val type: ScorerType
    var givenAway: Boolean = false
    var meters: Map<ResourceType, Int>
    var lastGeneratedScore: Int = 0

    constructor(type: ScorerType){
        this.type = type
        this.name = type.generate_name()
        this.meters =
            ResourceType.entries.associateWith { 0 }
    }

    fun tickdown(parameters: GameParameters){
        this.meters = this.type
            .tickdown_meters(parameters, this.meters)
            .map { it.key to min(
                    (this.meters.getOrDefault(it.key, 0) - it.value),
                    this.type.meterLimits[it.key]!!.first
                )
            }
            .associate { it }
    }

    fun score(parameters: GameParameters): Int {
        this.lastGeneratedScore = this.type.score(parameters, this.meters)
        return this.lastGeneratedScore
    }

    /**
     * Try to give away the scorer and return the score if successful
     */
    fun try_giveaway(parameters: GameParameters): Int {
        if (!this.givenAway){
            this.givenAway = this.type.try_giveaway(parameters, this.meters)
            return this.type.giveawayReward * if (this.givenAway) 1 else 0
        }
        return 0
    }

    fun allocate(resources: Map<ResourceType, Int>){
        this.meters = ResourceType.entries.associateWith { resType ->
            // The amount of resources to be allocated is hard-capped  by the type's upper meter limit
            max(
                this.meters[resType]!! + resources.getOrDefault(resType, 0),
                this.type.meterLimits[resType]!!.second
            )
        }
    }
}