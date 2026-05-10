package com.example.angelorphanage.ui.screen

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.angelorphanage.R
import com.example.angelorphanage.data.GameRepository
import com.example.angelorphanage.domain.ScorerInstance
import com.example.angelorphanage.domain.ScorerType

private val EndPanel = Color(0xFFF2EADF)
private val EndPanelBorder = Color(0xFFA98569)
private val StatsPanel = Color(0xFFD7C9BA)
private val PetCardColor = Color(0xFFD7D2CC)
private val TitleColor = Color(0xFF3E2723)
private val DetailColor = Color(0xFF5D4037)
private val LovePink = Color(0xFFE573B3)

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

    LaunchedEffect(Unit) {
        gameRepository.clearCurrentGame()
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
                Text(
                    text = pet.name,
                    color = TitleColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1
                )
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
