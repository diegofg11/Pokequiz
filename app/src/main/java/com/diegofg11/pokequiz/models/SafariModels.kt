package com.diegofg11.pokequiz.models

import androidx.compose.ui.graphics.Color

/**
 * Common difficulties for minigames.
 */
enum class DifficultyLevel {
    EASY, NORMAL, HARD, INFERNAL
}

data class MinigamePokemon(
    val id: Int,
    val nombre: String
)

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
    FIRE(Color(0xFFEE8130), "FUEGO"),
    WATER(Color(0xFF6390F0), "AGUA"),
    GRASS(Color(0xFF7AC74C), "PLANTA"),
    ELECTRIC(Color(0xFFF7D02C), "ELÉCTRICO"),
    GROUND(Color(0xFFE2BF65), "TIERRA"),
    FLYING(Color(0xFFA98FF3), "VOLADOR"),
    ICE(Color(0xFF96D9D6), "HIELO"),
    FIGHTING(Color(0xFFC22E28), "LUCHA"),
    PSYCHIC(Color(0xFFF95587), "PSÍQUICO"),
    BUG(Color(0xFFA6B91A), "BICHO"),
    POISON(Color(0xFFA33EA1), "VENENO"),
    GHOST(Color(0xFF735797), "FANTASMA"),
    DRAGON(Color(0xFF6F35FC), "DRAGÓN"),
    STEEL(Color(0xFFB7B7CE), "ACERO"),
    DARK(Color(0xFF705746), "SINIESTRO"),
    NORMAL(Color(0xFFA8A77A), "NORMAL"),
    ROCK(Color(0xFFB6A136), "ROCA"),
    FAIRY(Color(0xFFD685AD), "HADA");

    companion object {
        fun getColorByString(typeName: String): Color {
            return entries.find { 
                it.name.equals(typeName, ignoreCase = true) || 
                it.nombreEs.equals(typeName, ignoreCase = true) 
            }?.color ?: Color.Gray
        }
    }
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
