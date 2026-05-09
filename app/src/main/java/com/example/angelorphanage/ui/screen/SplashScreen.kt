package com.example.angelorphanage.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.angelorphanage.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToTitle: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2000L)
        onNavigateToTitle()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.splash),
                contentDescription = "Splash",
                modifier = Modifier.size(220.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
