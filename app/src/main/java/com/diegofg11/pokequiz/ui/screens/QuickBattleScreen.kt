package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.ui.components.*
import com.diegofg11.pokequiz.ui.theme.*
import com.diegofg11.pokequiz.R
import com.diegofg11.pokequiz.utils.SafariGameState
import com.diegofg11.pokequiz.utils.SafariUtils
import com.diegofg11.pokequiz.utils.SoundManager
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    
    var gameState by remember { mutableStateOf(SafariGameState.START) }
    var isInverse by remember { mutableStateOf(false) }
    var currentOpponent by remember { mutableStateOf<QuickBattleOpponent?>(null) }
    var roundCount by remember { mutableIntStateOf(0) }
    var victories by remember { mutableIntStateOf(0) }
    var showResultDialog by remember { mutableStateOf(false) }
    var globalError by remember { mutableStateOf<String?>(null) }
    var showExitWarning by remember { mutableStateOf(false) }
    
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
                            showExitWarning = true
                        } else {
                            gameState = SafariGameState.START
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = if (gameState == SafariGameState.START) 0.dp else 80.dp),
                contentAlignment = Alignment.Center
            ) {
                if (gameState == SafariGameState.PLAYING) {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        // Large Stats Bar outside header
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RetroStatCard(
                                label = "RONDA",
                                value = "${roundCount + 1}/3",
                                containerColor = Color(0xFF673AB7), // Purple
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                            )
                            
                            RetroStatCard(
                                label = "VICTORIAS",
                                value = "$victories",
                                containerColor = Color(0xFFFFA000), // Amber
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                            )
                        }
                        
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            if (isLoading) {
                                CircularProgressIndicator(color = GoldPoke)
                            } else if (currentOpponent != null) {
                                QuickBattleGame(
                                    opponent = currentOpponent!!,
                                    isInverse = isInverse,
                                    onResult = { won ->
                                        if (won) victories += 1
                                        roundCount += 1
                                        if (roundCount >= 3) {
                                            gameState = SafariGameState.RESULT
                                            val rewardBase = if (isInverse) 250 else 150
                                            val reward = if (victories == 3) rewardBase else if (victories == 2) (rewardBase * 0.4).toInt() else 0
                                            SafariUtils.rewardUser(scope = scope, coins = reward, gameType = "quickbattle", difficulty = "default")
                                        } else {
                                            fetchNewOpponent()
                                        }
                                    }
                                )
                            }
                        }
                    }
                } else {
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
                                                gameType = "quickbattle",
                                                difficulty = "default",
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
                                                gameType = "quickbattle",
                                                difficulty = "default",
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
                        SafariGameState.RESULT -> QuickBattleResult(
                            victories = victories,
                            isInverse = isInverse,
                            onRetry = { gameState = SafariGameState.START },
                            onExit = onNavigateBack
                        )
                        else -> {}
                    }
                }
            }
        }

        if (showExitWarning) {
            val penalty = if (isInverse) 40 else 20
            PokemonAlertDialog(
                title = "¡Atención!",
                message = "Si abandonas ahora perderás tu entrada de $penalty monedas. ¿Seguro que quieres salir?",
                isError = true,
                confirmText = "Abandonar",
                onConfirm = {
                    showExitWarning = false
                    onNavigateBack()
                },
                onDismiss = { showExitWarning = false }
            )
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
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.battle_base),
                    contentDescription = null,
                    modifier = Modifier
                        .width(300.dp)
                        .height(150.dp)
                        .align(Alignment.BottomCenter)
                        .offset(x = (-10).dp, y = 30.dp),
                    contentScale = ContentScale.Fit
                )
                AsyncImage(
                    model = opponent.imageUrl,
                    contentDescription = opponent.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
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
                PokeType.values().toList().shuffled().take(4)
            } else {
                val correct = targetList.random()
                val incorrects = PokeType.values().filter { it !in targetList }.shuffled().take(3)
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
    val contentColor = when(type) {
        PokeType.ELECTRIC, PokeType.ICE, PokeType.GROUND, PokeType.STEEL, PokeType.NORMAL -> Color.Black
        else -> Color.White
    }

    RetroButton(
        text = type.nombreEs,
        onClick = onClick,
        modifier = modifier.height(48.dp),
        containerColor = type.color,
        contentColor = contentColor,
        borderColor = Color.Black.copy(alpha = 0.5f),
        fontSize = 11.sp
    )
}

@Composable
fun QuickBattleResult(victories: Int, isInverse: Boolean, onRetry: () -> Unit, onExit: () -> Unit) {
    val rewardBase = if (isInverse) 250 else 150
    val reward = if (victories == 3) rewardBase else if (victories == 2) (rewardBase * 0.4).toInt() else 0
    
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
