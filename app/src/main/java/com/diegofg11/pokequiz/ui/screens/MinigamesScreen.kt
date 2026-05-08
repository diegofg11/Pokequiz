/**
 * @authors: Gaizka, Diego y Xiker
 * Pantalla de selección de Minijuegos de la Zona Safari.
 * Muestra las opciones disponibles: Siluetas, Memorama, Sopa de Letras, Batalla Rápida y PokeDojo.
 */
package com.diegofg11.pokequiz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.diegofg11.pokequiz.R
import com.diegofg11.pokequiz.ui.components.*
import com.diegofg11.pokequiz.ui.theme.*

@Composable
fun MinigamesScreen(navController: NavController? = null) {
    var showHelp by remember { mutableStateOf(false) }

    RetroBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            RetroHeader(
                title = stringResource(R.string.safari_zone),
                onBackClick = { navController?.popBackStack() },
                isSafariStyle = true,
                onHelpClick = { showHelp = true }
            )

            SafariSelectionScreen(
                title = stringResource(R.string.minigames),
                subtitle = stringResource(R.string.minigames_subtitle),
                cards = listOf(
                    DifficultyCardData(
                        stringResource(R.string.silhouettes), 
                        stringResource(R.string.silhouettes_desc), 
                        reward = "40", 
                        rewardLabel = stringResource(R.string.reward_up_to), 
                        color = Color(0xFF2196F3), 
                        onClick = { navController?.navigate("safari_zone/0") }
                    ),
                    DifficultyCardData(
                        stringResource(R.string.memorama), 
                        stringResource(R.string.memorama_desc), 
                        reward = "200", 
                        rewardLabel = stringResource(R.string.reward_up_to), 
                        color = Color(0xFF4CAF50), 
                        onClick = { navController?.navigate("safari_zone/1") }
                    ),
                    DifficultyCardData(
                        stringResource(R.string.word_soup), 
                        stringResource(R.string.word_soup_desc), 
                        reward = "75", 
                        rewardLabel = stringResource(R.string.reward_up_to), 
                        color = Color(0xFFFF9800), 
                        onClick = { navController?.navigate("safari_zone/2") }
                    ),
                    DifficultyCardData(
                        stringResource(R.string.battle), 
                        stringResource(R.string.battle_desc), 
                        reward = "150", 
                        rewardLabel = stringResource(R.string.reward_up_to), 
                        color = Color(0xFFE91E63), 
                        onClick = { navController?.navigate("safari_zone/3") }
                    ),
                    DifficultyCardData(
                        stringResource(R.string.poke_dojo), 
                        stringResource(R.string.poke_dojo_desc), 
                        reward = "120", 
                        rewardLabel = stringResource(R.string.reward_up_to), 
                        color = Color(0xFF795548), 
                        onClick = { navController?.navigate("safari_zone/4") },
                        span = 2
                    )
                )
            )
        }

        if (showHelp) {
            PokemonHelpDialog(
                title = stringResource(R.string.safari_zone),
                onDismiss = { showHelp = false }
            ) {
                Column {
                    HelpSection(stringResource(R.string.safari_help_what), stringResource(R.string.safari_help_what_desc))
                    HelpSection(stringResource(R.string.safari_help_coins), stringResource(R.string.safari_help_coins_desc))
                    HelpSection(stringResource(R.string.safari_help_difficulty), stringResource(R.string.safari_help_difficulty_desc))
                    HelpSection(stringResource(R.string.safari_help_use), stringResource(R.string.safari_help_use_desc))
                }
            }
        }
    }
}
