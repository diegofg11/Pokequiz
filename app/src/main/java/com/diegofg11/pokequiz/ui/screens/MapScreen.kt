package com.diegofg11.pokequiz.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

@Composable
fun MapScreen(
    completedLevel: Int,
    onNavigateToBattle: (Int) -> Unit
) {
    val context = LocalContext.current
    // --- CONFIGURACIÓN ---
    val totalLevels = 20 
    // ----------------------

    val listState = rememberLazyListState()

    // Scroll automático al nivel actual (o al siguiente por desbloquear)
    LaunchedEffect(completedLevel) {
        // Calculamos la posición. Como la lista está invertida visualmente (el nivel 1 abajo),
        // el índice en la LazyColumn para el nivel actual es (totalLevels - nivel)
        val targetLevel = (completedLevel + 1).coerceAtMost(totalLevels)
        val indexToScroll = (totalLevels - targetLevel).coerceAtLeast(0)
        listState.animateScrollToItem(indexToScroll)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo dinámico (Leído de preferencias)
        Image(
            painter = painterResource(id = WallpaperManager.getSelectedWallpaperRes(context)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Capa de contraste para legibilidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
        )

        // Lista de niveles con Scroll
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 120.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rango de niveles de mayor a menor (para que el 20 esté arriba y el 1 abajo)
            val levelRange = (1..totalLevels).reversed().toList()
            
            itemsIndexed(levelRange) { _, levelId ->
                val isUnlocked = levelId <= completedLevel + 1
                val isCompleted = levelId <= completedLevel
                val isLeft = levelId % 2 != 0 // Zig-zag: impares izquierda, pares derecha

                LevelItem(
                    levelId = levelId,
                    isUnlocked = isUnlocked,
                    isCompleted = isCompleted,
                    isLeft = isLeft,
                    onClick = { onNavigateToBattle(levelId) }
                )
                
                // Espacio vertical entre cada nivel
                if (levelId > 1) {
                    Spacer(modifier = Modifier.height(60.dp))
                }
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
            .padding(horizontal = 50.dp),
        contentAlignment = if (isLeft) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .clickable(
                    enabled = isUnlocked,
                    onClick = onClick
                )
        ) {
            // Etiqueta del nivel
            Text(
                text = "Nivel $levelId",
                color = if (isUnlocked) Color.White else Color.Gray,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Círculo del nivel
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = if (isCompleted) RedPoke else if (isUnlocked) DarkPoke else Color.DarkGray,
                        shape = CircleShape
                    )
                    .border(
                        width = 4.dp,
                        color = if (isUnlocked) Color.White else Color.Gray,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    // Indicador de nivel superado
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color.White, CircleShape)
                    )
                } else if (!isUnlocked) {
                    // Podrías poner un icono de candado aquí si quisieras
                }
            }
        }
    }
}
