package com.example.angelorphanage.domain

import androidx.annotation.StringRes
import com.example.angelorphanage.R
import kotlinx.serialization.Serializable

@Serializable
enum class ResourceType(
    @param:StringRes val nameRes: Int,
    val initialValue: Int
) : Resource {
    FOOD(R.string.food_label, 2) {
        override fun generate(parameters: GameParameters): Int {
            return parameters.getFoodMultiplier()
        }
    },
    WATER(R.string.water_label, 3) {
        override fun generate(parameters: GameParameters): Int {
            return parameters.getWaterMultiplier()
        }
    }
}