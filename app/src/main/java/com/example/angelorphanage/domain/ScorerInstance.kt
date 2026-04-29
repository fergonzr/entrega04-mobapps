package com.example.angelorphanage.domain

/// A scorer holding data in the game

class ScorerInstance {
    val name: String
    val type: ScorerType
    var meters: Map<ResourceType, Int>

    constructor(type: ScorerType){
        this.type = type
        this.name = type.generate_name()
        this.meters =
            ResourceType.entries.associateWith { 0 }
    }

    fun tickdown(parameters: GameParameters){
        this.meters = this.type.tickdown_meters(parameters, this.meters)
    }

    fun score(parameters: GameParameters): Int {
        return this.type.score(parameters, this.meters)
    }

    fun try_giveaway(parameters: GameParameters): Boolean {
        return this.type.try_giveaway(parameters, this.meters)
    }
}