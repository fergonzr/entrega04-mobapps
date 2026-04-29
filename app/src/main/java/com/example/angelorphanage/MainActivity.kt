package com.example.angelorphanage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.angelorphanage.ui.GameScreen
import com.example.angelorphanage.ui.theme.AngelOrphanageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AngelOrphanageTheme {
                GameScreen()
            }
        }
    }
}
