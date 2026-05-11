package com.juego.petangels.domain

interface GameParameterDiff {
    fun apply(parameters: GameParameters): GameParameters

    fun add(other: GameParameterDiff): GameParameterDiff {
        return object: GameParameterDiff {
            override fun apply(parameters: GameParameters): GameParameters =
                this@GameParameterDiff.apply(other.apply(parameters))
        }
    }
}
