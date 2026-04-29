package com.example.angelorphanage.domain

interface GameParameterDiff {
    fun apply(parameters: GameParameters): GameParameters

    operator fun plus(other: GameParameterDiff): GameParameterDiff {
        return object: GameParameterDiff {
            override fun apply(parameters: GameParameters): GameParameters =
                other.apply(this.apply(parameters))
        }
    }
}