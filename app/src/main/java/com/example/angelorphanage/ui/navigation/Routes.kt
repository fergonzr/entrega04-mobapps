package com.example.angelorphanage.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe route definitions for the app's navigation graph.
 */
@Serializable
object Routes {
    @Serializable
    object Splash

    @Serializable
    object Title

    @Serializable
    object Game

    @Serializable
    object End

    @Serializable
    object Score
}
