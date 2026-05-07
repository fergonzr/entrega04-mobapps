package com.example.angelorphanage.domain

const val maxGameLevel = 6

fun newpets_per_level(level: Int): Int =
    when (level) {
        1 -> 3
        in 2..4 -> level
        else -> level - 1
    }

fun givenaway_pet_level_target(level: Int): Int =
    level * (level + 1)/2
