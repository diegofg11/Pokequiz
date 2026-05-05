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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

// --- Modelos de Datos ---

enum class PokeType(val color: Color) {
    FIRE(Color(0xFFF44336)),
    WATER(Color(0xFF2196F3)),
    GRASS(Color(0xFF4CAF50)),
    ELECTRIC(Color(0xFFFFEB3B)),
    GROUND(Color(0xFF795548)),
    FLYING(Color(0xFF9C27B0)),
    ICE(Color(0xFF00BCD4)),
    FIGHTING(Color(0xFFFF5722)),
    PSYCHIC(Color(0xFFE91E63))
}

data class QuickBattleOpponent(
    val name: String,
    val imageUrl: String,
    val weaknesses: List<PokeType>,
    val resistances: List<PokeType>
)

val OPPONENTS_POOL = listOf(
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
    QuickBattleOpponent("FLAREON", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/136.png", listOf(PokeType.WATER, PokeType.GROUND), listOf(PokeType.FIRE, PokeType.GRASS, PokeType.ICE)),
    QuickBattleOpponent("PIDGEOT", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/18.png", listOf(PokeType.ELECTRIC, PokeType.ICE), listOf(PokeType.GRASS, PokeType.FIGHTING)),
    QuickBattleOpponent("GOLEM", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/76.png", listOf(PokeType.WATER, PokeType.GRASS, PokeType.ICE, PokeType.FIGHTING, PokeType.GROUND), listOf(PokeType.FIRE, PokeType.ELECTRIC, PokeType.FLYING)),
    QuickBattleOpponent("SCYTHER", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/123.png", listOf(PokeType.FIRE, PokeType.ELECTRIC, PokeType.ICE, PokeType.FLYING), listOf(PokeType.GRASS, PokeType.FIGHTING)),
    QuickBattleOpponent("ELECTABUZZ", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/125.png", listOf(PokeType.GROUND), listOf(PokeType.ELECTRIC, PokeType.FLYING)),
    QuickBattleOpponent("MAGMAR", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/126.png", listOf(PokeType.WATER, PokeType.GROUND), listOf(PokeType.FIRE, PokeType.GRASS, PokeType.ICE)),
    QuickBattleOpponent("MOLTRES", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/146.png", listOf(PokeType.WATER, PokeType.ELECTRIC), listOf(PokeType.FIRE, PokeType.GRASS, PokeType.FIGHTING))
)

// --- Pantallas ---

@Composable
fun QuickBattleScreen(
    onNavigateBack: () -> Unit,
    onStateChange: (Boolean) -> Unit = {}
) {
    var gameState by remember { mutableStateOf("START") } // START, PLAYING, RESULT
    var hasWon by remember { mutableStateOf(false) }
    var currentRound by remember { mutableIntStateOf(0) }
    var isInverseMode by remember { mutableStateOf(false) }
    var globalError by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()

    LaunchedEffect(gameState) {
        onStateChange(gameState == "START")
    }

    RetroBackground {
        when (gameState) {
            "START" -> QuickBattleStart(
                onStart = { inverse ->
                    isInverseMode = inverse
                    val cost = if (inverse) -50 else -30
                    scope.launch {
                        try {
                            val response = Network.api.rewardUser(RewardRequest(
                                userId = SessionManager.currentUserId,
                                levelId = 0,
                                coinsEarned = cost
                            ))
                            if (response.isSuccessful) {
                                currentRound = 1
                                gameState = "PLAYING"
                            } else {
                                globalError = "No tienes suficientes monedas para entrar."
                            }
                        } catch (e: Exception) {
                            globalError = "Error de conexión: ${e.localizedMessage}"
                        }
                    }
                }
            )
            "PLAYING" -> QuickBattleGame(
                round = currentRound,
                isInverse = isInverseMode,
                onRoundWin = {
                    if (currentRound < 3) {
                        currentRound++
                    } else {
                        hasWon = true
                        gameState = "RESULT"
                        // Reward
                        scope.launch {
                            try {
                                val reward = if (isInverseMode) 200 else 100
                                Network.api.rewardUser(RewardRequest(
                                    userId = SessionManager.currentUserId,
                                    levelId = 0,
                                    coinsEarned = reward
                                ))
                            } catch (e: Exception) { /* ignore */ }
                        }
                    }
                },
                onGameOver = {
                    hasWon = false
                    gameState = "RESULT"
                }
            )
            "RESULT" -> QuickBattleResult(
                hasWon = hasWon,
                isInverse = isInverseMode,
                onRetry = { gameState = "START" },
                onExit = onNavigateBack
            )
        }
    }

        if (globalError != null) {
            PokemonAlertDialog(
                title = "Error",
                message = globalError ?: "",
                onDismiss = { globalError = null }
            )
        }
    }

@Composable
fun QuickBattleStart(onStart: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            RetroText(
                "BATALLA RÁPIDA",
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            )
            Text(
                "Selecciona un modo para empezar",
                color = Color.LightGray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
            )

            // Selector de Modo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Classic Mode Card
                RetroDifficultyCard(
                    title = "CLÁSICO",
                    subtitle = "Usa debilidades",
                    cost = "-30 💰",
                    reward = "+100 💰",
                    color = Color(0xFF4CAF50),
                    onClick = { onStart(false) },
                    modifier = Modifier.weight(1f)
                )

                // Inverse Mode Card
                RetroDifficultyCard(
                    title = "INVERSO",
                    subtitle = "Usa resistencias",
                    cost = "-50 💰",
                    reward = "+200 💰",
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
        // Round Indicator
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RetroText(
                "RONDA $round / 3",
                fontSize = 18.sp
            )
            
            RetroMenuBox(
                backgroundColor = if (isInverse) Color(0xFF9C27B0).copy(alpha = 0.8f) else Color(0xFF4CAF50).copy(alpha = 0.8f),
                borderColor = Color.White,
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    if (isInverse) " INVERSO " else " CLÁSICO ",
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

        // Opponent area
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

        // Attack Buttons
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
                            type.name, 
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
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(if (hasWon) Color(0xFF4CAF50) else Color(0xFFF44336), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (hasWon) "🏆" else "❌",
                fontSize = 60.sp
            )
        }

        Text(
            if (hasWon) "¡VICTORIA!" else "¡DERROTA!",
            color = Color.White,
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 24.dp)
        )

        val modeName = if (isInverse) "Inverso" else "Clásico"
        Text(
            "Modo $modeName",
            color = if (isInverse) Color(0xFFCE93D8) else Color(0xFFA5D6A7),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            if (hasWon) "Has vencido a los 3 rivales.\n¡Eres un maestro de los tipos!" 
            else "No has logrado completar el desafío.\n¡Sigue practicando!",
            color = Color.LightGray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp, bottom = 48.dp)
        )

        if (hasWon) {
            val reward = if (isInverse) 200 else 100
            Text(
                "+$reward MONEDAS",
                color = GoldPoke,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("INTENTAR DE NUEVO", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onExit) {
            Text("VOLVER AL MENÚ", color = Color.White.copy(alpha = 0.7f))
        }
    }
}
