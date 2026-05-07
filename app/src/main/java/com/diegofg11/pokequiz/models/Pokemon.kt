package com.diegofg11.pokequiz.models

data class Pokemon(
    val idPokedex: Int,
    val nombre: String,
    val tipos: List<String>,
    val hpBase: Int,
    val spriteFront: String,
    val spriteBack: String,
    val spriteIcon: String,
    // Database features
    val inventoryId: Int? = null,
    var inParty: Boolean = false,
    val level: Int = 1,
    val exp: Int = 0,
    val pokedexDescription: String? = "",
    var isFavorite: Boolean = false,
    var isShiny: Boolean = false
)
