package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegofg11.pokequiz.R
import com.diegofg11.pokequiz.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun SafariZonePager(
    initialPage: Int = 0,
    onNavigateBack: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialPage) { 5 }
    val scope = rememberCoroutineScope()
    
    // Estado para saber si mostrar las flechas (solo en selección de modo)
    var showNavigation by remember { mutableStateOf(true) }
    var showHelp by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2D5A27)) // Un verde selva base
    ) {
        // Fondo temático
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.fondo_zona_safari),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            alpha = 0.4f // Sutil para no distraer
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = if (showNavigation) 44.dp else 0.dp),
            userScrollEnabled = showNavigation,
            beyondViewportPageCount = 1
        ) { page ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (page) {
                    0 -> GuessPokemonScreen(
                        onNavigateBack = onNavigateBack,
                        onStateChange = { showNavigation = it }
                    )
                    1 -> MemoryGameScreen(
                        onNavigateBack = onNavigateBack,
                        onStateChange = { showNavigation = it }
                    )
                    2 -> WordSearchScreen(
                        onNavigateBack = onNavigateBack,
                        onStateChange = { showNavigation = it }
                    )
                    3 -> QuickBattleScreen(
                        onNavigateBack = onNavigateBack,
                        onStateChange = { showNavigation = it }
                    )
                    4 -> PokeDojoScreen(
                        onNavigateBack = onNavigateBack,
                        onStateChange = { showNavigation = it }
                    )
                }
            }
        }

        // --- BARRA SUPERIOR GLOBAL ---
        AnimatedVisibility(
            visible = showNavigation,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SafariRetroHeader(
                title = "ZONA SAFARI",
                onBackClick = onNavigateBack,
                onHelpClick = { showHelp = true }
            )
        }

        // --- DIÁLOGO DE AYUDA GLOBAL ---
        if (showHelp) {
            val title = when(pagerState.currentPage) {
                0 -> "¿QUIÉN ES ESE POKÉMON?"
                1 -> "MEMORAMA"
                2 -> "SOPA DE LETRAS"
                3 -> "BATALLA RÁPIDA"
                else -> "POKÉ-DOJO"
            }
            
            com.diegofg11.pokequiz.ui.components.PokemonHelpDialog(
                title = title,
                onDismiss = { showHelp = false }
            ) {
                when(pagerState.currentPage) {
                    0 -> Column {
                        com.diegofg11.pokequiz.ui.components.HelpSection("MODO FÁCIL", "Adivina el Pokémon por su silueta. Sin límite de tiempo.")
                        com.diegofg11.pokequiz.ui.components.HelpSection("MODO DIFÍCIL", "Pokémon rotado aleatoriamente. Tienes 5 segundos.")
                        com.diegofg11.pokequiz.ui.components.HelpSection("MODO INFERNAL", "Siluetas distorsionadas, efectos visuales y solo 4 segundos.")
                    }
                    1 -> Column {
                        com.diegofg11.pokequiz.ui.components.HelpSection("MODO NORMAL", "Encuentra todas las parejas de Pokémon antes de que se acabe el tiempo.")
                        com.diegofg11.pokequiz.ui.components.HelpSection("MODO INFERNAL", "¡Cuidado! Hay bombas ocultas que restan tiempo si las pulsas.")
                    }
                    2 -> Column {
                        com.diegofg11.pokequiz.ui.components.HelpSection("MODO NORMAL", "Busca los nombres de los Pokémon en la cuadrícula.")
                        com.diegofg11.pokequiz.ui.components.HelpSection("MODO DIFÍCIL", "Más palabras y menos tiempo.")
                        com.diegofg11.pokequiz.ui.components.HelpSection("MODO INFERNAL", "Palabras en todas direcciones, incluso invertidas.")
                    }
                    3 -> Column {
                        com.diegofg11.pokequiz.ui.components.HelpSection("REGLAS", "Vence a 3 entrenadores seguidos eligiendo el tipo de ataque correcto.")
                        com.diegofg11.pokequiz.ui.components.HelpSection("MODO INVERSO", "Las debilidades se invierten. ¡Usa ataques que normalmente no serían efectivos!")
                    }
                    4 -> Column {
                        com.diegofg11.pokequiz.ui.components.HelpSection("OBJETIVO", "Golpea a los Diglett que salgan de los agujeros para ganar puntos.")
                        com.diegofg11.pokequiz.ui.components.HelpSection("PELIGRO", "No golpees a los Voltorb o perderás puntos y tiempo.")
                        com.diegofg11.pokequiz.ui.components.HelpSection("MODO INFERNAL", "¡Más velocidad y más Voltorbs explosivos!")
                    }
                }
            }
        }

        // Flechas de navegación (Capa superior)
        AnimatedVisibility(
            visible = showNavigation,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Flecha Izquierda
                if (pagerState.currentPage > 0) {
                    NavigationArrow(
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 4.dp),
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    )
                }

                // Flecha Derecha
                if (pagerState.currentPage < 4) {
                    NavigationArrow(
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp),
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    )
                }

                // Indicadores de página (Pokeballs)
                PokeballPageIndicator(
                    pagerState = pagerState.currentPage,
                    pageCount = 5,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
fun NavigationArrow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(4.dp),
        color = Color.Black.copy(alpha = 0.7f),
        contentColor = Color.White,
        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
