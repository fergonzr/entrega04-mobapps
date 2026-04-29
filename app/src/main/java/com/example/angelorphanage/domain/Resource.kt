package com.example.angelorphanage.domain

interface Resource {
    fun generate(parameters: GameParameters): Int
}