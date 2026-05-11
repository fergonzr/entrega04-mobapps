package com.juego.petangels.ui.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.juego.petangels.R

@Composable
private fun AnimatedTitleButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    alpha: Float,
    offsetX: Float,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 520f
        ),
        label = "titleButtonPressScale"
    )

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            translationX = offsetX
            scaleX = pressScale
            scaleY = pressScale
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

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
    var titleVisible by remember { mutableStateOf(false) }
    var buttonsVisible by remember { mutableStateOf(false) }
    val titleAlpha by animateFloatAsState(
        targetValue = if (titleVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 850,
            easing = FastOutSlowInEasing
        ),
        label = "titleAlpha"
    )
    val titleOffsetY by animateFloatAsState(
        targetValue = if (titleVisible) 0f else 32f,
        animationSpec = tween(
            durationMillis = 850,
            easing = FastOutSlowInEasing
        ),
        label = "titleOffsetY"
    )
    val titleScale by animateFloatAsState(
        targetValue = if (titleVisible) 1f else 0.78f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = 150f
        ),
        label = "titleScale"
    )
    val scrimAlpha by animateFloatAsState(
        targetValue = if (titleVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 650,
            easing = FastOutSlowInEasing
        ),
        label = "scrimAlpha"
    )
    val firstButtonAlpha by animateFloatAsState(
        targetValue = if (buttonsVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 420, delayMillis = 120, easing = FastOutSlowInEasing),
        label = "firstButtonAlpha"
    )
    val firstButtonOffset by animateFloatAsState(
        targetValue = if (buttonsVisible) 0f else -42f,
        animationSpec = tween(durationMillis = 420, delayMillis = 120, easing = FastOutSlowInEasing),
        label = "firstButtonOffset"
    )
    val secondButtonAlpha by animateFloatAsState(
        targetValue = if (buttonsVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 420, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "secondButtonAlpha"
    )
    val secondButtonOffset by animateFloatAsState(
        targetValue = if (buttonsVisible) 0f else 42f,
        animationSpec = tween(durationMillis = 420, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "secondButtonOffset"
    )
    val logoFloatTransition = rememberInfiniteTransition(label = "logoFloat")
    val logoFloatY by logoFloatTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoFloatY"
    )

    LaunchedEffect(Unit) {
        titleVisible = true
        kotlinx.coroutines.delay(560)
        buttonsVisible = true
    }

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
                            .graphicsLayer {
                                alpha = titleAlpha
                                translationY = titleOffsetY + if (titleVisible) logoFloatY else 0f
                                scaleX = titleScale
                                scaleY = titleScale
                            }
                            .fillMaxWidth()
                            .height(logoHeight),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(if (compactHeight) 14.dp else 18.dp))

                    AnimatedTitleButton(
                        text = if (hasSavedGame) "Continuar Juego" else "Iniciar Juego",
                        onClick = onNavigateToGame,
                        containerColor = primaryCta,
                        contentColor = ctaText,
                        alpha = firstButtonAlpha,
                        offsetX = firstButtonOffset,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(mainButtonHeight)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AnimatedTitleButton(
                        text = "Puntajes",
                        onClick = onNavigateToScore,
                        containerColor = secondaryCta,
                        contentColor = ctaText,
                        alpha = secondButtonAlpha,
                        offsetX = secondButtonOffset,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(mainButtonHeight)
                    )
                }
            }
        }
    }
}
