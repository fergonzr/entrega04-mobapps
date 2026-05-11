package com.juego.petangels.ui.screen

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.juego.petangels.ui.screen.PowerupStoreDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.juego.petangels.R
import com.juego.petangels.data.GameRepository
import com.juego.petangels.data.GameSummary
import com.juego.petangels.debug.RICH_DEBUG_STATE
import com.juego.petangels.domain.GameState
import com.juego.petangels.ui.theme.AngelOrphanageTheme
import com.juego.petangels.domain.ResourceType
import com.juego.petangels.domain.ScorerInstance
import com.juego.petangels.domain.ScorerType
import com.juego.petangels.domain.givenaway_pet_level_target
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Maximum number of active (non-givenaway) pets that can be displayed at once.
 * The game logic ensures there are never more than this many active pets.
 */
private const val MAX_ACTIVE_PETS = 6
private const val PET_AMBIENT_SOUND_INTERVAL_MS = 9_000L

/**
 * Fixed display positions for pets in the salon room.
 * Each pair is (xFraction, yFraction) relative to the center play area.
 * Active pets are dynamically mapped to these positions; when a pet is given
 * away, its position is freed for the next incoming pet.
 */
private val PET_POSITIONS = listOf(
    0.12f to 0.30f,
    0.34f to 0.16f,
    0.66f to 0.14f,
    0.18f to 0.56f,
    0.45f to 0.50f,
    0.72f to 0.54f,
)

/**
 * The main game screen. Displays the salon room with pets, resource indicators,
 * a powerups button, game stats, and a turn-advancement button.
 * Forces landscape orientation (set in AndroidManifest).
 *
 * Allocation mechanism:
 * 1. User taps a resource indicator in the HUD to select it.
 * 2. User taps a pet to allocate +1 of the selected resource to it.
 * 3. Each pet has a reset button to clear its allocation.
 */
@Composable
fun GameScreen(
    onGameFinished: (GameState) -> Unit,
    repository: GameRepository,
    debugInitialState: GameState? = null
) {
    val context = LocalContext.current

    val soundPool = remember {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder()
            .setAudioAttributes(attributes)
            .setMaxStreams(8)
            .build()
    }
    val barkSoundId = remember(soundPool, context) { soundPool.load(context, R.raw.bark, 1) }
    val meowSoundId = remember(soundPool, context) { soundPool.load(context, R.raw.meow, 1) }
    val levelUpSoundId = remember(soundPool, context) { soundPool.load(context, R.raw.levelup, 1) }
    val putFoodWaterSoundId = remember(soundPool, context) { soundPool.load(context, R.raw.putfoodwater, 1) }
    val newDaySoundId = remember(soundPool, context) { soundPool.load(context, R.raw.newday, 1) }
    val coinSoundId = remember(soundPool, context) { soundPool.load(context, R.raw.coin, 1) }
    val failSoundId = remember(soundPool, context) { soundPool.load(context, R.raw.fail, 1) }
    val buyPowerupSoundId = remember(soundPool, context) { soundPool.load(context, R.raw.buypowerup, 1) }

    val themePlayer = remember(context) { MediaPlayer.create(context, R.raw.theme) }

    fun playSfx(soundId: Int) {
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    fun playBarkSfx() {
        // Layer a softer second bark to make it stand out a bit more.
        soundPool.play(barkSoundId, 1f, 1f, 1, 0, 1f)
        soundPool.play(barkSoundId, 0.65f, 0.65f, 0, 0, 1f)
    }

    var gameState by remember { mutableStateOf(debugInitialState ?: GameState()) }
    var allocationMap by remember {
        mutableStateOf<List<Map<ResourceType, Int>>>(
            List(gameState.scorers.size) { emptyMap() }
        )
    }
    var allocationHistory by remember { mutableStateOf<List<Pair<Int, ResourceType>>>(emptyList()) }
    var selectedResource by remember { mutableStateOf<ResourceType?>(null) }
    var hasLoaded by remember { mutableStateOf(debugInitialState != null) }
    var levelUpMessage by remember { mutableStateOf<Int?>(null) }
    var levelCelebrationKey by remember { mutableStateOf(0) }
    var showPowerupLockedMessage by remember { mutableStateOf(false) }

    // Dynamic mapping from active pet names to display positions (0 until MAX_ACTIVE_PETS).
    // Given-away pets are removed; new pets are assigned to freed positions.
    var petPositionMap by remember { mutableStateOf(mapOf<String, Int>()) }

    // Whether the powerup store dialog is currently visible
    var showPowerupStore by remember { mutableStateOf(false) }

    // Load saved game on first composition (skip if debug state was provided)
    LaunchedEffect(Unit) {
        if (debugInitialState == null) {
            val saved = repository.loadCurrentGame()
            if (saved != null) {
                gameState = saved
                allocationMap = List(saved.scorers.size) { emptyMap() }
                allocationHistory = emptyList()
            }
            hasLoaded = true
        }
    }

    // Save state after each turn
    LaunchedEffect(gameState.elapsedTurns) {
        if (hasLoaded && gameState.elapsedTurns > 0) {
            repository.saveCurrentGame(gameState)
        }
    }

    // Start and stop background music + release audio resources when leaving this screen
    DisposableEffect(themePlayer, soundPool) {
        themePlayer?.isLooping = true
        themePlayer?.start()

        onDispose {
            themePlayer?.apply {
                runCatching {
                    if (isPlaying) stop()
                }
                release()
            }
            soundPool.release()
        }
    }

    // Play ambient meow/bark from active pets every few seconds
    LaunchedEffect(gameState.scorers) {
        while (true) {
            delay(PET_AMBIENT_SOUND_INTERVAL_MS)
            val activePetTypes = gameState.scorers
                .filter { !it.givenAway }
                .map { it.type }

            if (activePetTypes.isEmpty()) continue

            when (activePetTypes[Random.nextInt(activePetTypes.size)]) {
                ScorerType.DOG -> playBarkSfx()
                ScorerType.CAT -> playSfx(meowSoundId)
            }
        }
    }

    // Ensure allocation map grows when new pets arrive
    if (allocationMap.size < gameState.scorers.size) {
        allocationMap = allocationMap +
                List(gameState.scorers.size - allocationMap.size) { emptyMap<ResourceType, Int>() }
    }

    // Update pet position map: remove given-away pets, assign new pets to free positions
    val activePetNames = gameState.scorers.filter { !it.givenAway }.map { it.name }.toSet()
    petPositionMap = petPositionMap.toMutableMap().apply {
        keys.retainAll { it in activePetNames }
        val usedPositions = values.toSet()
        val freePositions = (0 until MAX_ACTIVE_PETS).filter { it !in usedPositions }
        val unassignedPets = activePetNames.filter { it !in this }
        unassignedPets.zip(freePositions).forEach { (name, pos) -> this[name] = pos }
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

    GameScreenContent(
        gameState = gameState,
        allocationMap = allocationMap,
        totalAllocated = totalAllocated,
        petPositionMap = petPositionMap,
        selectedResource = selectedResource,
        onAllocate = { index, newAllocation ->
            val newAllocationMap = allocationMap.toMutableList()
            if (index < newAllocationMap.size) {
                val previousAllocation = newAllocationMap[index]
                val oldTotal = previousAllocation.values.sum()
                val newTotal = newAllocation.values.sum()
                if (newTotal > oldTotal) {
                    playSfx(putFoodWaterSoundId)
                    ResourceType.entries
                        .filter { resType ->
                            (newAllocation[resType] ?: 0) > (previousAllocation[resType] ?: 0)
                        }
                        .forEach { increasedType ->
                            val delta =
                                (newAllocation[increasedType] ?: 0) - (previousAllocation[increasedType] ?: 0)
                            repeat(delta) {
                                allocationHistory = allocationHistory + (index to increasedType)
                            }
                        }
                }
                newAllocationMap[index] = newAllocation
            }
            allocationMap = newAllocationMap
        },
        onUndoLastAllocation = {
            if (allocationHistory.isEmpty()) return@GameScreenContent

            val (scorerIndex, resourceType) = allocationHistory.last()
            allocationHistory = allocationHistory.dropLast(1)

            if (scorerIndex >= allocationMap.size) return@GameScreenContent

            val newAllocationMap = allocationMap.toMutableList()
            val currentAllocation = newAllocationMap[scorerIndex]
            val currentAmount = currentAllocation[resourceType] ?: 0
            if (currentAmount <= 0) return@GameScreenContent

            newAllocationMap[scorerIndex] =
                if (currentAmount == 1) currentAllocation - resourceType
                else currentAllocation + (resourceType to (currentAmount - 1))

            allocationMap = newAllocationMap
        },
        canUndoAllocation = allocationHistory.isNotEmpty(),
        onRunTurn = {
            playSfx(newDaySoundId)
            val paddedAllocation = allocationMap +
                    List(
                        maxOf(0, gameState.scorers.size - allocationMap.size)
                    ) { emptyMap<ResourceType, Int>() }
            val previousLevel = gameState.level
            val previousScore = gameState.score
            val newState = gameState.run(paddedAllocation)
            if (newState.level > previousLevel) {
                playSfx(levelUpSoundId)
                levelUpMessage = newState.level
                levelCelebrationKey += 1
            }
            if (newState.score > previousScore) {
                playSfx(coinSoundId)
            } else if (newState.score < previousScore) {
                playSfx(failSoundId)
            }
            gameState = newState
            allocationMap = List(gameState.scorers.size) { emptyMap() }
            allocationHistory = emptyList()
        },
        onSelectResource = { resourceType ->
            selectedResource = if (selectedResource == resourceType) null else resourceType
        },
        onOpenPowerupStore = {
            if (gameState.level < 2 || !gameState.powerUpsUnlocked) {
                showPowerupLockedMessage = true
            } else {
                showPowerupStore = true
            }
        },
        levelUpMessage = levelUpMessage,
        levelCelebrationKey = levelCelebrationKey,
        onLevelMessageDone = { levelUpMessage = null },
        showPowerupLockedMessage = showPowerupLockedMessage,
        onPowerupLockedMessageDone = { showPowerupLockedMessage = false }
    )

    // Powerup store dialog
    if (showPowerupStore) {
        PowerupStoreDialog(
            gameState = gameState,
            onConfirm = { newState ->
                playSfx(buyPowerupSoundId)
                gameState = newState
                showPowerupStore = false
            },
            onDismiss = { showPowerupStore = false }
        )
    }
}

/**
 * Stateless rendering of the game screen. Accepts all display data and
 * callbacks as parameters so it can be previewed with a fixed GameState.
 */
@Composable
fun GameScreenContent(
    gameState: GameState,
    allocationMap: List<Map<ResourceType, Int>>,
    totalAllocated: Map<ResourceType, Int>,
    petPositionMap: Map<String, Int>,
    selectedResource: ResourceType?,
    onAllocate: (Int, Map<ResourceType, Int>) -> Unit,
    onUndoLastAllocation: () -> Unit,
    canUndoAllocation: Boolean,
    onRunTurn: () -> Unit,
    onSelectResource: (ResourceType) -> Unit,
    onOpenPowerupStore: () -> Unit,
    levelUpMessage: Int? = null,
    levelCelebrationKey: Int = 0,
    onLevelMessageDone: () -> Unit = {},
    showPowerupLockedMessage: Boolean = false,
    onPowerupLockedMessageDone: () -> Unit = {}
) {
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
            selectedResource = selectedResource,
            onSelectResource = onSelectResource,
            onOpenPowerupStore = onOpenPowerupStore,
            modifier = Modifier.align(Alignment.TopStart)
        )

        AdoptionProgressBadge(
            level = gameState.level,
            adoptedPets = gameState.scorers.count { it.givenAway },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 42.dp)
        )

        // Center play area — pets placed at arbitrary positions
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 52.dp, bottom = 60.dp, start = 8.dp, end = 8.dp)
        ) {
            val areaWidth = maxWidth
            val areaHeight = maxHeight

            gameState.scorers.forEachIndexed { scorerIndex, scorer ->
                val displayPosition = petPositionMap[scorer.name] ?: return@forEachIndexed
                if (!scorer.givenAway) {
                    val (xFraction, yFraction) = PET_POSITIONS[displayPosition]

                    val currentAllocation = allocationMap.getOrElse(scorerIndex) { emptyMap() }

                    // Compute per-resource allocation availability for this pet
                    val canAllocateMore = ResourceType.entries.associateWith { resType ->
                        val available = gameState.currentResources[resType] ?: 0
                        val totalAlloc = totalAllocated[resType] ?: 0
                        val currentAlloc = currentAllocation[resType] ?: 0
                        val meterValue = scorer.meters[resType] ?: 0
                        val maxLimit = scorer.type.meterLimits[resType]?.second ?: Int.MAX_VALUE
                        totalAlloc < available && (meterValue + currentAlloc) < maxLimit
                    }

                    PetDisplay(
                        scorer = scorer,
                        currentAllocation = currentAllocation,
                        canAllocateMore = canAllocateMore,
                        selectedResource = selectedResource,
                        levelCelebrationKey = levelCelebrationKey,
                        onAllocate = { newAllocation ->
                            onAllocate(scorerIndex, newAllocation)
                        },
                        modifier = Modifier.offset(
                            x = areaWidth * xFraction,
                            y = areaHeight * yFraction
                        )
                    )
                }
            }
        }

        // Bottom-right action buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onUndoLastAllocation,
                enabled = canUndoAllocation,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFEF9A9A),
                    disabledContentColor = Color(0xFFB71C1C)
                )
            ) {
                Text(
                    text = "Deshacer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = onRunTurn,
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

        levelUpMessage?.let { level ->
            SimpleLevelUpMessage(
                level = level,
                onDone = onLevelMessageDone,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        PowerupLockedMessage(
            visible = showPowerupLockedMessage,
            onDone = onPowerupLockedMessageDone,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 58.dp)
        )
    }
}

// ─── Top HUD Bar ──────────────────────────────────────────────────────

@Composable
private fun AdoptionProgressBadge(
    level: Int,
    adoptedPets: Int,
    modifier: Modifier = Modifier
) {
    val levelTarget = givenaway_pet_level_target(level)
    val previousLevelTarget = givenaway_pet_level_target(level - 1)
    val requiredThisLevel = (levelTarget - previousLevelTarget).coerceAtLeast(0)
    val adoptedThisLevel = (adoptedPets - previousLevelTarget).coerceIn(0, requiredThisLevel)
    val remainingPets = (requiredThisLevel - adoptedThisLevel).coerceAtLeast(0)
    val message = if (remainingPets == 0) {
        "\u00A1Nivel completado!"
    } else {
        "Faltan $remainingPets mascotas por adoptar"
    }

    Text(
        text = message,
        color = Color(0xFFFFF8E1),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xCC3E2723))
            .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    )
}

@Composable
fun TopHudBar(
    currentResources: Map<ResourceType, Int>,
    totalAllocated: Map<ResourceType, Int>,
    level: Int,
    elapsedTurns: Int,
    score: Int,
    powerUpsUnlocked: Boolean,
    selectedResource: ResourceType?,
    onSelectResource: (ResourceType) -> Unit,
    onOpenPowerupStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hudBg = Color(0xFF34251D)
    val hudBorder = Color(0xFF34251D)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(hudBg)
            .border(1.5.dp, hudBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1.9f)
                .padding(horizontal = 2.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ResourceIndicator(
                type = ResourceType.FOOD,
                amount = currentResources[ResourceType.FOOD] ?: 0,
                allocated = totalAllocated[ResourceType.FOOD] ?: 0,
                isSelected = selectedResource == ResourceType.FOOD,
                onClick = { onSelectResource(ResourceType.FOOD) },
                modifier = Modifier.weight(1f)
            )
            ResourceIndicator(
                type = ResourceType.WATER,
                amount = currentResources[ResourceType.WATER] ?: 0,
                allocated = totalAllocated[ResourceType.WATER] ?: 0,
                isSelected = selectedResource == ResourceType.WATER,
                onClick = { onSelectResource(ResourceType.WATER) },
                modifier = Modifier.weight(1f)
            )
        }

        val powerupButtonColor = if (powerUpsUnlocked) Color(0xFFFFD54F) else Color(0xFF8D6E63)
        val powerupTextColor = if (powerUpsUnlocked) Color(0xFF4E342E) else Color(0xFFFFE0B2)

        Button(
            onClick = onOpenPowerupStore,
            colors = ButtonDefaults.buttonColors(
                containerColor = powerupButtonColor,
                contentColor = powerupTextColor
            ),
            modifier = Modifier
                .weight(1.15f)
                .height(30.dp)
                .graphicsLayer { alpha = if (powerUpsUnlocked) 1f else 0.72f },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Powerups",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Row(
            modifier = Modifier
                .weight(1.2f)
                .padding(horizontal = 2.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoBadge(
                icon = "Lvl",
                value = level.toString(),
                modifier = Modifier.weight(1f)
            )
            InfoBadge(
                icon = "Dia",
                value = elapsedTurns.toString(),
                modifier = Modifier.weight(1f)
            )
            InfoBadge(
                icon = "🪙",
                value = score.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Shows a resource type with its current stock and allocated amount.
 * Tappable: selects this resource as the active allocation target.
 * When selected, displays a highlighted border.
 */
@Composable
fun ResourceIndicator(
    type: ResourceType,
    amount: Int,
    allocated: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageRes = when (type) {
        ResourceType.FOOD -> if (amount > 0) R.drawable.pplatolleno else R.drawable.pplatovacio
        ResourceType.WATER -> if (amount > 0) R.drawable.pconagua else R.drawable.psinagua
    }
    val selectedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "selectedResourceScale"
    )

    val highlightModifier = if (isSelected) {
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(10.dp))
            .background(Color(0xFF7A4F33))
    } else {
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF7A4F33))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .graphicsLayer {
                scaleX = selectedScale
                scaleY = selectedScale
            }
            .then(highlightModifier)
            .clickable(onClick = onClick)
            .height(30.dp)
            .padding(horizontal = 5.dp, vertical = 0.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = stringResource(type.nameRes),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = amount.toString(),
                color = if (isSelected) Color(0xFFFFF59D) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            if (allocated > 0) {
                Text(
                    text = "-$allocated",
                    color = Color(0xFFFFCCBC),
                    fontSize = 7.sp
                )
            }
        }
    }
}

/**
 * A compact info badge showing an icon and value.
 */
@Composable
fun InfoBadge(
    icon: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF7A4F33))
            .height(30.dp)
            .padding(horizontal = 4.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = icon, fontSize = 8.sp, color = Color(0xFFFFE0B2), fontWeight = FontWeight.Bold)
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
fun PetDisplay(
    scorer: ScorerInstance,
    currentAllocation: Map<ResourceType, Int>,
    canAllocateMore: Map<ResourceType, Boolean>,
    selectedResource: ResourceType?,
    levelCelebrationKey: Int = 0,
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

    // Whether tapping this pet would do anything
    val canAllocate = selectedResource != null && canAllocateMore[selectedResource] == true
    val foodValue = scorer.meters[ResourceType.FOOD] ?: 0
    val isHungry = foodValue < 0
    var isHungryJumpActive by remember { mutableStateOf(false) }
    var isCelebrationJumpActive by remember { mutableStateOf(false) }
    val allocationTotal = currentAllocation.values.sum()
    var previousAllocationTotal by remember(scorer.name) { mutableStateOf(allocationTotal) }
    var isFedBounceActive by remember { mutableStateOf(false) }
    val petScale by animateFloatAsState(
        targetValue = if (isFedBounceActive) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "petFedBounceScale"
    )
    val hungryJumpY by animateFloatAsState(
        targetValue = if (isHungryJumpActive) -12f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "petHungryJump"
    )
    val celebrationJumpY by animateFloatAsState(
        targetValue = if (isCelebrationJumpActive) -18f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "petLevelCelebrationJump"
    )

    LaunchedEffect(isHungry) {
        isHungryJumpActive = false
        while (isHungry) {
            delay(2_400)
            isHungryJumpActive = true
            delay(180)
            isHungryJumpActive = false
        }
    }

    LaunchedEffect(allocationTotal) {
        if (allocationTotal > previousAllocationTotal) {
            isFedBounceActive = true
            delay(180)
            isFedBounceActive = false
        }
        previousAllocationTotal = allocationTotal
    }

    LaunchedEffect(levelCelebrationKey) {
        if (levelCelebrationKey > 0) {
            isCelebrationJumpActive = true
            delay(220)
            isCelebrationJumpActive = false
        }
    }

    Box(
        modifier = modifier.clickable(
            enabled = canAllocate,
            onClick = {
                if (selectedResource != null) {
                    val currentAmount = currentAllocation[selectedResource] ?: 0
                    onAllocate(currentAllocation + (selectedResource to currentAmount + 1))
                }
            }
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                // Pet name (above image)
                // Pet image with reset button to its right
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = scorer.name,
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal=4.dp)
                    )

                }
                Image(
                    painter = painterResource(id = petImageRes),
                    contentDescription = scorer.name,
                    modifier = Modifier
                        .size(96.dp)
                        .graphicsLayer {
                            scaleX = petScale
                            scaleY = petScale
                            translationY = hungryJumpY + celebrationJumpY
                        }
                )

            Row(
                modifier = Modifier
                    .offset(y = (-4).dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PetMeterIndicator(
                    label = "Comida",
                    value = scorer.meters[ResourceType.FOOD] ?: 0,
                    allocation = currentAllocation[ResourceType.FOOD] ?: 0,
                    minLimit = scorer.type.meterLimits[ResourceType.FOOD]?.first ?: 0,
                    maxLimit = scorer.type.meterLimits[ResourceType.FOOD]?.second ?: 0
                )
                PetMeterIndicator(
                    label = "Agua",
                    value = scorer.meters[ResourceType.WATER] ?: 0,
                    allocation = currentAllocation[ResourceType.WATER] ?: 0,
                    minLimit = scorer.type.meterLimits[ResourceType.WATER]?.first ?: 0,
                    maxLimit = scorer.type.meterLimits[ResourceType.WATER]?.second ?: 0
                )
            }
        }
    }
}

@Composable
private fun PetMeterIndicator(
    label: String,
    value: Int,
    allocation: Int,
    minLimit: Int,
    maxLimit: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            color = Color.Black,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        HorizontalMeterBar(
            value = value,
            allocation = allocation,
            minLimit = minLimit,
            maxLimit = maxLimit,
            modifier = Modifier
                .width(58.dp)
                .height(12.dp)
        )
    }
}

// ─── Vertical Meter Bar ────────────────────────────────────────────────

/**
 * A vertical segmented meter bar.
 * Fills from bottom to top. Segments in the negative zone (≤ 0) are colored
 * orange-red when filled; segments in the positive zone (> 0) are colored
 * green when filled. Allocation preview segments are light blue.
 * Unfilled segments are semi-transparent.
 *
 * Three visual zones:
 * - **Filled** (segment ≤ value): orange-red for ≤ 0, green for > 0
 * - **Allocation preview** (value < segment ≤ value + allocation): light blue
 * - **Empty** (segment > value + allocation): semi-transparent
 */
@Composable
private fun HorizontalMeterBar(
    value: Int,
    allocation: Int,
    minLimit: Int,
    maxLimit: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0x30FFFFFF)),
        horizontalArrangement = Arrangement.Start
    ) {
        for (segment in (minLimit + 1)..maxLimit) {
            val isFilled = segment <= value
            val isAllocationPreview = !isFilled && segment <= value + allocation

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(1.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            isFilled && segment <= 0 -> Color(0xFFFF7043)
                            isFilled -> Color(0xFF66BB6A)
                            isAllocationPreview -> Color(0xFF4FC3F7)
                            else -> Color(0x40FFFFFF)
                        }
                    )
            )
        }
    }
}

@Composable
fun VerticalMeterBar(
    value: Int,
    allocation: Int,
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
            val isAllocationPreview = !isFilled && segment <= value + allocation

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(1.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            isFilled && segment <= 0 -> Color(0xFFFF7043) // danger zone: orange-red
                            isFilled -> Color(0xFF66BB6A)                 // good zone: green
                            isAllocationPreview -> Color(0xFF4FC3F7)     // allocation preview: light blue
                            else -> Color(0x40FFFFFF)                    // empty: semi-transparent
                        }
                    )
            )
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────

@Composable
private fun SimpleLevelUpMessage(
    level: Int,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember(level) { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.82f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "simpleLevelScale"
    )

    LaunchedEffect(level) {
        visible = true
        delay(1_500)
        visible = false
        delay(180)
        onDone()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(180)),
        exit = fadeOut(animationSpec = tween(180)),
        modifier = modifier
    ) {
        Text(
            text = "\u00A1Nivel $level!",
            color = Color(0xFFFFF8E1),
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xCC3E2723))
                .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                .padding(horizontal = 28.dp, vertical = 14.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )
    }
}

@Composable
private fun PowerupLockedMessage(
    visible: Boolean,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(2_200)
            onDone()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(160)),
        exit = fadeOut(animationSpec = tween(180)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xEE3E2723))
                .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(14.dp))
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Los Power Ups se desbloquean a partir del nivel 2.",
                color = Color(0xFFFFF8E1),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Sigue cuidando a tu mascota para subir de nivel.",
                color = Color(0xFFFFE0B2),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(
    name = "Game Screen Landscape",
    showBackground = true,
    widthDp = 640,
    heightDp = 360
)
@Composable
fun GameScreenPreview() {
    val state = RICH_DEBUG_STATE

    // Some pets have allocations to show the light blue preview effect
    val allocationMap = listOf(
        mapOf(ResourceType.FOOD to 1),                           // Firulais: +1 food
        emptyMap<ResourceType, Int>(),                            // Michi: nothing
        mapOf(ResourceType.WATER to 1),                           // Rex: +1 water
        emptyMap<ResourceType, Int>(),                            // Pelusa: given away
        mapOf(ResourceType.FOOD to 2, ResourceType.WATER to 1),  // Luna: +2 food, +1 water
        emptyMap<ResourceType, Int>(),                            // Bigotes: nothing
    )

    val totalAllocated = allocationMap.fold(emptyMap<ResourceType, Int>()) { acc, map ->
        acc + map.mapValues { (key, value) -> (acc[key] ?: 0) + value }
    }

    // Map active pet names to display positions
    val petPositionMap = state.scorers
        .filter { !it.givenAway }
        .mapIndexed { idx, scorer -> scorer.name to idx }
        .toMap()

    AngelOrphanageTheme {
        GameScreenContent(
            gameState = state,
            allocationMap = allocationMap,
            totalAllocated = totalAllocated,
            petPositionMap = petPositionMap,
            selectedResource = ResourceType.FOOD,
            onAllocate = { _, _ -> },
            onUndoLastAllocation = {},
            canUndoAllocation = true,
            onRunTurn = {},
            onSelectResource = {},
            onOpenPowerupStore = {}
        )
    }
}


