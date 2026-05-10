package com.example.angelorphanage.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.juego.petangels.R
import com.example.angelorphanage.debug.ENABLE_DEBUG

@Composable
fun TitleScreen(
    hasSavedGame: Boolean,
    onNavigateToGame: () -> Unit,
    onNavigateToScore: () -> Unit,
    onStartDebugGame: (level: Int) -> Unit = {}
) {
    val primaryCta = Color(0xFF5A3D2B)
    val secondaryCta = Color(0xFF2F6F5D)
    val ctaText = Color(0xFFFFF7E8)

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.salon),
                contentDescription = "Fondo orfanato",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x9A1E1A18),
                                Color(0xCC2E231D),
                                Color(0xE64A3527)
                            )
                        )
                    )
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                val compactHeight = maxHeight < 420.dp
                val logoHeight = if (compactHeight) 108.dp else 138.dp
                val mainButtonHeight = if (compactHeight) 48.dp else 54.dp

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.68f)
                        .widthIn(max = 420.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xB34A3527))
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.titlescreen),
                        contentDescription = "Angel Orphanage",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(logoHeight),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(if (compactHeight) 14.dp else 18.dp))

                    Button(
                        onClick = onNavigateToGame,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryCta,
                            contentColor = ctaText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(mainButtonHeight)
                    ) {
                        Text(
                            text = if (hasSavedGame) "Continuar Juego" else "Iniciar Juego",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onNavigateToScore,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = secondaryCta,
                            contentColor = ctaText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(mainButtonHeight)
                    ) {
                        Text(
                            text = "Puntajes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (ENABLE_DEBUG) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "DEBUG: Iniciar en nivel",
                            color = Color(0xFFFFE0B2),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x66322014))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            for (level in 1..5) {
                                Button(
                                    onClick = { onStartDebugGame(level) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF8D5A3B),
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.width(40.dp)
                                ) {
                                    Text(
                                        text = level.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
