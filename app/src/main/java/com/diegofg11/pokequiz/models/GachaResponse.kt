package com.diegofg11.pokequiz.models

data class GachaResponse(
    val success: Boolean,
    val pulled: Pokemon?,
    val user: User
)
