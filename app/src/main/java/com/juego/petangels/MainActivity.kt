package com.juego.petangels

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.juego.petangels.data.JsonGameRepository
import com.juego.petangels.ui.AppNavigation
import com.juego.petangels.ui.theme.AngelOrphanageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = JsonGameRepository(applicationContext)

        setContent {
            AngelOrphanageTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    AppNavigation(repository = repository)
                }
            }
        }
    }
}
