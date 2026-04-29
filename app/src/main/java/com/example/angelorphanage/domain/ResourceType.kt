package com.example.angelorphanage.domain

import androidx.annotation.StringRes
import com.example.angelorphanage.R

enum class ResourceType(
    @StringRes name: Int,
) : Resource {
    FOOD(R.string.food_label) {
        override fun generate(parameters: GameParameters): Int {
            TODO("Not yet implemented")
        }
    },
    WATER(R.string.water_label) {
        override fun generate(parameters: GameParameters): Int {
            TODO("Not yet implemented")
        }
    }
}