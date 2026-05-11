package com.juego.petangels.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juego.petangels.R
import com.juego.petangels.data.GameSummary

private val PanelBg = Color(0xB34A3527)
private val PanelBorder = Color(0xFF8C6B4F)
private val CardBg = Color(0xF0E6D6C2)
private val CardBorder = Color(0xFFAA8465)
private val TitleText = Color(0xFFFFF3E0)
private val MainText = Color(0xFF3E2723)
private val SecondaryText = Color(0xFF5D4037)
private val PrimaryCta = Color(0xFF5A3D2B)
private val CtaText = Color(0xFFFFF7E8)

@Composable
fun ScoreScreen(
    gameSummaries: List<GameSummary>,
    onNavigateToTitle: () -> Unit
) {
    val sortedSummaries = gameSummaries.sortedBy { it.elapsedTurns }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.salon),
                contentDescription = "Fondo puntajes",
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
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                val compactHeight = maxHeight < 430.dp
                val headerImageHeight = if (compactHeight) 84.dp else 100.dp

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.9f)
                        .widthIn(max = 900.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(PanelBg)
                        .border(1.dp, PanelBorder, RoundedCornerShape(22.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.titlescreen),
                        contentDescription = "PetAngels",
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(headerImageHeight),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = "Tabla de Puntajes",
                        color = TitleText,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (compactHeight) 20.sp else 24.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (sortedSummaries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = true)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x66FFFFFF))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aun no hay partidas registradas.\nJuega una partida para ver tus resultados aqui.",
                                color = TitleText,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f, fill = true),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(sortedSummaries) { index, summary ->
                                GameSummaryCard(rank = index + 1, summary = summary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onNavigateToTitle,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryCta,
                            contentColor = CtaText
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.46f)
                            .height(if (compactHeight) 44.dp else 50.dp)
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

@Composable
private fun GameSummaryCard(rank: Int, summary: GameSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RankBadge(rank = rank)

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Puntaje ${summary.score}",
                        color = MainText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    CompactStars(rating = summary.rating)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Turnos ${summary.elapsedTurns}",
                        color = SecondaryText,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Nivel ${summary.level}",
                        color = SecondaryText,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Adopciones ${summary.petsAdopted}",
                        color = SecondaryText,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RankBadge(rank: Int) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0xFF7A4F33)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rank.toString(),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun CompactStars(rating: Int) {
    val safe = rating.coerceIn(0, 5)
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            Image(
                painter = painterResource(
                    id = if (i <= safe) R.drawable.estrellacompleta else R.drawable.estrella
                ),
                contentDescription = "Estrella $i",
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
