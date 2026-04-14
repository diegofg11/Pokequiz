package com.diegofg11.pokequiz.models

data class Pokemon(
    val idPokedex: Int,
    val nombre: String,
    val tipos: List<String>,
    val hpBase: Int,
    val spriteFront: String,
    val spriteBack: String,
    val spriteIcon: String
)
