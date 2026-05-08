package com.example.angelorphanage.data

import com.example.angelorphanage.domain.GameState

/**
 * Abstraction for game data persistence.
 *
 * Manages two categories of data:
 * - **Current game**: The in-progress game state, saved after each turn
 *   and loaded on app startup to allow resuming.
 * - **Game history**: A list of completed game summaries, accumulated
 *   across sessions and displayed on the score screen.
 */
interface GameRepository {

    /**
     * Persist the current game state so it can be resumed later.
     */
    suspend fun saveCurrentGame(state: GameState)

    /**
     * Load the saved game state, or null if no game is in progress.
     */
    suspend fun loadCurrentGame(): GameState?

    /**
     * Remove the saved game state (e.g. after a game is completed).
     */
    suspend fun clearCurrentGame()

    /**
     * Append a completed game summary to the persistent history.
     */
    suspend fun saveCompletedGame(summary: GameSummary)

    /**
     * Retrieve all completed game summaries, oldest first.
     */
    suspend fun getCompletedGames(): List<GameSummary>
}