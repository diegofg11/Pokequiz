package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (showNavigation) 44.dp else 0.dp), // Margen para las flechas
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

                // Indicadores de página (Dots)
                Row(
                    Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) { iteration ->
                        val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.3f)
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .background(color, CircleShape)
                                .size(8.dp)
                        )
                    }
                }
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
        modifier = modifier.size(40.dp), // Reducido de 48dp a 40dp
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.15f), // Más sutil
        contentColor = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
