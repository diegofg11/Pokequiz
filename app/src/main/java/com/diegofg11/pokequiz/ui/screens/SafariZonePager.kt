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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegofg11.pokequiz.R
import com.diegofg11.pokequiz.ui.components.*
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource

private const val PAGE_COUNT = 5

@Composable
fun SafariZonePager(
    initialPage: Int = 0,
    onNavigateBack: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialPage) { PAGE_COUNT }
    val scope = rememberCoroutineScope()
    
    // Estado para saber si mostrar las flechas (solo en selección de modo)
    var showNavigation by remember { mutableStateOf(true) }
    var showHelp by remember { mutableStateOf(false) }

    RetroBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- BARRA SUPERIOR INTEGRADA ---
                AnimatedVisibility(
                    visible = showNavigation,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    SafariRetroHeader(
                        title = stringResource(R.string.safari_zone),
                        onBackClick = onNavigateBack,
                        onHelpClick = { showHelp = true }
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                    userScrollEnabled = showNavigation,
                    beyondViewportPageCount = 1
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (page) {
                            0 -> GuessPokemonScreen(onNavigateBack = onNavigateBack, onStateChange = { showNavigation = it })
                            1 -> MemoryGameScreen(onNavigateBack = onNavigateBack, onStateChange = { showNavigation = it })
                            2 -> WordSearchScreen(onNavigateBack = onNavigateBack, onStateChange = { showNavigation = it })
                            3 -> QuickBattleScreen(onNavigateBack = onNavigateBack, onStateChange = { showNavigation = it })
                            4 -> PokeDojoScreen(onNavigateBack = onNavigateBack, onStateChange = { showNavigation = it })
                        }
                    }
                }

                // Capa de navegación inferior integrada en el flujo
                AnimatedVisibility(
                    visible = showNavigation,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp, top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Indicadores de página (Pokeballs)
                        PokeballPageIndicator(
                            pagerState = pagerState.currentPage,
                            pageCount = PAGE_COUNT
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botones de navegación cuadrados
                        Row(
                            modifier = Modifier.fillMaxWidth(0.5f),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NavigationArrow(
                                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                modifier = Modifier.alpha(if (pagerState.currentPage > 0) 1f else 0.3f),
                                onClick = { 
                                    if (pagerState.currentPage > 0) {
                                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } 
                                    }
                                }
                            )

                            NavigationArrow(
                                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                modifier = Modifier.alpha(if (pagerState.currentPage < PAGE_COUNT - 1) 1f else 0.3f),
                                onClick = { 
                                    if (pagerState.currentPage < PAGE_COUNT - 1) {
                                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } 
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- DIÁLOGO DE AYUDA GLOBAL ---
        if (showHelp) {
            val title = when(pagerState.currentPage) {
                0 -> stringResource(R.string.guess_help_title)
                1 -> stringResource(R.string.memory_help_title)
                2 -> stringResource(R.string.wordsearch_help_title)
                3 -> stringResource(R.string.quickbattle_help_title)
                else -> stringResource(R.string.dojo_help_title)
            }

            PokemonHelpDialog(
                title = title,
                onDismiss = { showHelp = false }
            ) {
                when(pagerState.currentPage) {
                    0 -> Column {
                        HelpSection(stringResource(R.string.guess_help_easy), stringResource(R.string.guess_help_easy_desc))
                        HelpSection(stringResource(R.string.guess_help_hard), stringResource(R.string.guess_help_hard_desc))
                        HelpSection(stringResource(R.string.guess_help_infernal), stringResource(R.string.guess_help_infernal_desc))
                    }
                    1 -> Column {
                        HelpSection(stringResource(R.string.memory_help_normal), stringResource(R.string.memory_help_normal_desc))
                        HelpSection(stringResource(R.string.guess_help_infernal), stringResource(R.string.memory_help_infernal_desc))
                    }
                    2 -> Column {
                        HelpSection(stringResource(R.string.memory_help_normal), stringResource(R.string.wordsearch_help_normal_desc))
                        HelpSection(stringResource(R.string.guess_help_hard), stringResource(R.string.wordsearch_help_hard_desc))
                        HelpSection(stringResource(R.string.guess_help_infernal), stringResource(R.string.wordsearch_help_infernal_desc))
                    }
                    3 -> Column {
                        HelpSection(stringResource(R.string.quickbattle_help_rules), stringResource(R.string.quickbattle_help_rules_desc))
                        HelpSection(stringResource(R.string.quickbattle_help_inverse), stringResource(R.string.quickbattle_help_inverse_desc))
                    }
                    4 -> Column {
                        HelpSection(stringResource(R.string.dojo_help_objective), stringResource(R.string.dojo_help_objective_desc))
                        HelpSection(stringResource(R.string.dojo_help_danger), stringResource(R.string.dojo_help_danger_desc))
                        HelpSection(stringResource(R.string.guess_help_infernal), stringResource(R.string.dojo_help_infernal_desc))
                    }
                }
            }
        }
    }
}

