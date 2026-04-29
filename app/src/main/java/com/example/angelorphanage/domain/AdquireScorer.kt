package com.example.angelorphanage.domain

import kotlin.random.Random

private val scorerWeights = mapOf(
    ScorerType.DOG to 2,
    ScorerType.CAT to 3
)

fun try_adquire(parameters: GameParameters): ScorerInstance? {
    val totalWeights = scorerWeights.values.sum() + (15 - parameters.getVisibility())
    val choice = Random.nextInt(totalWeights)
    var probabilityFloor = 0

    // Check if the roll fell into any scorer type
    for (entry in scorerWeights){
        if (choice >= probabilityFloor && choice <  entry.value + probabilityFloor)
            return ScorerInstance(entry.key)
        probabilityFloor += entry.value
    }

    // Return null if it is not
    return null
}

fun adquire(): ScorerInstance {
    val totalWeights = scorerWeights.values.sum()
    var probabilityFloor = 0
    val choice = Random.nextInt(totalWeights)

    // Check if the roll fell into any scorer type
    for (entry in scorerWeights){
        if (choice >= probabilityFloor && choice <  entry.value + probabilityFloor)
            return ScorerInstance(entry.key)
        probabilityFloor += entry.value
    }
    throw IllegalStateException()
}