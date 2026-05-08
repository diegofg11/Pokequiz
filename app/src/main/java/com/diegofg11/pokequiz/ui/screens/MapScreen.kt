package com.diegofg11.pokequiz.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegofg11.pokequiz.R
import com.diegofg11.pokequiz.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import com.diegofg11.pokequiz.utils.WallpaperManager
import com.diegofg11.pokequiz.ui.components.*
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.models.User
import com.diegofg11.pokequiz.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun MapScreen(
    completedLevel: Int,
    onNavigateToBattle: (Int) -> Unit
) {
    val context = LocalContext.current
    var user by remember { mutableStateOf<User?>(null) }
    val totalLevels = 20 
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Cargar datos del usuario para el HUD
    LaunchedEffect(Unit) {
        try {
            val userId = SessionManager.currentUserId
            val response = withContext(Dispatchers.IO) { Network.api.getUser(userId) }
            if (response.isSuccessful) {
                user = response.body()
            }
        } catch (e: Exception) {
            // Error silencioso en el mapa
        }
    }

    // Scroll automático al nivel actual
    LaunchedEffect(completedLevel) {
        val targetLevel = (completedLevel + 1).coerceAtMost(totalLevels)
        val indexToScroll = (totalLevels - targetLevel).coerceAtLeast(0)
        listState.animateScrollToItem(indexToScroll)
    }

    var showHelp by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Fondo dinámico
        Image(
            painter = painterResource(id = WallpaperManager.getSelectedWallpaperRes(context)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.7f
        )

        // Overlay de rejilla retro
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 8.dp.toPx()
            for (y in 0..size.height.toInt() step step.toInt()) {
                drawLine(
                    color = Color.Black.copy(alpha = 0.15f),
                    start = Offset(0f, y.toFloat()),
                    end = Offset(size.width, y.toFloat()),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // Capa de contraste
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)))

        // Cabecera Estándar (Sin botón de atrás por ser pantalla principal)
        RetroHeader(
            title = "RUTA ${user?.nivelProgreso ?: completedLevel + 1}",
            onHelpClick = { showHelp = true }
        )

        // Lista de niveles
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 100.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val levelRange = (1..totalLevels).reversed().toList()
            
            itemsIndexed(levelRange) { _, levelId ->
                val isUnlocked = levelId <= completedLevel + 1
                val isCompleted = levelId <= completedLevel
                val isLeft = levelId % 2 != 0 

                LevelItem(
                    levelId = levelId,
                    isUnlocked = isUnlocked,
                    isCompleted = isCompleted,
                    isLeft = isLeft,
                    onClick = { onNavigateToBattle(levelId) }
                )
                
                if (levelId > 1) {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        // --- HUD DE UBICACIÓN (ARRIBA) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .offset(y = (-8).dp),
            contentAlignment = Alignment.TopCenter
        ) {
            RetroMenuBox(
                modifier = Modifier.width(220.dp),
                backgroundColor = Color.White,
                borderColor = Color(0xFF2D5A27)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    RetroText(
                        text = "RUTA POKÉQUIZ", 
                        fontSize = 16.sp, 
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "EXPLORANDO REGIÓN", 
                        fontSize = 9.sp, 
                        color = Color.Gray, 
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            RetroMenuBox(
                modifier = Modifier.clickable {
                    scope.launch {
                        val targetLevel = (completedLevel + 1).coerceAtMost(totalLevels)
                        val indexToScroll = (totalLevels - targetLevel).coerceAtLeast(0)
                        listState.animateScrollToItem(indexToScroll)
                    }
                },
                backgroundColor = Color(0xFFF8F8D8),
                borderColor = GoldPoke
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column {
                        RetroText(text = "NIVEL ${user?.nivelProgreso ?: completedLevel + 1}", fontSize = 12.sp, showShadow = false)
                        RetroText(text = "🪙 ${user?.monedasGacha ?: 0}", fontSize = 11.sp, showShadow = false)
                    }
                }
            }
        }
    }

    if (showHelp) {
        PokemonHelpDialog(
            title = "EL MAPA",
            onDismiss = { showHelp = false }
        ) {
            Column {
                HelpSection("AVENTURA", "Este es tu viaje Pokémon. Recorre las rutas y derrota a los entrenadores para avanzar.")
                HelpSection("NIVELES", "Cada punto en el mapa es una batalla. Los puntos verdes son niveles superados, los amarillos son los que puedes jugar ahora.")
                HelpSection("DIFICULTAD", "A medida que avanzas por las rutas, los Pokémon serán más fuertes y los retos más difíciles.")
                HelpSection("PROGRESO", "Supera niveles para desbloquear nuevas zonas y obtener mejores recompensas.")
            }
        }
    }
}

@Composable
fun LevelItem(
    levelId: Int,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    isLeft: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        contentAlignment = if (isLeft) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        RetroMenuBox(
            modifier = Modifier
                .width(150.dp)
                .clickable(enabled = isUnlocked, onClick = onClick),
            backgroundColor = when {
                isCompleted -> Color(0xFFE8F5E9)
                isUnlocked -> Color.White
                else -> Color(0xFF333333).copy(alpha = 0.9f) // Gris casi negro muy sólido
            },
            borderColor = when {
                isCompleted -> Color(0xFF4CAF50)
                isUnlocked -> Color.Black
                else -> Color.Black.copy(alpha = 0.3f)
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                // Icono Retro
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(if (isUnlocked) Color.White else Color.Transparent, CircleShape)
                        .border(1.dp, if (isUnlocked) Color.Black else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Text("🏅", fontSize = 16.sp) // Medalla
                    } else if (isUnlocked) {
                        Text("🔴", fontSize = 16.sp) // Pokéball simplificada
                    } else {
                        Text("🔒", fontSize = 14.sp) // Candado
                    }
                }

                Column {
                    RetroText(
                        text = "NIVEL $levelId",
                        fontSize = 12.sp,
                        color = if (isUnlocked) Color.Black else Color.White,
                        showShadow = false
                    )
                    if (isCompleted) {
                        Text(
                            text = "COMPLETO", 
                            fontSize = 8.sp, 
                            color = Color(0xFF4CAF50),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (!isUnlocked) {
                        Text(
                            text = "BLOQUEADO", 
                            fontSize = 9.sp, 
                            color = Color.White.copy(alpha = 0.8f),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
