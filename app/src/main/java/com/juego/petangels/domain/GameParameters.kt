package com.juego.petangels.domain

interface GameParameters {
    fun getComfort(): Int
    fun getVisibility(): Int
    fun getFoodMultiplier(): Int
    fun getWaterMultiplier(): Int
}
