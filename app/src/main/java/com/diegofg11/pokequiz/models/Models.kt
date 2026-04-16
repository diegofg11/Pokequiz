package com.diegofg11.pokequiz.models

data class Question(
    val id: Int,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

data class PokemonBattle(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val maxHp: Int,
    val questions: List<Question>
)

data class LevelResponse(
    val levelId: String,
    val enemy: Pokemon?,
    val questions: List<Question>
)

data class RewardRequest(
    val userId: Int,
    val levelId: Int,
    val coinsEarned: Int
)

data class TogglePartyRequest(
    val userId: Int,
    val inventoryId: Int,
    val inParty: Boolean
)
