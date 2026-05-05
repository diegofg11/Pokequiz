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

private val OPPONENTS_POOL = listOf(
    QuickBattleOpponent("CHARIZARD", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/6.png", listOf(PokeType.WATER, PokeType.GROUND), listOf(PokeType.GRASS, PokeType.FIRE)),
    QuickBattleOpponent("BLASTOISE", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/9.png", listOf(PokeType.ELECTRIC, PokeType.GRASS), listOf(PokeType.FIRE, PokeType.WATER)),
    QuickBattleOpponent("VENUSAUR", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/3.png", listOf(PokeType.FIRE, PokeType.FLYING, PokeType.ICE), listOf(PokeType.WATER, PokeType.ELECTRIC)),
    QuickBattleOpponent("GYARADOS", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/130.png", listOf(PokeType.ELECTRIC, PokeType.ROCK_POKE), listOf(PokeType.FIGHTING, PokeType.WATER)),
    QuickBattleOpponent("DRAGONITE", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/149.png", listOf(PokeType.ICE, PokeType.ROCK_POKE), listOf(PokeType.FIRE, PokeType.WATER, PokeType.GRASS, PokeType.ELECTRIC))
)

@Composable
fun QuickBattleScreen(
    onNavigateBack: () -> Unit,
    onStateChange: (Boolean) -> Unit = {}
) {
    var gameState by remember { mutableStateOf(SafariGameState.START) }
    var currentOpponent by remember { mutableStateOf<QuickBattleOpponent?>(null) }
    var roundCount by remember { mutableIntStateOf(0) }
    var victories by remember { mutableIntStateOf(0) }
    var globalError by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()

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
                    title = "BATALLA RÁPIDA",
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
                                "COMBATIR", 
                                "3 Rondas | 1 Intento", 
                                "-20", 
                                "150", 
                                Color(0xFFE53935), 
                                {
                                    SafariUtils.rewardUser(
                                        scope = scope,
                                        coins = -20,
                                        onSuccess = {
                                            victories = 0
                                            roundCount = 0
                                            currentOpponent = OPPONENTS_POOL.random()
                                            gameState = SafariGameState.PLAYING
                                        },
                                        onError = { globalError = it }
                                    )
                                },
                                span = 2
                            )
                        )
                    )
                    SafariGameState.PLAYING -> QuickBattleGame(
                        opponent = currentOpponent!!,
                        onResult = { isVictory ->
                            if (isVictory) victories++
                            roundCount++
                            if (roundCount >= 3) {
                                gameState = SafariGameState.RESULT
                                val reward = if (victories == 3) 150 else if (victories == 2) 60 else 20
                                SafariUtils.rewardUser(scope = scope, coins = reward)
                            } else {
                                currentOpponent = OPPONENTS_POOL.filter { it != currentOpponent }.random()
                            }
                        }
                    )
                    SafariGameState.RESULT -> QuickBattleResult(
                        victories = victories,
                        onRetry = { gameState = SafariGameState.START },
                        onExit = onNavigateBack
                    )
                }
            }
        }
    }
}

@Composable
fun QuickBattleGame(opponent: QuickBattleOpponent, onResult: (Boolean) -> Unit) {
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
            "¿Qué tipo es súper efectivo?",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val types = remember(opponent) {
            val correct = opponent.weaknesses.random()
            val incorrects = PokeType.entries.filter { it !in opponent.weaknesses }.shuffled().take(3)
            (listOf(correct) + incorrects).shuffled()
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
                                    val isWin = opponent.weaknesses.contains(type)
                                    showEffect = if (isWin) "¡SÚPER EFECTIVO!" else "NO ES MUY EFECTIVO..."
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
                color = if (showEffect?.contains("SÚPER") == true) Color(0xFF4CAF50) else Color(0xFFE53935),
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
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = type.color,
        border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black.copy(alpha = 0.3f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = type.nombreEs,
                color = if (type == PokeType.ELECTRIC) Color.Black else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun QuickBattleResult(victories: Int, onRetry: () -> Unit, onExit: () -> Unit) {
    val reward = if (victories == 3) 150 else if (victories == 2) 60 else 20
    
    SafariResultScreen(
        title = if (victories >= 2) "¡MAESTRO DE TIPOS!" else "SESIÓN FINALIZADA",
        subtitle = "BATALLA RÁPIDA - $victories/3 VICTORIAS",
        description = "Has demostrado tus conocimientos en el campo de batalla.",
        isVictory = victories >= 2,
        coinsEarned = reward,
        onRetry = onRetry,
        onExit = onExit
    )
}
