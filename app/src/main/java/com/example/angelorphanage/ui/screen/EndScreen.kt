package com.example.angelorphanage.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.angelorphanage.R
import com.example.angelorphanage.data.GameRepository
import com.example.angelorphanage.domain.ScorerInstance

/**
 * Screen shown when a game finishes.
 * Displays a star rating, game statistics, and the list of pets encountered.
 *
 * @param rating Star rating from 0 to 5 based on performance (fewer turns = more stars).
 * @param score Final score achieved in the game.
 * @param elapsedTurns Number of turns taken to complete the game.
 * @param level Final level reached.
 * @param pets List of all pets encountered during the game.
 * @param onNavigateToTitle Callback to navigate back to the title screen.
 */
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
    LaunchedEffect(Unit) {
        gameRepository.clearCurrentGame()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            item {
                Text(
                    text = "¡Juego Terminado!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Star rating
            item {
                StarRating(rating = rating)
            }

            // Statistics card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Estadísticas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        StatRow(label = "Puntaje final", value = score.toString())
                        StatRow(label = "Turnos", value = elapsedTurns.toString())
                        StatRow(label = "Nivel alcanzado", value = level.toString())
                        StatRow(label = "Mascotas encontradas", value = pets.size.toString())
                        StatRow(
                            label = "Mascotas adoptadas",
                            value = pets.count { it.givenAway }.toString()
                        )
                    }
                }
            }

            // Pets encountered section
            item {
                Text(
                    text = "Mascotas encontradas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Pet list
            items(pets) { pet ->
                PetSummaryCard(pet = pet)
            }

            // Back to title button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onNavigateToTitle,
                    modifier = Modifier.width(240.dp)
                ) {
                    Text(
                        text = "Volver al Inicio",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

/**
 * Displays a row of 5 stars, filled or empty based on the rating.
 * Uses the estrellacompleta drawable for filled stars and estrella for empty stars.
 */
@Composable
fun StarRating(rating: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            Image(
                painter = painterResource(
                    id = if (i <= rating) R.drawable.estrellacompleta else R.drawable.estrellacompleta
                ),
                contentDescription = if (i <= rating) "Estrella completa" else "Estrella vacía",
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

/**
 * A simple row displaying a label and its value for the statistics card.
 */
@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * A compact card summarizing a single pet encountered during the game.
 */
@Composable
fun PetSummaryCard(pet: ScorerInstance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = pet.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = pet.type.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            if (pet.givenAway) {
                Text(
                    text = "Adoptada ❤",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
