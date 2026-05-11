package com.example.angelorphanage.domain

import androidx.annotation.StringRes
import com.example.angelorphanage.R
import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.random.Random

// Each score generator types on the game

@Serializable
enum class ScorerType(
    @StringRes name: Int,
    val meterLimits: Map<ResourceType, Pair<Int, Int>>,
    val giveawayReward: Int
): Scorer {
    DOG(name = R.string.dog_label,
        meterLimits = mapOf(
            ResourceType.FOOD to Pair(-3,2),
            ResourceType.WATER to Pair(-3,2),
        ),
        giveawayReward = 30,
    ) {
        override fun tickdown_meters(
            parameters: GameParameters,
            meters: Map<ResourceType, Int>
        ): Map<ResourceType, Int> = mapOf(
            ResourceType.FOOD to 2,
            ResourceType.WATER to 1,
        )

        override fun score(
            parameters: GameParameters,
            meters: Map<ResourceType, Int>
        ): Int {
            return meters.map {
                when (it.key) {
                    // Dogs are more sensitive to hunger than thirst
                    ResourceType.FOOD -> ceil(7.0 * ln(it.value / 3.0 + 1.001)).toInt()
                    ResourceType.WATER -> ceil(3.5 * ln(it.value / 3.0 + 1.001)).toInt()
                }
            }.sum()
        }

        override fun try_giveaway(
            parameters: GameParameters,
            meters: Map<ResourceType, Int>
        ): Boolean {
            val metersFull = meters.all { it.value == this.meterLimits[it.key]!!.second }
            if (!metersFull) return false
            // 43% base chance, each visibility level adds 19%, reaching 100% at max level (3)
            val chance = (43 + parameters.getVisibility() * 19).coerceAtMost(100)
            return Random.nextInt(100) < chance
        }

    },

    CAT(name = R.string.cat_label,
        meterLimits = mapOf(
            ResourceType.FOOD to Pair(-1,2),
            ResourceType.WATER to Pair(-2,1),
        ),
        giveawayReward = 25
    ){
        override fun tickdown_meters(
            parameters: GameParameters,
            meters: Map<ResourceType, Int>
        ): Map<ResourceType, Int> = mapOf(
            ResourceType.FOOD to 1,
            ResourceType.WATER to 2,
        )

        override fun score(
            parameters: GameParameters,
            meters: Map<ResourceType, Int>
        ): Int {
            return meters.map {
                when (it.key) {
                    // Cats are more sensitive to thirst than hunger
                    ResourceType.FOOD -> 2 * it.value
                    ResourceType.WATER -> 3 * it.value
                }
            }.sum()
        }

        override fun try_giveaway(
            parameters: GameParameters,
            meters: Map<ResourceType, Int>
        ): Boolean {
            val metersFull = meters.all { it.value == this.meterLimits[it.key]!!.second }
            if (!metersFull) return false
            // 43% base chance, each visibility level adds 19%, reaching ~100% at max level (3)
            val chance = (43 + parameters.getVisibility() * 19).coerceAtMost(100)
            return Random.nextInt(100) < chance
        }

    };

    override fun generate_name(): String {
        return scorerNames[this]!!.random()
    }
}
