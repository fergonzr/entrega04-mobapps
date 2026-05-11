package com.example.angelorphanage.domain

import androidx.annotation.StringRes
import com.example.angelorphanage.R
import kotlinx.serialization.Serializable
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
        giveawayReward = 250,
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
                    ResourceType.FOOD -> it.value
                    ResourceType.WATER -> it.value * 2
                }
            }.sum()
        }

        override fun try_giveaway(
            parameters: GameParameters,
            meters: Map<ResourceType, Int>
        ): Boolean {
            val metersFull = meters.all { it.value == this.meterLimits[it.key]!!.second }
            if (!metersFull) return false
            // 10% base chance, each visibility level adds 30%, reaching 100% at max level (3)
            val chance = (10 + parameters.getVisibility() * 30).coerceAtMost(100)
            return Random.nextInt(100) < chance
        }

    },

    CAT(name = R.string.cat_label,
        meterLimits = mapOf(
            ResourceType.FOOD to Pair(-1,1),
            ResourceType.WATER to Pair(-1,2),
        ),
        giveawayReward = 150
    ){
        override fun tickdown_meters(
            parameters: GameParameters,
            meters: Map<ResourceType, Int>
        ): Map<ResourceType, Int> = mapOf(
            ResourceType.FOOD to 1,
            ResourceType.WATER to 1,
        )

        override fun score(
            parameters: GameParameters,
            meters: Map<ResourceType, Int>
        ): Int {
            return meters.map {
                when (it.key) {
                    ResourceType.FOOD -> it.value
                    ResourceType.WATER -> it.value * 2
                }
            }.sum()
        }

        override fun try_giveaway(
            parameters: GameParameters,
            meters: Map<ResourceType, Int>
        ): Boolean {
            val metersFull = meters.all { it.value == this.meterLimits[it.key]!!.second }
            if (!metersFull) return false
            // 20% base chance, each visibility level adds 27%, reaching ~100% at max level (3)
            val chance = (20 + parameters.getVisibility() * 27).coerceAtMost(100)
            return Random.nextInt(100) < chance
        }

    };

    override fun generate_name(): String {
        return scorerNames[this]!!.random()
    }
}
