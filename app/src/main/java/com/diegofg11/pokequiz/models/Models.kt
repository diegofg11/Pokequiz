package com.diegofg11.pokequiz.models

enum class GachaState {
    IDLE,       // Esperando a que el usuario pulse
    SHAKING,    // La Pokéball tiembla
    OPENING,    // La Pokéball se abre
    REVEALED    // El Pokémon se muestra
}

data class Question(
    val id: Int,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

data class GuessRoundResponse(
    val targetId: Int,
    val options: List<GuessOption>
)

data class GuessOption(
    val id: Int,
    val name: String
)

data class PokemonBattle(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val maxHp: Int,
    val questions: List<Question>
)

data class BattleDamageRequest(
    val playerLevel: Int,
    val enemyLevel: Int,
    val isCorrect: Boolean,
    val enemyHpBase: Int
)

data class BattleDamageResponse(
    val damageDealt: Int,
    val damageReceived: Int
)

data class LevelResponse(
    val levelId: String,
    val enemy: Pokemon?,
    val questions: List<Question>
)

data class RewardRequest(
    val userId: Int,
    val levelId: Int
)

data class SafariRewardRequest(
    val userId: Int,
    val coinsEarned: Int,
    val gameType: String,
    val difficulty: String
)

data class TogglePartyRequest(
    val userId: Int,
    val inventoryId: Int,
    val inParty: Boolean
)

data class UpdateUserRequest(
    val userId: Int,
    val avatarUrl: String?,
    val wallpaperId: String?
)

data class ToggleFavoriteRequest(
    val inventoryId: Int,
    val isFavorite: Boolean
)
