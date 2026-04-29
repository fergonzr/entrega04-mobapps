package com.example.angelorphanage.domain

import androidx.annotation.StringRes
import com.example.angelorphanage.R

enum class PowerupType(
    @StringRes name: Int,
    @StringRes description: Int,
    val order: Int,
    val maxLevel: Int
) {
    COMFORT(
        R.string.decoration_label,
        R.string.decoration_desc,
        1,
        3
    ){
        override fun apply(parameters: GameParameters, level: Int): GameParameters =
            object: GameParameters {
                override fun getComfort() = parameters.getComfort() + level
                override fun getVisibility() = parameters.getVisibility()
                override fun getFoodMultiplier() = parameters.getFoodMultiplier()
                override fun getWaterMultiplier() = parameters.getWaterMultiplier()
            }
    },
    VISIBILITY(
        R.string.visibility_label,
        R.string.visibility_desc,
        1,
        3
    ){
        override fun apply(parameters: GameParameters, level: Int): GameParameters =
            object: GameParameters {
                override fun getComfort() = parameters.getComfort()
                override fun getVisibility() = parameters.getVisibility() + level
                override fun getFoodMultiplier() = parameters.getFoodMultiplier()
                override fun getWaterMultiplier() = parameters.getWaterMultiplier()
            }
    };
    abstract fun apply(parameters: GameParameters, level: Int): GameParameters;
}