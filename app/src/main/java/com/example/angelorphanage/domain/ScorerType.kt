package com.example.angelorphanage.domain

import androidx.annotation.StringRes
import com.example.angelorphanage.R

// Each score generator types on the game

enum class ScorerType(
    @StringRes name: Int,
    meterLimits: Map<ResourceType, Pair<Int, Int>>
): Scorer {
    DOG(name = R.string.dog_label,
        meterLimits = mapOf(
            ResourceType.FOOD to Pair(-2,3),
            ResourceType.WATER to Pair(-2,2),
        )
    ) {
        override fun tickdown_meters(
            parameters: GameParameters,
            meters: Map<ResourceType, Int>
        ): Map<ResourceType, Int> {
            TODO("Not yet implemented")
        }

        override fun score(
            parameters: GameParameters,
            meters: Map<ResourceType, Int>
        ): Int {
            TODO("Not yet implemented")
        }

        override fun try_giveaway(
            parameters: GameParameters,
            meters: Map<ResourceType, Int>
        ): Boolean {
            TODO("Not yet implemented")
        }

    },

    CAT(name = R.string.cat_label,
        meterLimits = mapOf(
            ResourceType.FOOD to Pair(-1,1),
            ResourceType.WATER to Pair(-1,2),
        )
    ){
        override fun tickdown_meters(
            parameters: GameParameters,
            meters: Map<ResourceType, Int>
        ): Map<ResourceType, Int> {
            TODO("Not yet implemented")
        }

        override fun score(
            parameters: GameParameters,
            meters: Map<ResourceType, Int>
        ): Int {
            TODO("Not yet implemented")
        }

        override fun try_giveaway(
            parameters: GameParameters,
            meters: Map<ResourceType, Int>
        ): Boolean {
            TODO("Not yet implemented")
        }

    };

    override fun generate_name(): String {
        return scorerNames[this]!!.random()
    }
}