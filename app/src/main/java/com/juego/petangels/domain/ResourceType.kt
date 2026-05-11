package com.juego.petangels.domain

import androidx.annotation.StringRes
import com.juego.petangels.R
import kotlinx.serialization.Serializable

@Serializable
enum class ResourceType(
    @param:StringRes val nameRes: Int,
    val initialValue: Int
) : Resource {
    FOOD(R.string.food_label, 1) {
        override fun generate(parameters: GameParameters): Int {
            return parameters.getFoodMultiplier()
        }
    },
    WATER(R.string.water_label, 2) {
        override fun generate(parameters: GameParameters): Int {
            return parameters.getWaterMultiplier()
        }
    }
}
