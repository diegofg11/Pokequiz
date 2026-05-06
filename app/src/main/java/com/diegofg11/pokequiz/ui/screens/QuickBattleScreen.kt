package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.ui.components.*
import com.diegofg11.pokequiz.ui.theme.*
import com.diegofg11.pokequiz.utils.SafariGameState
import com.diegofg11.pokequiz.utils.SafariUtils
import com.diegofg11.pokequiz.models.PokeType
import com.diegofg11.pokequiz.models.QuickBattleOpponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.diegofg11.pokequiz.api.Network

@Composable
fun QuickBattleScreen(
    onNavigateBack: () -> Unit,
    onStateChange: (Boolean) -> Unit = {}
) {
    var gameState by remember { mutableStateOf(SafariGameState.START) }
    var isInverse by remember { mutableStateOf(false) }
    var currentOpponent by remember { mutableStateOf<QuickBattleOpponent?>(null) }
    var roundCount by remember { mutableIntStateOf(0) }
    var victories by remember { mutableIntStateOf(0) }
    var globalError by remember { mutableStateOf<String?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()

    fun fetchNewOpponent(onSuccess: () -> Unit = {}) {
        scope.launch {
            isLoading = true
            try {
                val response = Network.api.getQuickBattleOpponent()
                if (response.isSuccessful && response.body() != null) {
                    currentOpponent = response.body()
                    onSuccess()
                } else {
                    globalError = "No se pudo obtener un oponente del servidor."
                }
            } catch (e: Exception) {
                globalError = "Error de conexión: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(gameState) {
        onStateChange(gameState == SafariGameState.START)
    }

    if (globalError != null) {
        PokemonAlertDialog(
            title = "Error",
            message = globalError ?: "",
            onDismiss = { globalError = null },
            onConfirm = { globalError = null }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (gameState != SafariGameState.START) {
                SafariRetroHeader(
                    title = if (isInverse) "BATALLA INVERSA" else "BATALLA RÁPIDA",
                    onBackClick = {
                        if (gameState == SafariGameState.PLAYING) {
                            gameState = SafariGameState.RESULT
                        } else {
                            gameState = SafariGameState.START
                        }
                    },
                    extraContent = {
                        if (gameState == SafariGameState.PLAYING) {
                            Box(modifier = Modifier.fillMaxWidth().padding(end = 48.dp), contentAlignment = Alignment.CenterEnd) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("RONDA", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                        Text("${roundCount + 1}/3", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.3f)))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("VICTORIAS", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                        Text("$victories", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (gameState) {
                    SafariGameState.START -> SafariSelectionScreen(
                        title = "BATALLA RÁPIDA",
                        subtitle = "Demuestra tu conocimiento de tipos",
                        cards = listOf(
                            DifficultyCardData(
                                "CLÁSICO", 
                                "Tipos efectivos", 
                                cost = "-20", 
                                reward = "150", 
                                color = Color(0xFFE53935), 
                                onClick = {
                                    if (!isLoading) {
                                        SafariUtils.rewardUser(
                                            scope = scope,
                                            coins = -20,
                                            onSuccess = {
                                                isInverse = false
                                                victories = 0
                                                roundCount = 0
                                                fetchNewOpponent {
                                                    gameState = SafariGameState.PLAYING
                                                }
                                            },
                                            onError = { globalError = it }
                                        )
                                    }
                                }
                            ),
                            DifficultyCardData(
                                "INVERSO", 
                                "Usa resistencias", 
                                cost = "-40", 
                                reward = "250", 
                                color = Color(0xFF9C27B0), 
                                onClick = {
                                    if (!isLoading) {
                                        SafariUtils.rewardUser(
                                            scope = scope,
                                            coins = -40,
                                            onSuccess = {
                                                isInverse = true
                                                victories = 0
                                                roundCount = 0
                                                fetchNewOpponent {
                                                    gameState = SafariGameState.PLAYING
                                                }
                                            },
                                            onError = { globalError = it }
                                        )
                                    }
                                }
                            )
                        )
                    )
                    SafariGameState.PLAYING -> if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GoldPoke)
                        }
                    } else if (currentOpponent != null) {
                        QuickBattleGame(
                            opponent = currentOpponent!!,
                            isInverse = isInverse,
                            onResult = { isVictory ->
                                if (isVictory) victories++
                                roundCount++
                                if (roundCount >= 3) {
                                    gameState = SafariGameState.RESULT
                                    val rewardBase = if (isInverse) 250 else 150
                                    val reward = if (victories == 3) rewardBase else if (victories == 2) (rewardBase * 0.4).toInt() else (rewardBase * 0.1).toInt()
                                    SafariUtils.rewardUser(scope = scope, coins = reward)
                                } else {
                                    fetchNewOpponent()
                                }
                            }
                        )
                    }
                    SafariGameState.RESULT -> QuickBattleResult(
                        victories = victories,
                        isInverse = isInverse,
                        onRetry = { gameState = SafariGameState.START },
                        onExit = onNavigateBack
                    )
                }
            }
        }
    }
}

@Composable
fun QuickBattleGame(opponent: QuickBattleOpponent, isInverse: Boolean, onResult: (Boolean) -> Unit) {
    var selectedType by remember { mutableStateOf<PokeType?>(null) }
    var showEffect by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RetroMenuBox(modifier = Modifier.size(200.dp)) {
            AsyncImage(
                model = opponent.imageUrl,
                contentDescription = opponent.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        
        Text(
            text = opponent.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            if (isInverse) "¿Qué tipo NO es muy efectivo?" else "¿Qué tipo es súper efectivo?",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val types = remember(opponent, isInverse) {
            val targetList = if (isInverse) opponent.resistances else opponent.weaknesses
            if (targetList.isEmpty()) {
                // Fallback de seguridad para evitar crashes si el backend fallara
                PokeType.entries.shuffled().take(4)
            } else {
                val correct = targetList.random()
                val incorrects = PokeType.entries.filter { it !in targetList }.shuffled().take(3)
                (listOf(correct) + incorrects).shuffled()
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            types.chunked(2).forEach { rowTypes ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowTypes.forEach { type ->
                        TypeButton(
                            type = type,
                            onClick = {
                                if (selectedType == null) {
                                    selectedType = type
                                    val targetList = if (isInverse) opponent.resistances else opponent.weaknesses
                                    val isWin = targetList.contains(type)
                                    showEffect = if (isInverse) {
                                        if (isWin) "¡NO ES MUY EFECTIVO!" else "¡ES SÚPER EFECTIVO! (MAL)"
                                    } else {
                                        if (isWin) "¡SÚPER EFECTIVO!" else "NO ES MUY EFECTIVO..."
                                    }
                                    scope.launch {
                                        delay(1500)
                                        onResult(isWin)
                                        selectedType = null
                                        showEffect = null
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = showEffect != null) {
            Text(
                text = showEffect ?: "",
                color = if (showEffect != null && showEffect!!.contains("EFECTIVO") && !showEffect!!.contains("MAL")) Color(0xFF4CAF50) else Color(0xFFE53935),
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}

@Composable
fun TypeButton(type: PokeType, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .border(2.dp, Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = type.color,
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = type.nombreEs.uppercase(),
                color = when(type) {
                    PokeType.ELECTRIC, PokeType.ICE, PokeType.GROUND, PokeType.STEEL, PokeType.NORMAL -> Color.Black
                    else -> Color.White
                },
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun QuickBattleResult(victories: Int, isInverse: Boolean, onRetry: () -> Unit, onExit: () -> Unit) {
    val rewardBase = if (isInverse) 250 else 150
    val reward = if (victories == 3) rewardBase else if (victories == 2) (rewardBase * 0.4).toInt() else (rewardBase * 0.1).toInt()
    
    SafariResultScreen(
        title = if (victories >= 2) "¡MAESTRO DE TIPOS!" else "SESIÓN FINALIZADA",
        subtitle = "BATALLA ${if (isInverse) "INVERSA" else "RÁPIDA"} - $victories/3 VICTORIAS",
        description = if (isInverse) "Has dominado las resistencias Pokémon." else "Has demostrado tus conocimientos en el campo de batalla.",
        isVictory = victories >= 2,
        coinsEarned = reward,
        onRetry = onRetry,
        onExit = onExit
    )
}
