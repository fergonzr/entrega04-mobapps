package com.example.angelorphanage.data

import kotlinx.serialization.Serializable

/**
 * Brief summary of a completed game, used for the score screen listing
 * and persisted to game history.
 */
@Serializable
data class GameSummary(
    val score: Int,
    val elapsedTurns: Int,
    val level: Int,
    val petsAdopted: Int,
    val rating: Int
)
