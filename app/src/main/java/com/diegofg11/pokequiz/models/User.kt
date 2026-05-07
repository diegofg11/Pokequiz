package com.diegofg11.pokequiz.models

data class User(
    val id: Int,
    val nombre: String,
    val nivelProgreso: Int,
    val monedasGacha: Int,
    val starterId: Int? = null,
    val avatarUrl: String? = "",
    val wallpaperId: String? = "default",
    val token: String? = null
)
