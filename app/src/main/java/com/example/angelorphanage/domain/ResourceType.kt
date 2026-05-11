package com.example.angelorphanage.domain

import androidx.annotation.StringRes
import com.example.angelorphanage.R
import kotlinx.serialization.Serializable
import org.apache.commons.math3.distribution.NormalDistribution
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

@Serializable
enum class ResourceType(
    @param:StringRes val nameRes: Int,
    val initialValue: Int
) : Resource {
    FOOD(R.string.food_label, 1) {
        override fun generate(parameters: GameParameters): Int {
            return max(NormalDistribution(parameters.getFoodMultiplier().toDouble(), 1.0).sample()
                .roundToInt(), 0)
        }
    },
    WATER(R.string.water_label, 2) {
        override fun generate(parameters: GameParameters): Int {
            return max(NormalDistribution(parameters.getWaterMultiplier().toDouble(), 2.0).sample()
                .roundToInt(), 0)
        }
    }
}
