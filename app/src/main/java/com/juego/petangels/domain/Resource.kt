package com.juego.petangels.domain

interface Resource {
    fun generate(parameters: GameParameters): Int
}
