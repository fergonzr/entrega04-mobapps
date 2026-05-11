package com.juego.petangels.ui.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juego.petangels.R

@Composable
fun CreditScreen(
    onNavigateBack: () -> Unit
) {
    val returnCta = Color(0xFF5A3D2B)
    val ctaText = Color(0xFFFFF7E8)

    var contentVisible by remember { mutableStateOf(false) }

    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 750,
            easing = FastOutSlowInEasing
        ),
        label = "creditsAlpha"
    )
    val contentOffsetY by animateFloatAsState(
        targetValue = if (contentVisible) 0f else 24f,
        animationSpec = tween(
            durationMillis = 750,
            easing = FastOutSlowInEasing
        ),
        label = "creditsOffsetY"
    )
    val scrimAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 550,
            easing = FastOutSlowInEasing
        ),
        label = "scrimAlpha"
    )

    val logoFloatTransition = rememberInfiniteTransition(label = "logoFloat")
    val logoFloatY by logoFloatTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoFloatY"
    )

    val backInteractionSource = remember { MutableInteractionSource() }
    val isBackPressed by backInteractionSource.collectIsPressedAsState()
    val backScale by animateFloatAsState(
        targetValue = if (isBackPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 520f),
        label = "backPressScale"
    )

    LaunchedEffect(Unit) { contentVisible = true }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Same background as TitleScreen
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
                                Color(0x9A1E1A18).copy(alpha = 0.6f * scrimAlpha),
                                Color(0xCC2E231D).copy(alpha = 0.8f * scrimAlpha),
                                Color(0xE64A3527).copy(alpha = 0.9f * scrimAlpha)
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
                val logoHeight = if (compactHeight) 90.dp else 120.dp

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
                    // Logo
                    Image(
                        painter = painterResource(id = R.drawable.titlescreen),
                        contentDescription = "PetAngels",
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = contentAlpha
                                translationY = contentOffsetY + if (contentVisible) logoFloatY else 0f
                            }
                            .fillMaxWidth()
                            .height(logoHeight),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // App name
                    Text(
                        text = "PetAngels",
                        color = Color(0xFFFFF7E8),
                        fontSize = if (compactHeight) 22.sp else 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer {
                            alpha = contentAlpha
                            translationY = contentOffsetY
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Slogan
                    Text(
                        text = "Porque cada mascota merece un hogar",
                        color = Color(0xFFD9B89A),
                        fontSize = if (compactHeight) 11.sp else 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer {
                            alpha = contentAlpha
                            translationY = contentOffsetY
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Developers header
                    Text(
                        text = "Desarrollado por",
                        color = Color(0xFFD9B89A),
                        fontSize = if (compactHeight) 10.sp else 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer {
                            alpha = contentAlpha
                            translationY = contentOffsetY
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Developer names
                    val developers = listOf(
                        "Fernando González",
                        "Zharick Rocío",
                        "Nicole Yuqui"
                    )
                    developers.forEach { name ->
                        Text(
                            text = name,
                            color = Color(0xFFFFF7E8),
                            fontSize = if (compactHeight) 12.sp else 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.graphicsLayer {
                                alpha = contentAlpha
                                translationY = contentOffsetY
                            }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Back button
                    Button(
                        onClick = onNavigateBack,
                        interactionSource = backInteractionSource,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = returnCta,
                            contentColor = ctaText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (compactHeight) 42.dp else 48.dp)
                            .graphicsLayer {
                                scaleX = backScale
                                scaleY = backScale
                                alpha = contentAlpha
                            }
                    ) {
                        Text(
                            text = "Volver",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}