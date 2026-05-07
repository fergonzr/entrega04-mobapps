package com.example.angelorphanage.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.angelorphanage.domain.GameState
import com.example.angelorphanage.domain.ScorerInstance
import com.example.angelorphanage.ui.navigation.Routes
import com.example.angelorphanage.ui.screen.EndScreen
import com.example.angelorphanage.ui.screen.GameScreen
import com.example.angelorphanage.ui.screen.GameSummary
import com.example.angelorphanage.ui.screen.ScoreScreen
import com.example.angelorphanage.ui.screen.SplashScreen
import com.example.angelorphanage.ui.screen.TitleScreen

/**
 * Result data for a completed game, shared between GameScreen and EndScreen.
 */
data class GameResult(
    val score: Int,
    val elapsedTurns: Int,
    val level: Int,
    val rating: Int,
    val pets: List<ScorerInstance>
)

/**
 * Calculates a star rating (0-5) based on the number of turns taken.
 * Fewer turns = higher rating. Thresholds are preliminary and can be tuned.
 */
fun calculateRating(elapsedTurns: Int): Int = when {
    elapsedTurns <= 15 -> 5
    elapsedTurns <= 25 -> 4
    elapsedTurns <= 35 -> 3
    elapsedTurns <= 50 -> 2
    elapsedTurns <= 70 -> 1
    else -> 0
}

/**
 * Top-level navigation host for the Angel Orphanage app.
 *
 * Manages shared state (game results, score history) and defines
 * the navigation graph connecting all screens:
 *
 * Splash → Title → Game → End → Title
 *                  └→ Score → Title
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // Shared state persisted across screen transitions within a session
    var gameSummaries by remember { mutableStateOf<List<GameSummary>>(emptyList()) }
    var lastGameResult by remember { mutableStateOf<GameResult?>(null) }

    NavHost(
        navController = navController,
        startDestination = Routes.Splash,
        modifier = modifier
    ) {
        composable<Routes.Splash> {
            SplashScreen(
                onNavigateToTitle = {
                    navController.navigate(Routes.Title) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.Title> {
            TitleScreen(
                onNavigateToGame = { navController.navigate(Routes.Game) },
                onNavigateToScore = { navController.navigate(Routes.Score) }
            )
        }

        composable<Routes.Game> {
            GameScreen(
                onGameFinished = { gameState ->
                    val rating = calculateRating(gameState.elapsedTurns)
                    lastGameResult = GameResult(
                        score = gameState.score,
                        elapsedTurns = gameState.elapsedTurns,
                        level = gameState.level,
                        rating = rating,
                        pets = gameState.scorers
                    )
                    // Record this game in the summaries for the Score screen
                    gameSummaries = gameSummaries + GameSummary(
                        score = gameState.score,
                        elapsedTurns = gameState.elapsedTurns,
                        level = gameState.level,
                        petsAdopted = gameState.scorers.count { it.givenAway },
                        rating = rating
                    )
                    navController.navigate(Routes.End) {
                        popUpTo(Routes.Game) { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.End> {
            val result = lastGameResult
            if (result != null) {
                EndScreen(
                    rating = result.rating,
                    score = result.score,
                    elapsedTurns = result.elapsedTurns,
                    level = result.level,
                    pets = result.pets,
                    onNavigateToTitle = {
                        navController.navigate(Routes.Title) {
                            popUpTo(Routes.End) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable<Routes.Score> {
            ScoreScreen(
                gameSummaries = gameSummaries,
                onNavigateToTitle = {
                    navController.popBackStack(Routes.Title, inclusive = false)
                }
            )
        }
    }
}