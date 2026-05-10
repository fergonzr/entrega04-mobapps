package com.example.angelorphanage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.angelorphanage.data.JsonGameRepository
import com.example.angelorphanage.ui.AppNavigation
import com.example.angelorphanage.ui.theme.AngelOrphanageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = JsonGameRepository(applicationContext)

        setContent {
            AngelOrphanageTheme {
                Surface(
                    modifier = Modifier.safeDrawingPadding()
                        .fillMaxSize()
                ) {
                    AppNavigation(repository = repository)
                }
            }
        }
    }
}
