package com.example.angelorphanage.domain

import androidx.annotation.StringRes
import com.example.angelorphanage.R
import kotlin.random.Random

// Each score generator types on the game

enum class ScorerType(
    @StringRes name: Int,
    val meterLimits: Map<ResourceType, Pair<Int, Int>>,
    val giveawayReward: Int
): Scorer {
    DOG(name = R.string.dog_label,
        meterLimits = mapOf(
            ResourceType.FOOD to Pair(-2,3),
            ResourceType.WATER to Pair(-2,2),
        ),
        giveawayReward = 300,
    ) {
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
            return meters
                .all { it.value == this.meterLimits[it.key]!!.second }
                    && Random.nextInt(100) >= 80
        }

    },

    CAT(name = R.string.cat_label,
        meterLimits = mapOf(
            ResourceType.FOOD to Pair(-1,1),
            ResourceType.WATER to Pair(-1,2),
        ),
        giveawayReward = 200
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
            return meters
                .all { it.value == this.meterLimits[it.key]!!.second }
                    && Random.nextInt(100) >= 70
        }

    };

    override fun generate_name(): String {
        return scorerNames[this]!!.random()
    }
}