package com.diegofg11.pokequiz.models

import androidx.compose.ui.graphics.Color

/**
 * Common difficulties for minigames.
 */
enum class DifficultyLevel {
    EASY, NORMAL, HARD, INFERNAL
}

// --- Guess Pokémon Models ---

enum class GuessDifficulty {
    EASY, HARD, INFERNAL
}

// --- Memory Game Models ---

enum class MemoryDifficulty {
    NORMAL, INFERNAL
}

data class MemoryCardData(
    val id: Int,
    val pokemonId: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

// --- Word Search Models ---

enum class WordSearchDifficulty {
    NORMAL, HARD, INFERNAL
}

// --- Quick Battle Models ---

enum class PokeType(val color: Color, val nombreEs: String) {
    FIRE(Color(0xFFF44336), "FUEGO"),
    WATER(Color(0xFF2196F3), "AGUA"),
    GRASS(Color(0xFF4CAF50), "PLANTA"),
    ELECTRIC(Color(0xFFFFEB3B), "ELÉCTRICO"),
    GROUND(Color(0xFF795548), "TIERRA"),
    FLYING(Color(0xFF9C27B0), "VOLADOR"),
    ICE(Color(0xFF00BCD4), "HIELO"),
    FIGHTING(Color(0xFFFF5722), "LUCHA"),
    PSYCHIC(Color(0xFFE91E63), "PSÍQUICO"),
    BUG(Color(0xFF8BC34A), "BICHO"),
    POISON(Color(0xFF9C27B0), "VENENO"),
    GHOST(Color(0xFF673AB7), "FANTASMA"),
    DRAGON(Color(0xFF3F51B5), "DRAGÓN"),
    STEEL(Color(0xFF607D8B), "ACERO"),
    DARK(Color(0xFF212121), "SINIESTRO"),
    NORMAL(Color(0xFF9E9E9E), "NORMAL"),
    ROCK_POKE(Color(0xFFC0CA33), "ROCA")
}

data class QuickBattleOpponent(
    val name: String,
    val imageUrl: String,
    val weaknesses: List<PokeType>,
    val resistances: List<PokeType>
)

// --- Poké-Dojo Models ---

enum class DojoDifficulty {
    NORMAL, INFERNAL
}

enum class MoleType(val score: Int, val imageUrl: String, val duration: Long) {
    EMPTY(0, "", 0L),
    DIGLETT(10, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/50.png", 1200L),
    DUGTRIO(25, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/51.png", 900L),
    PIKACHU(50, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png", 700L),
    VOLTORB(-20, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/100.png", 1000L)
}

data class HoleState(
    val id: Int,
    var type: MoleType = MoleType.EMPTY,
    var isHit: Boolean = false
)
