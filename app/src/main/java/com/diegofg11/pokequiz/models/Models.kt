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
