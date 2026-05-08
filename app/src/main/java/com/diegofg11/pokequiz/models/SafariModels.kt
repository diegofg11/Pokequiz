package com.diegofg11.pokequiz.models

import androidx.compose.ui.graphics.Color
import com.diegofg11.pokequiz.R

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

enum class PokeType(val color: Color, val nombreEs: String, val stringResId: Int) {
    FIRE(Color(0xFFEE8130), "FUEGO", R.string.type_fire),
    WATER(Color(0xFF6390F0), "AGUA", R.string.type_water),
    GRASS(Color(0xFF7AC74C), "PLANTA", R.string.type_grass),
    ELECTRIC(Color(0xFFF7D02C), "ELÉCTRICO", R.string.type_electric),
    GROUND(Color(0xFFE2BF65), "TIERRA", R.string.type_ground),
    FLYING(Color(0xFFA98FF3), "VOLADOR", R.string.type_flying),
    ICE(Color(0xFF96D9D6), "HIELO", R.string.type_ice),
    FIGHTING(Color(0xFFC22E28), "LUCHA", R.string.type_fighting),
    PSYCHIC(Color(0xFFF95587), "PSÍQUICO", R.string.type_psychic),
    BUG(Color(0xFFA6B91A), "BICHO", R.string.type_bug),
    POISON(Color(0xFFA33EA1), "VENENO", R.string.type_poison),
    GHOST(Color(0xFF735797), "FANTASMA", R.string.type_ghost),
    DRAGON(Color(0xFF6F35FC), "DRAGÓN", R.string.type_dragon),
    STEEL(Color(0xFFB7B7CE), "ACERO", R.string.type_steel),
    DARK(Color(0xFF705746), "SINIESTRO", R.string.type_dark),
    NORMAL(Color(0xFFA8A77A), "NORMAL", R.string.type_normal),
    ROCK(Color(0xFFB6A136), "ROCA", R.string.type_rock),
    FAIRY(Color(0xFFD685AD), "HADA", R.string.type_fairy);

    fun getAccessibleColor(): Color {
        return com.diegofg11.pokequiz.utils.AccessibilityManager.applyColorBlindFilter(this.color)
    }

    companion object {
        fun getColorByString(typeName: String): Color {
            val type = entries.find { 
                it.name.equals(typeName, ignoreCase = true) || 
                it.nombreEs.equals(typeName, ignoreCase = true) 
            }
            return type?.getAccessibleColor() ?: Color.Gray
        }

        fun getByString(typeName: String): PokeType? {
            return entries.find { 
                it.name.equals(typeName, ignoreCase = true) || 
                it.nombreEs.equals(typeName, ignoreCase = true) 
            }
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
