package com.example.angelorphanage.domain

class BaseGameParameters: GameParameters {
    override fun getComfort(): Int = 1
    override fun getVisibility(): Int = 0
    override fun getFoodMultiplier(): Int = 1
    override fun getWaterMultiplier(): Int = 2
}
