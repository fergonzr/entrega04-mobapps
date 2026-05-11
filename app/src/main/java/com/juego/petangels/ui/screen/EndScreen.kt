package com.juego.petangels.ui.screen

import android.media.MediaPlayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juego.petangels.R
import com.juego.petangels.data.GameRepository
import com.juego.petangels.domain.ScorerInstance
import com.juego.petangels.domain.ScorerType
import kotlinx.coroutines.delay

private val EndPanel = Color(0xFFF2EADF)
private val EndPanelBorder = Color(0xFFA98569)
private val StatsPanel = Color(0xFFD7C9BA)
private val PetCardColor = Color(0xFFD7D2CC)
private val TitleColor = Color(0xFF3E2723)
private val DetailColor = Color(0xFF5D4037)
private val LovePink = Color(0xFFE573B3)
private val PetNameTagBg = Color(0xFF7A4F33)
private val PetNameTagText = Color(0xFFFFF8E1)

@Composable
fun EndScreen(
    rating: Int,
    score: Int,
    elapsedTurns: Int,
    level: Int,
    pets: List<ScorerInstance>,
    onNavigateToTitle: () -> Unit,
    gameRepository: GameRepository
) {
    val context = LocalContext.current
    val finishPlayer = remember(context) { MediaPlayer.create(context, R.raw.finish) }
    var contentVisible by remember { mutableStateOf(false) }
    var confettiVisible by remember { mutableStateOf(true) }
    var confettiRunning by remember { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 550),
        label = "endScreenFade"
    )
    val confettiProgress by animateFloatAsState(
        targetValue = if (confettiRunning) 1f else 0f,
        animationSpec = tween(durationMillis = 2_000),
        label = "victoryConfettiProgress"
    )

    LaunchedEffect(Unit) {
        gameRepository.clearCurrentGame()
        contentVisible = true
        confettiRunning = true
        delay(2_000)
        confettiVisible = false
    }

    DisposableEffect(finishPlayer) {
        finishPlayer?.start()
        onDispose {
            finishPlayer?.apply {
                runCatching {
                    if (isPlaying) stop()
                }
                release()
            }
        }
    }

    val adoptedPets = pets.filter { it.givenAway }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        Image(
            painter = painterResource(id = R.drawable.salon),
            contentDescription = "Fondo salon",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.24f
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = contentAlpha }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "JUEGO TERMINADO",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        color = TitleColor,
                        textAlign = TextAlign.Center
                    )
                    StarRating(rating = rating)
                }
            }

            item {
                StatsCard(
                    score = score,
                    elapsedTurns = elapsedTurns,
                    level = level,
                    petsFound = pets.size,
                    petsAdopted = adoptedPets.size
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "MASCOTAS ADOPTADAS",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Button(
                        onClick = onNavigateToTitle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC9C1B6),
                            contentColor = Color(0xFF2F2A25)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .width(110.dp)
                            .height(40.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        )
                    ) {
                        Text(
                            text = "Volver",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (adoptedPets.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PetCardColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Todavia no hubo adopciones en esta partida.",
                            modifier = Modifier.padding(16.dp),
                            color = DetailColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                items(adoptedPets.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdoptedPetCard(
                            pet = pair[0],
                            modifier = Modifier.weight(1f)
                        )

                        if (pair.size > 1) {
                            AdoptedPetCard(
                                pet = pair[1],
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        if (confettiVisible) {
            VictoryConfettiOverlay(
                progress = confettiProgress,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun VictoryConfettiOverlay(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFFFFD54F),
        Color(0xFFE57373),
        Color(0xFF4FC3F7),
        Color(0xFF81C784),
        Color(0xFFBA68C8)
    )
    val pieces = listOf(
        0.08f to 0.00f,
        0.18f to 0.10f,
        0.29f to 0.02f,
        0.39f to 0.16f,
        0.50f to 0.06f,
        0.62f to 0.14f,
        0.73f to 0.03f,
        0.84f to 0.12f,
        0.94f to 0.05f,
        0.13f to 0.22f,
        0.35f to 0.26f,
        0.58f to 0.24f,
        0.79f to 0.28f
    )

    BoxWithConstraints(modifier = modifier) {
        pieces.forEachIndexed { index, piece ->
            val delayedProgress = ((progress - piece.second).coerceIn(0f, 1f))
            Box(
                modifier = Modifier
                    .offset(
                        x = maxWidth * piece.first,
                        y = (-28).dp + (maxHeight + 64.dp) * delayedProgress
                    )
                    .graphicsLayer {
                        rotationZ = delayedProgress * 240f + index * 12f
                        alpha = if (delayedProgress <= 0f) 0f else 1f - delayedProgress * 0.25f
                    }
                    .size(if (index % 3 == 0) 9.dp else 7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors[index % colors.size])
            )
        }

        Text(
            text = "\u00A1Juego completado!",
            color = Color(0xFFFFF8E1),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer { alpha = 1f - progress * 0.35f }
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xDD3E2723))
                .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(18.dp))
                .padding(horizontal = 28.dp, vertical = 14.dp)
        )
    }
}

@Composable
fun StarRating(rating: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            Image(
                painter = painterResource(
                    id = if (i <= rating) R.drawable.estrellacompleta else R.drawable.estrella
                ),
                contentDescription = "Estrella $i",
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

@Composable
private fun StatsCard(
    score: Int,
    elapsedTurns: Int,
    level: Int,
    petsFound: Int,
    petsAdopted: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, EndPanelBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = EndPanel)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(StatsPanel)
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = "Estadisticas", color = TitleColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    StatRow(label = "Puntaje final", value = score.toString())
                    StatRow(label = "Nivel alcanzado", value = level.toString())
                    StatRow(label = "Mascotas encontradas", value = petsFound.toString())
                    StatRow(label = "Mascotas adoptadas", value = petsAdopted.toString())
                    StatRow(label = "Turnos", value = elapsedTurns.toString())
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = DetailColor, fontSize = 12.sp)
        Text(text = value, color = TitleColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun AdoptedPetCard(
    pet: ScorerInstance,
    modifier: Modifier = Modifier
) {
    val petImage = when (pet.type) {
        ScorerType.DOG -> R.drawable.perrofeliz
        ScorerType.CAT -> R.drawable.gatofeliz
    }
    val typeLabel = if (pet.type == ScorerType.DOG) "Perro" else "Gato"

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = PetCardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = petImage),
                contentDescription = pet.name,
                modifier = Modifier.size(56.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PetNameTagBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = pet.name,
                        color = PetNameTagText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }
                Text(
                    text = typeLabel,
                    color = DetailColor,
                    fontSize = 12.sp
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 3.dp),
                    thickness = 1.dp,
                    color = Color(0x55FFFFFF)
                )
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "💗", color = LovePink, fontSize = 12.sp)
                    Text(
                        text = "Soy muy feliz con mi nuevo dueno!",
                        color = DetailColor,
                        fontSize = 11.sp,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}
