package com.example.angelorphanage.domain

import kotlinx.serialization.Serializable

@Serializable
class Powerup(val type: PowerupType, val level: Int = 1) : GameParameterDiff {

    override fun apply(parameters: GameParameters): GameParameters =
        this.type.apply(parameters, this.level)

    override fun equals(other: Any?): Boolean =
        if (other is Powerup)
            this.type == other.type
        else
            false

    override fun hashCode(): Int {
        var result = level
        result = 31 * result + type.hashCode()
        return result
    }

    operator fun plus(other: Powerup): Powerup {
        if (other.type != this.type)
            throw IllegalArgumentException("Can't add powerups of different types")

        val resultantLevel = this.level + other.level

        if (resultantLevel > this.type.maxLevel)
            throw IllegalArgumentException("Can't have a level $resultantLevel powerup of type ${this.type.name}.")

        return Powerup(this.type, resultantLevel)
    }
}