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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.angelorphanage.R
import com.example.angelorphanage.domain.GameState
import com.example.angelorphanage.domain.ResourceType
import com.example.angelorphanage.domain.ScorerInstance
import com.example.angelorphanage.domain.ScorerType

/**
 * Predefined arbitrary positions for pets in the salon room.
 * Each pair is (xFraction, yFraction) relative to the center play area.
 * Pets are assigned positions in order of their index in the scorers list.
 */
private val PET_POSITIONS = listOf(
    0.04f to 0.05f,
    0.28f to 0.02f,
    0.52f to 0.10f,
    0.76f to 0.04f,
    0.08f to 0.40f,
    0.32f to 0.44f,
    0.56f to 0.38f,
    0.80f to 0.42f,
    0.16f to 0.72f,
    0.42f to 0.68f,
    0.66f to 0.70f,
    0.88f to 0.25f,
)

/**
 * The main game screen. Displays the salon room with pets, resource indicators,
 * a powerups button, game stats, and a turn-advancement button.
 * Forces landscape orientation (set in AndroidManifest).
 */
@Composable
fun GameScreen(onGameFinished: (GameState) -> Unit) {
    var gameState by remember { mutableStateOf(GameState()) }
    var allocationMap by remember {
        mutableStateOf<List<Map<ResourceType, Int>>>(
            List(gameState.scorers.size) { emptyMap() }
        )
    }

    // Ensure allocation map grows when new pets arrive
    if (allocationMap.size < gameState.scorers.size) {
        allocationMap = allocationMap +
                List(gameState.scorers.size - allocationMap.size) { emptyMap<ResourceType, Int>() }
    }

    // Navigate to end screen when game is finished
    LaunchedEffect(gameState.finished) {
        if (gameState.finished) {
            onGameFinished(gameState)
        }
    }

    // Calculate total allocated resources across all pets
    val totalAllocated = remember(allocationMap) {
        allocationMap.fold(emptyMap<ResourceType, Int>()) { acc, map ->
            acc + map.mapValues { (key, value) -> (acc[key] ?: 0) + value }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background — the salon room image
        Image(
            painter = painterResource(id = R.drawable.salon),
            contentDescription = "Salón del orfanato",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Top HUD bar — semi-transparent overlay
        TopHudBar(
            currentResources = gameState.currentResources,
            totalAllocated = totalAllocated,
            level = gameState.level,
            elapsedTurns = gameState.elapsedTurns,
            score = gameState.score,
            powerUpsUnlocked = gameState.powerUpsUnlocked,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Center play area — pets placed at arbitrary positions
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 52.dp, bottom = 60.dp, start = 8.dp, end = 8.dp)
        ) {
            val areaWidth = maxWidth
            val areaHeight = maxHeight

            gameState.scorers.forEachIndexed { index, scorer ->
                if (!scorer.givenAway && index < PET_POSITIONS.size) {
                    val (xFraction, yFraction) = PET_POSITIONS[index]

                    val currentAllocation = allocationMap.getOrElse(index) { emptyMap() }
                    val currentFood = currentAllocation[ResourceType.FOOD] ?: 0
                    val currentWater = currentAllocation[ResourceType.WATER] ?: 0

                    // Allocation availability checks
                    val availableFood = gameState.currentResources[ResourceType.FOOD] ?: 0
                    val availableWater = gameState.currentResources[ResourceType.WATER] ?: 0
                    val allocatedFood = totalAllocated[ResourceType.FOOD] ?: 0
                    val allocatedWater = totalAllocated[ResourceType.WATER] ?: 0
                    val canAllocateMoreFood =
                        allocatedFood < availableFood &&
                        ((scorer.meters[ResourceType.FOOD] ?: 0) + currentFood) <
                            (scorer.type.meterLimits[ResourceType.FOOD]?.second ?: Int.MAX_VALUE)
                    val canAllocateMoreWater =
                        allocatedWater < availableWater &&
                        ((scorer.meters[ResourceType.WATER] ?: 0) + currentWater) <
                            (scorer.type.meterLimits[ResourceType.WATER]?.second ?: Int.MAX_VALUE)

                    PetDisplay(
                        scorer = scorer,
                        currentFoodAllocation = currentFood,
                        currentWaterAllocation = currentWater,
                        canAllocateMoreFood = canAllocateMoreFood,
                        canAllocateMoreWater = canAllocateMoreWater,
                        onAllocate = { newAllocation ->
                            val newAllocationMap = allocationMap.toMutableList()
                            if (index < newAllocationMap.size) {
                                newAllocationMap[index] = newAllocation
                            }
                            allocationMap = newAllocationMap
                        },
                        modifier = Modifier.offset(
                            x = areaWidth * xFraction,
                            y = areaHeight * yFraction
                        )
                    )
                }
            }
        }

        // Bottom-left: Run turn button
        Button(
            onClick = {
                val paddedAllocation = allocationMap +
                        List(
                            maxOf(0, gameState.scorers.size - allocationMap.size)
                        ) { emptyMap<ResourceType, Int>() }
                gameState = gameState.run(paddedAllocation)
                allocationMap = List(gameState.scorers.size) { emptyMap() }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Avanzar Día",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

// ─── Top HUD Bar ──────────────────────────────────────────────────────

@Composable
fun TopHudBar(
    currentResources: Map<ResourceType, Int>,
    totalAllocated: Map<ResourceType, Int>,
    level: Int,
    elapsedTurns: Int,
    score: Int,
    powerUpsUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Resource indicators (food and water)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ResourceIndicator(
                type = ResourceType.FOOD,
                amount = currentResources[ResourceType.FOOD] ?: 0,
                allocated = totalAllocated[ResourceType.FOOD] ?: 0
            )
            ResourceIndicator(
                type = ResourceType.WATER,
                amount = currentResources[ResourceType.WATER] ?: 0,
                allocated = totalAllocated[ResourceType.WATER] ?: 0
            )
        }

        // Center: Powerups button (stub)
        Button(
            onClick = { /* TODO: Powerup buying dialog */ },
            enabled = powerUpsUnlocked,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7E57C2),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF424242),
                disabledContentColor = Color(0xFF9E9E9E)
            ),
            modifier = Modifier.height(36.dp)
        ) {
            Text("🛒 Powerups", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        // Right: Level, Day, Score (coins)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoBadge(icon = "⭐", label = "Nivel", value = level.toString())
            InfoBadge(icon = "📅", label = "Día", value = elapsedTurns.toString())
            InfoBadge(icon = "🪙", label = "Monedas", value = score.toString())
        }
    }
}

/**
 * Shows a resource type with its current stock and allocated amount.
 * Uses bowl/water images from drawable resources.
 */
@Composable
fun ResourceIndicator(type: ResourceType, amount: Int, allocated: Int) {
    val imageRes = when (type) {
        ResourceType.FOOD -> if (amount > 0) R.drawable.pplatolleno else R.drawable.pplatovacio
        ResourceType.WATER -> if (amount > 0) R.drawable.pconagua else R.drawable.psinagua
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = stringResource(type.nameRes),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = amount.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            if (allocated > 0) {
                Text(
                    text = "-$allocated",
                    color = Color(0xFFFFAB91),
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * A compact info badge showing an icon, label, and value.
 */
@Composable
fun InfoBadge(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 14.sp)
        Text(
            text = label,
            color = Color(0xFFB0BEC5),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

// ─── Pet Display ──────────────────────────────────────────────────────

/**
 * Displays a single pet with vertical meter bars on the left,
 * the pet image, name, and allocation controls.
 * Positioned at an arbitrary offset via the modifier.
 */
@Composable
fun PetDisplay(
    scorer: ScorerInstance,
    currentFoodAllocation: Int,
    currentWaterAllocation: Int,
    canAllocateMoreFood: Boolean,
    canAllocateMoreWater: Boolean,
    onAllocate: (Map<ResourceType, Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine if pet is happy: all meters >= 0
    val isHappy = scorer.meters.values.all { it >= 0 }

    // Select the appropriate pet image
    val petImageRes = when (scorer.type) {
        ScorerType.DOG -> if (isHappy) R.drawable.perrofeliz else R.drawable.perrotriste
        ScorerType.CAT -> if (isHappy) R.drawable.gatofeliz else R.drawable.gatotriste
    }

    Box(modifier = modifier) {
        // Semi-transparent background pill behind the pet
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vertical meter bars
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Food meter column
                MeterColumn(
                    label = "C",
                    value = scorer.meters[ResourceType.FOOD] ?: 0,
                    minLimit = scorer.type.meterLimits[ResourceType.FOOD]?.first ?: 0,
                    maxLimit = scorer.type.meterLimits[ResourceType.FOOD]?.second ?: 0,
                    allocation = currentFoodAllocation,
                    canAllocateMore = canAllocateMoreFood,
                    onDecrement = {
                        onAllocate(
                            mapOf(
                                ResourceType.FOOD to (currentFoodAllocation - 1).coerceAtLeast(0),
                                ResourceType.WATER to currentWaterAllocation
                            )
                        )
                    },
                    onIncrement = {
                        onAllocate(
                            mapOf(
                                ResourceType.FOOD to currentFoodAllocation + 1,
                                ResourceType.WATER to currentWaterAllocation
                            )
                        )
                    }
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Water meter column
                MeterColumn(
                    label = "A",
                    value = scorer.meters[ResourceType.WATER] ?: 0,
                    minLimit = scorer.type.meterLimits[ResourceType.WATER]?.first ?: 0,
                    maxLimit = scorer.type.meterLimits[ResourceType.WATER]?.second ?: 0,
                    allocation = currentWaterAllocation,
                    canAllocateMore = canAllocateMoreWater,
                    onDecrement = {
                        onAllocate(
                            mapOf(
                                ResourceType.FOOD to currentFoodAllocation,
                                ResourceType.WATER to (currentWaterAllocation - 1).coerceAtLeast(0)
                            )
                        )
                    },
                    onIncrement = {
                        onAllocate(
                            mapOf(
                                ResourceType.FOOD to currentFoodAllocation,
                                ResourceType.WATER to currentWaterAllocation + 1
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Pet image and name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = petImageRes),
                    contentDescription = scorer.name,
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    text = scorer.name,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.width(56.dp)
                )
            }
        }
    }
}

/**
 * A single vertical meter column for one resource type on a pet.
 * Shows a label, a vertical segmented bar, and allocation +/- controls.
 */
@Composable
fun MeterColumn(
    label: String,
    value: Int,
    minLimit: Int,
    maxLimit: Int,
    allocation: Int,
    canAllocateMore: Boolean,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label
        Text(
            text = label,
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Vertical meter bar
        VerticalMeterBar(
            value = value,
            minLimit = minLimit,
            maxLimit = maxLimit,
            modifier = Modifier
                .width(12.dp)
                .height(48.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Allocation controls row
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decrement button (small circle)
            Button(
                onClick = onDecrement,
                modifier = Modifier.size(18.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE57373),
                    contentColor = Color.White
                ),
                shape = CircleShape
            ) {
                Text(text = "−", fontSize = 10.sp, textAlign = TextAlign.Center)
            }

            // Allocation count
            Text(
                text = allocation.toString(),
                color = if (allocation > 0) Color(0xFFFFF176) else Color(0xFF9E9E9E),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(12.dp),
                textAlign = TextAlign.Center
            )

            // Increment button (small circle)
            Button(
                onClick = onIncrement,
                modifier = Modifier.size(18.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                enabled = canAllocateMore,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF66BB6A),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF424242),
                    disabledContentColor = Color(0xFF757575)
                ),
                shape = CircleShape
            ) {
                Text(text = "+", fontSize = 10.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

/**
 * A vertical segmented meter bar.
 * Fills from bottom to top. Segments in the negative zone (≤ 0) are colored
 * orange-red when filled; segments in the positive zone (> 0) are colored
 * green when filled. Unfilled segments are semi-transparent gray.
 */
@Composable
fun VerticalMeterBar(
    value: Int,
    minLimit: Int,
    maxLimit: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0x30FFFFFF)),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Display segments from top (max) to bottom (min+1)
        for (segment in maxLimit downTo (minLimit + 1)) {
            val isFilled = segment <= value
            val isNegativeZone = segment <= 0

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(1.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            !isFilled -> Color(0x40FFFFFF) // unfilled: semi-transparent
                            isNegativeZone -> Color(0xFFFF7043) // danger zone: orange-red
                            else -> Color(0xFF66BB6A) // good zone: green
                        }
                    )
            )
        }
    }
}