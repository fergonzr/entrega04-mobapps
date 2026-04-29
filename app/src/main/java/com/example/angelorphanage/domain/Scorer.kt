package com.example.angelorphanage.domain

// What it means to be a score generator type in the game

interface Scorer {
    fun tickdown_meters(parameters: GameParameters, meters: Map<ResourceType, Int>): Map<ResourceType, Int>
    fun score(parameters: GameParameters, meters: Map<ResourceType, Int>): Int
    fun try_giveaway(parameters: GameParameters, meters: Map<ResourceType, Int>): Boolean
    fun generate_name(): String
}