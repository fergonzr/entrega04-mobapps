package com.example.angelorphanage.domain

interface GameParameterDiff {
    fun apply(parameters: GameParameters): GameParameters

    fun add(other: GameParameterDiff): GameParameterDiff {
        return object: GameParameterDiff {
            override fun apply(parameters: GameParameters): GameParameters =
                this@GameParameterDiff.apply(other.apply(parameters))
        }
    }
}
