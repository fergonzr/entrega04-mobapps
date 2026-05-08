package com.example.angelorphanage.data

import android.content.Context
import com.example.angelorphanage.domain.GameState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import java.io.File

/**
 * JSON-based implementation of [GameRepository].
 *
 * Persists data to two files in the app's internal storage:
 * - `current_game.json` — the in-progress game state
 * - `game_history.json` — a list of completed game summaries
 *
 * All file I/O is performed on [Dispatchers.IO] to avoid blocking the main thread.
 * Read errors (missing file, corrupted JSON) are handled gracefully by returning
 * null or an empty list rather than throwing.
 */
class JsonGameRepository(private val context: Context) : GameRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private val currentGameFile: File
        get() = File(context.filesDir, CURRENT_GAME_FILE)

    private val gameHistoryFile: File
        get() = File(context.filesDir, GAME_HISTORY_FILE)

    override suspend fun saveCurrentGame(state: GameState) {
        withContext(Dispatchers.IO) {
            val jsonString = json.encodeToString(GameState.serializer(), state)
            currentGameFile.writeText(jsonString)
        }
    }

    override suspend fun loadCurrentGame(): GameState? {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = currentGameFile.readText()
                json.decodeFromString(GameState.serializer(), jsonString)
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun clearCurrentGame() {
        withContext(Dispatchers.IO) {
            currentGameFile.delete()
        }
    }

    override suspend fun saveCompletedGame(summary: GameSummary) {
        withContext(Dispatchers.IO) {
            val history = readHistoryInternal().toMutableList()
            history.add(summary)
            val jsonString = json.encodeToString(
                ListSerializer(GameSummary.serializer()),
                history
            )
            gameHistoryFile.writeText(jsonString)
        }
    }

    override suspend fun getCompletedGames(): List<GameSummary> {
        return withContext(Dispatchers.IO) {
            readHistoryInternal()
        }
    }

    private fun readHistoryInternal(): List<GameSummary> {
        return try {
            val jsonString = gameHistoryFile.readText()
            json.decodeFromString(ListSerializer(GameSummary.serializer()), jsonString)
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val CURRENT_GAME_FILE = "current_game.json"
        private const val GAME_HISTORY_FILE = "game_history.json"
    }
}