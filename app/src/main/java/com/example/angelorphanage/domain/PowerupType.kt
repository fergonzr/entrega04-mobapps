package com.example.angelorphanage.domain

import androidx.annotation.StringRes
import com.juego.petangels.R
import kotlinx.serialization.Serializable

@Serializable
enum class PowerupType(
    @StringRes name: Int,
    @StringRes description: Int,
    val order: Int,
    val maxLevel: Int,
    val costPerlevel: Int,
) {
    COMFORT(
        R.string.comfort_label,
        R.string.comfort_desc,
        1,
        3,
        200
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
        R.string.adoption_marketing_label,
        R.string.adoption_marketing_desc,
        2,
        3,
        250
    ){
        override fun apply(parameters: GameParameters, level: Int): GameParameters =
            object: GameParameters {
                override fun getComfort() = parameters.getComfort()
                override fun getVisibility() = parameters.getVisibility() + level
                override fun getFoodMultiplier() = parameters.getFoodMultiplier()
                override fun getWaterMultiplier() = parameters.getWaterMultiplier()
            }
    },
    FOOD_INCREMENT(
        R.string.food_increment_label,
        R.string.food_increment_desc,
        3,
        3,
        300
    ){
        override fun apply(parameters: GameParameters, level: Int): GameParameters =
            object: GameParameters {
                override fun getComfort() = parameters.getComfort()
                override fun getVisibility() = parameters.getVisibility()
                override fun getFoodMultiplier() = parameters.getFoodMultiplier() + level
                override fun getWaterMultiplier() = parameters.getWaterMultiplier()
            }
    },
    WATER_INCREMENT(
        R.string.water_increment_label,
        R.string.water_increment_desc,
        4,
        3,
        300
    ){
        override fun apply(parameters: GameParameters, level: Int): GameParameters =
            object: GameParameters {
                override fun getComfort() = parameters.getComfort()
                override fun getVisibility() = parameters.getVisibility()
                override fun getFoodMultiplier() = parameters.getFoodMultiplier()
                override fun getWaterMultiplier() = parameters.getWaterMultiplier() + level
            }
    };
    abstract fun apply(parameters: GameParameters, level: Int): GameParameters;
}