package com.diegofg11.pokequiz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.diegofg11.pokequiz.ui.components.*
import com.diegofg11.pokequiz.ui.theme.*

@Composable
fun MinigamesScreen(navController: NavController? = null) {
    var showHelp by remember { mutableStateOf(false) }

    RetroBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            RetroHeader(isSafariStyle = true, 
                title = "ZONA SAFARI",
                onBackClick = { navController?.popBackStack() },
                onHelpClick = { showHelp = true }
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

        if (showHelp) {
            PokemonHelpDialog(
                title = "ZONA SAFARI",
                onDismiss = { showHelp = false }
            ) {
                Column {
                    HelpSection("¿QUÉ ES?", "La Zona Safari es el lugar donde puedes conseguir Monedas Poké gratis jugando a diversos minijuegos.")
                    HelpSection("MONEDAS", "Cada juego tiene un coste de entrada pero ofrece grandes recompensas si logras superar los retos.")
                    HelpSection("DIFICULTAD", "Dentro de cada juego podrás elegir entre varios niveles. ¡A mayor dificultad, mayor será el premio!")
                    HelpSection("USO", "Usa las monedas ganadas en el Bazar Pokémon para completar tu Pokédex.")
                }
            }
        }
    }
}
