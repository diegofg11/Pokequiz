package com.diegofg11.pokequiz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.diegofg11.pokequiz.ui.components.*
import com.diegofg11.pokequiz.ui.theme.*

@Composable
fun MinigamesScreen(navController: NavController? = null) {
    RetroBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            SafariRetroHeader(
                title = "ZONA SAFARI",
                onBackClick = { navController?.navigateUp() }
            )

            SafariSelectionScreen(
                title = "MINIJUEGOS",
                subtitle = "¡Elige un reto y gana monedas!",
                cards = listOf(
                    DifficultyCardData(
                        "SILUETAS", 
                        "¿Quién es ese Pokémon?", 
                        reward = "40", 
                        rewardLabel = "HASTA", 
                        color = Color(0xFF2196F3), 
                        onClick = { navController?.navigate("safari_zone/0") }
                    ),
                    DifficultyCardData(
                        "MEMORAMA", 
                        "Encuentra las parejas", 
                        reward = "200", 
                        rewardLabel = "HASTA", 
                        color = Color(0xFF4CAF50), 
                        onClick = { navController?.navigate("safari_zone/1") }
                    ),
                    DifficultyCardData(
                        "SOPA POKÉ", 
                        "Busca nombres ocultos", 
                        reward = "75", 
                        rewardLabel = "HASTA", 
                        color = Color(0xFFFF9800), 
                        onClick = { navController?.navigate("safari_zone/2") }
                    ),
                    DifficultyCardData(
                        "BATALLA", 
                        "Maestro de tipos", 
                        reward = "150", 
                        rewardLabel = "HASTA", 
                        color = Color(0xFFE91E63), 
                        onClick = { navController?.navigate("safari_zone/3") }
                    ),
                    DifficultyCardData(
                        "POKÉ-DOJO", 
                        "Golpea al Diglett", 
                        reward = "120", 
                        rewardLabel = "HASTA", 
                        color = Color(0xFF795548), 
                        onClick = { navController?.navigate("safari_zone/4") },
                        span = 2
                    )
                )
            )
        }
    }
}
