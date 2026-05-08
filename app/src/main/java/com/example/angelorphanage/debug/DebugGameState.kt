package com.example.angelorphanage.debug

import com.example.angelorphanage.domain.BaseGameParameters
import com.example.angelorphanage.domain.GameState
import com.example.angelorphanage.domain.Powerup
import com.example.angelorphanage.domain.PowerupType
import com.example.angelorphanage.domain.ResourceType
import com.example.angelorphanage.domain.ScorerInstance
import com.example.angelorphanage.domain.ScorerType
import com.example.angelorphanage.domain.givenaway_pet_level_target
import com.example.angelorphanage.domain.maxGameLevel
import com.example.angelorphanage.domain.newpets_per_level

/**
 * TEMPORARY — Remove this file and all references to it after manual testing is complete.
 *
 * Set to true to inject debug game states into the app.
 * When false, the app behaves normally (loads from repository or starts a fresh game).
 */
const val ENABLE_DEBUG = true

/** Maximum active (non-givenaway) pets the UI can display simultaneously. */
private const val MAX_ACTIVE_PETS = 6

/**
 * Generates a plausible [GameState] for the given [level].
 *
 * The state is constructed to look like a real mid-turn snapshot:
 * - Total pets = cumulative new pets from all levels up to [level],
 *   minus enough given-away pets to have reached [level].
 * - Active pets have varied meter levels (some happy, some sad).
 * - Given-away pets are marked as such with full meters.
 * - Score, turns, and resources scale with level.
 * - Powerups are unlocked from level 2 onward; one comfort powerup is added at level 3+.
 */
fun debugGameStateForLevel(level: Int): GameState {
    // Cumulative pets that have appeared through each level
    val totalPetsEver = (1..level).sumOf { newpets_per_level(it) }
    val givenAwayTarget = givenaway_pet_level_target(level - 1).coerceAtLeast(0)
    val givenAwayCount = givenAwayTarget.coerceAtMost(totalPetsEver)
    // The game never has more than MAX_ACTIVE_PETS active pets at once;
    // cap here so debug states stay within the display limit.
    val activePetCount = (totalPetsEver - givenAwayCount).coerceAtMost(MAX_ACTIVE_PETS)

    // Build the scorers list
    val scorers = mutableListOf<ScorerInstance>()

    // Add given-away pets (full meters, marked as given away)
    for (i in 0 until givenAwayCount) {
        val type = if (i % 3 == 0) ScorerType.CAT else ScorerType.DOG
        scorers.add(
            ScorerInstance(
                name = debugPetName(i, type),
                type = type,
                givenAway = true,
                meters = ResourceType.entries.associateWith { type.meterLimits[it]?.second ?: 0 },
                lastGeneratedScore = type.meterLimits.entries.sumOf { it.value.second * (if (it.key == ResourceType.WATER) 2 else 1) }
            )
        )
    }

    // Add active pets with varied meter levels
    for (i in 0 until activePetCount) {
        val type = if (i % 3 == 0) ScorerType.CAT else ScorerType.DOG
        val meters = debugMeters(type, i)
        scorers.add(
            ScorerInstance(
                name = debugPetName(givenAwayCount + i, type),
                type = type,
                givenAway = false,
                meters = meters,
                lastGeneratedScore = type.score(BaseGameParameters(), meters)
            )
        )
    }

    // Plausible score: roughly 50 per turn × number of turns
    val elapsedTurns = level * 8 + 3
    val score = elapsedTurns * 45 + level * 80
    val powerUpsUnlocked = level >= 2
    val powerups = if (level >= 3) setOf(Powerup(PowerupType.COMFORT)) else setOf()
    val finished = level > maxGameLevel

    return GameState(
        score = score,
        elapsedTurns = elapsedTurns,
        scorers = scorers,
        currentResources = mapOf(
            ResourceType.FOOD to (1 + level),
            ResourceType.WATER to (2 + level)
        ),
        level = level,
        powerups = powerups,
        powerUpsUnlocked = powerUpsUnlocked,
        finished = finished
    )
}

/**
 * A hand-crafted debug state that exercises many UI features:
 * mixed pet types, varied meters, given-away pets, powerups, etc.
 * Level 3, 4 active pets + 1 given away.
 */
val RICH_DEBUG_STATE: GameState = GameState(
    score = 420,
    elapsedTurns = 22,
    level = 3,
    powerups = setOf(Powerup(PowerupType.COMFORT)),
    powerUpsUnlocked = true,
    finished = false,
    scorers = listOf(
        // Happy dog with full food
        ScorerInstance(
            name = "Firulais",
            type = ScorerType.DOG,
            givenAway = false,
            meters = mapOf(ResourceType.FOOD to 3, ResourceType.WATER to 2),
            lastGeneratedScore = 7
        ),
        // Content cat
        ScorerInstance(
            name = "Michi",
            type = ScorerType.CAT,
            givenAway = false,
            meters = mapOf(ResourceType.FOOD to 1, ResourceType.WATER to 2),
            lastGeneratedScore = 5
        ),
        // Sad dog — hungry
        ScorerInstance(
            name = "Rex",
            type = ScorerType.DOG,
            givenAway = false,
            meters = mapOf(ResourceType.FOOD to -1, ResourceType.WATER to 0),
            lastGeneratedScore = -1
        ),
        // Given away cat — adopted!
        ScorerInstance(
            name = "Pelusa",
            type = ScorerType.CAT,
            givenAway = true,
            meters = mapOf(ResourceType.FOOD to 1, ResourceType.WATER to 2),
            lastGeneratedScore = 4
        ),
        // Thirsty dog
        ScorerInstance(
            name = "Luna",
            type = ScorerType.DOG,
            givenAway = false,
            meters = mapOf(ResourceType.FOOD to 0, ResourceType.WATER to -1),
            lastGeneratedScore = -2
        ),
        // Brand new cat (just arrived from level-up)
        ScorerInstance(
            name = "Bigotes",
            type = ScorerType.CAT,
            givenAway = false,
            meters = mapOf(ResourceType.FOOD to 0, ResourceType.WATER to 0),
            lastGeneratedScore = 0
        )
    ),
    currentResources = mapOf(ResourceType.FOOD to 2, ResourceType.WATER to 3)
)

// ─── Helpers ───────────────────────────────────────────────────────────

private val dogNames = listOf("Firulais", "Rex", "Luna", "Max", "Toby", "Bruno", "Rocky", "Duke")
private val catNames = listOf("Michi", "Pelusa", "Bigotes", "Mishi", "Luna", "Mia", "Simba", "Nina")

private fun debugPetName(index: Int, type: ScorerType): String {
    val names = if (type == ScorerType.DOG) dogNames else catNames
    return names[index % names.size]
}

/**
 * Generates varied meter levels for an active pet based on its index.
 * Produces a mix of happy (meters >= 0) and sad (some meters < 0) states.
 */
private fun debugMeters(type: ScorerType, index: Int): Map<ResourceType, Int> {
    val foodRange = type.meterLimits[ResourceType.FOOD] ?: (-1 to 1)
    val waterRange = type.meterLimits[ResourceType.WATER] ?: (-1 to 1)

    // Cycle through different meter states based on index
    val foodValue = when (index % 5) {
        0 -> foodRange.second           // Full food
        1 -> 0                          // Neutral
        2 -> foodRange.first + 1        // One above minimum (slightly hungry)
        3 -> foodRange.second - 1       // One below maximum
        else -> foodRange.first          // At minimum (starving)
    }
    val waterValue = when (index % 4) {
        0 -> waterRange.second           // Full water
        1 -> 0                           // Neutral
        2 -> waterRange.first            // Thirsty
        else -> waterRange.second - 1    // One below maximum
    }

    return mapOf(ResourceType.FOOD to foodValue, ResourceType.WATER to waterValue)
}