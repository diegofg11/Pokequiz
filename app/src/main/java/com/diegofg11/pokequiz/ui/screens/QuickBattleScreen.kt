package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.models.RewardRequest
import com.diegofg11.pokequiz.ui.components.*
import com.diegofg11.pokequiz.ui.theme.*
import com.diegofg11.pokequiz.utils.SessionManager
import com.diegofg11.pokequiz.utils.SafariGameState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

// --- Modelos de Datos ---

enum class PokeType(val color: Color, val nombreEs: String) {
    FIRE(Color(0xFFF44336), "FUEGO"),
    WATER(Color(0xFF2196F3), "AGUA"),
    GRASS(Color(0xFF4CAF50), "PLANTA"),
    ELECTRIC(Color(0xFFFFEB3B), "ELÉCTRICO"),
    GROUND(Color(0xFF795548), "TIERRA"),
    FLYING(Color(0xFF9C27B0), "VOLADOR"),
    ICE(Color(0xFF00BCD4), "HIELO"),
    FIGHTING(Color(0xFFFF5722), "LUCHA"),
    PSYCHIC(Color(0xFFE91E63), "PSÍQUICO")
}

data class QuickBattleOpponent(
    val name: String,
    val imageUrl: String,
    val weaknesses: List<PokeType>,
    val resistances: List<PokeType>
)

private val OPPONENTS_POOL = listOf(
    QuickBattleOpponent("CHARIZARD", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/6.png", listOf(PokeType.WATER, PokeType.ELECTRIC), listOf(PokeType.FIRE, PokeType.GRASS, PokeType.FIGHTING)),
    QuickBattleOpponent("BLASTOISE", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/9.png", listOf(PokeType.GRASS, PokeType.ELECTRIC), listOf(PokeType.FIRE, PokeType.WATER, PokeType.ICE)),
    QuickBattleOpponent("VENUSAUR", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/3.png", listOf(PokeType.FIRE, PokeType.ICE, PokeType.FLYING, PokeType.PSYCHIC), listOf(PokeType.WATER, PokeType.GRASS, PokeType.ELECTRIC, PokeType.FIGHTING)),
    QuickBattleOpponent("PIKACHU", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png", listOf(PokeType.GROUND), listOf(PokeType.ELECTRIC, PokeType.FLYING)),
    QuickBattleOpponent("GYARADOS", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/130.png", listOf(PokeType.ELECTRIC), listOf(PokeType.FIRE, PokeType.WATER, PokeType.FIGHTING)),
    QuickBattleOpponent("ONIX", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/95.png", listOf(PokeType.WATER, PokeType.GRASS, PokeType.ICE, PokeType.FIGHTING, PokeType.GROUND), listOf(PokeType.FIRE, PokeType.ELECTRIC, PokeType.FLYING)),
    QuickBattleOpponent("DRAGONITE", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/149.png", listOf(PokeType.ICE), listOf(PokeType.FIRE, PokeType.WATER, PokeType.GRASS, PokeType.FIGHTING)),
    QuickBattleOpponent("ALAKAZAM", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/65.png", listOf(PokeType.PSYCHIC), listOf(PokeType.FIGHTING, PokeType.PSYCHIC)),
    QuickBattleOpponent("MACHAMP", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/68.png", listOf(PokeType.FLYING, PokeType.PSYCHIC), listOf(PokeType.FIGHTING)),
    QuickBattleOpponent("LAPRAS", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/131.png", listOf(PokeType.ELECTRIC, PokeType.GRASS, PokeType.FIGHTING), listOf(PokeType.WATER, PokeType.ICE)),
    QuickBattleOpponent("ARCANINE", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/59.png", listOf(PokeType.WATER, PokeType.GROUND), listOf(PokeType.FIRE, PokeType.GRASS, PokeType.ICE)),
    QuickBattleOpponent("JOLTEON", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/135.png", listOf(PokeType.GROUND), listOf(PokeType.ELECTRIC, PokeType.FLYING)),
    QuickBattleOpponent("VAPOREON", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/134.png", listOf(PokeType.GRASS, PokeType.ELECTRIC), listOf(PokeType.FIRE, PokeType.WATER, PokeType.ICE)),
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
                    onClick = { onStart(false) },
                    modifier = Modifier.weight(1f)
                )

                // Tarjeta Modo Inverso
                RetroDifficultyCard(
                    title = "INVERSO",
                    subtitle = "Usa resistencias",
                    cost = "-50",
                    reward = "+200",
                    color = Color(0xFF9C27B0),
                    onClick = { onStart(true) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun QuickBattleGame(round: Int, isInverse: Boolean, onRoundWin: () -> Unit, onGameOver: () -> Unit) {
    val opponent = remember(round) { OPPONENTS_POOL.random() }
    var isAnswered by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableLongStateOf(3000L) }
    
    // Generar 4 tipos
    val typeButtons = remember(round, isInverse) {
        val targetList = if (isInverse) opponent.resistances else opponent.weaknesses
        val correct = if (targetList.isNotEmpty()) targetList.random() else PokeType.entries.random()
        val incorrects = PokeType.entries.filter { it !in targetList }.shuffled().take(3)
        (listOf(correct) + incorrects).shuffled()
    }

    LaunchedEffect(round) {
        isAnswered = false
        timeLeft = 3000L
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < 3000L && !isAnswered) {
            timeLeft = 3000L - (System.currentTimeMillis() - startTime)
            delay(16)
        }
        
        if (!isAnswered) {
            onGameOver()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Indicador de modo (Clásico/Inverso)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            RetroMenuBox(
                backgroundColor = if (isInverse) Color(0xFF9C27B0).copy(alpha = 0.8f) else Color(0xFF4CAF50).copy(alpha = 0.8f),
                borderColor = Color.White,
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    if (isInverse) " MODO INVERSO " else " MODO CLÁSICO ",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        PixelDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Timer Bar
        LinearProgressIndicator(
            progress = { timeLeft / 3000f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(8.dp)
                .clip(CircleShape),
            color = if (timeLeft > 1000) GoldPoke else Color.Red,
            trackColor = Color.White.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.weight(0.5f))

        // Zona del rival
        RetroMenuBox(
            modifier = Modifier.size(220.dp),
            backgroundColor = Color.White.copy(alpha = 0.1f),
            borderColor = if (isInverse) Color(0xFF9C27B0) else Color.White.copy(alpha = 0.3f)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = opponent.imageUrl,
                    contentDescription = opponent.name,
                    modifier = Modifier.size(180.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
        
        RetroText(
            opponent.name,
            fontSize = 24.sp,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Botones de ataque
        Text(
            if (isInverse) "¡ELIGE UN TIPO POCO EFICAZ!" else "¡ELIGE EL TIPO SÚPER EFICAZ!",
            color = if (isInverse) Color(0xFFE1BEE7) else GoldPoke,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        ) {
            items(typeButtons) { type ->
                RetroMenuBox(
                    modifier = Modifier.height(70.dp).clickable {
                        if (!isAnswered) {
                            isAnswered = true
                            val targetList = if (isInverse) opponent.resistances else opponent.weaknesses
                            if (type in targetList) {
                                onRoundWin()
                            } else {
                                onGameOver()
                            }
                        }
                    },
                    backgroundColor = type.color,
                    borderColor = Color.Black
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            type.nombreEs, 
                            fontWeight = FontWeight.Black, 
                            color = if (type == PokeType.ELECTRIC) Color.Black else Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickBattleResult(hasWon: Boolean, isInverse: Boolean, onRetry: () -> Unit, onExit: () -> Unit) {
    SafariResultScreen(
        title = if (hasWon) "¡VICTORIA!" else "DERROTA",
        subtitle = "BATALLA RÁPIDA - ${if (isInverse) "INVERSO" else "CLÁSICO"}",
        description = if (hasWon) 
            "¡Eres un maestro de los tipos! Has vencido a todos los rivales." 
            else "No has logrado superar el desafío. ¡Sigue entrenando!",
        isVictory = hasWon,
        coinsEarned = if (hasWon) (if (isInverse) 200 else 100) else 0,
        onRetry = onRetry,
        onExit = onExit
    )
}
