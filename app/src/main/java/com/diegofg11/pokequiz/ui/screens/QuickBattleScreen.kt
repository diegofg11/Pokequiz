package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.models.RewardRequest
import com.diegofg11.pokequiz.ui.components.PokemonAlertDialog
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
    val weaknesses: List<PokeType>
)

val OPPONENTS_POOL = listOf(
    QuickBattleOpponent("CHARIZARD", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/6.png", listOf(PokeType.WATER, PokeType.ELECTRIC)),
    QuickBattleOpponent("BLASTOISE", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/9.png", listOf(PokeType.GRASS, PokeType.ELECTRIC)),
    QuickBattleOpponent("VENUSAUR", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/3.png", listOf(PokeType.FIRE, PokeType.ICE, PokeType.FLYING, PokeType.PSYCHIC)),
    QuickBattleOpponent("PIKACHU", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png", listOf(PokeType.GROUND)),
    QuickBattleOpponent("GYARADOS", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/130.png", listOf(PokeType.ELECTRIC)),
    QuickBattleOpponent("ONIX", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/95.png", listOf(PokeType.WATER, PokeType.GRASS, PokeType.ICE, PokeType.FIGHTING, PokeType.GROUND)),
    QuickBattleOpponent("DRAGONITE", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/149.png", listOf(PokeType.ICE)),
    QuickBattleOpponent("ALAKAZAM", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/65.png", listOf(PokeType.PSYCHIC)),
    QuickBattleOpponent("MACHAMP", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/68.png", listOf(PokeType.FLYING, PokeType.PSYCHIC)),
    QuickBattleOpponent("LAPRAS", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/131.png", listOf(PokeType.ELECTRIC, PokeType.GRASS, PokeType.FIGHTING)),
    QuickBattleOpponent("ARCANINE", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/59.png", listOf(PokeType.WATER, PokeType.GROUND)),
    QuickBattleOpponent("JOLTEON", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/135.png", listOf(PokeType.GROUND)),
    QuickBattleOpponent("VAPOREON", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/134.png", listOf(PokeType.GRASS, PokeType.ELECTRIC)),
    QuickBattleOpponent("FLAREON", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/136.png", listOf(PokeType.WATER, PokeType.GROUND)),
    QuickBattleOpponent("PIDGEOT", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/18.png", listOf(PokeType.ELECTRIC, PokeType.ICE)),
    QuickBattleOpponent("GOLEM", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/76.png", listOf(PokeType.WATER, PokeType.GRASS, PokeType.ICE, PokeType.FIGHTING, PokeType.GROUND)),
    QuickBattleOpponent("SCYTHER", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/123.png", listOf(PokeType.FIRE, PokeType.ELECTRIC, PokeType.ICE, PokeType.FLYING)),
    QuickBattleOpponent("ELECTABUZZ", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/125.png", listOf(PokeType.GROUND)),
    QuickBattleOpponent("MAGMAR", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/126.png", listOf(PokeType.WATER, PokeType.GROUND)),
    QuickBattleOpponent("MOLTRES", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/146.png", listOf(PokeType.WATER, PokeType.ELECTRIC))
)

// --- Pantallas ---

@Composable
fun QuickBattleScreen(onNavigateBack: () -> Unit) {
    var gameState by remember { mutableStateOf("START") } // START, PLAYING, RESULT
    var hasWon by remember { mutableStateOf(false) }
    var currentRound by remember { mutableIntStateOf(0) }
    var globalError by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundStart, BackgroundMid, BackgroundEnd)))
    ) {
        when (gameState) {
            "START" -> QuickBattleStart(
                onBack = onNavigateBack,
                onStart = {
                    val cost = -30
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
                                globalError = "No tienes suficientes monedas para entrar (-30)."
                            }
                        } catch (e: Exception) {
                            globalError = "Error de conexión: ${e.localizedMessage}"
                        }
                    }
                }
            )
            "PLAYING" -> QuickBattleGame(
                round = currentRound,
                onRoundWin = {
                    if (currentRound < 3) {
                        currentRound++
                    } else {
                        hasWon = true
                        gameState = "RESULT"
                        // Reward
                        scope.launch {
                            try {
                                Network.api.rewardUser(RewardRequest(
                                    userId = SessionManager.currentUserId,
                                    levelId = 0,
                                    coinsEarned = 150
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
                onRetry = { gameState = "START" },
                onExit = onNavigateBack
            )
        }

        if (globalError != null) {
            PokemonAlertDialog(
                title = "Error",
                message = globalError ?: "",
                onDismiss = { globalError = null },
                onConfirm = { globalError = null }
            )
        }
    }
}

@Composable
fun QuickBattleStart(onBack: () -> Unit, onStart: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "BATALLA RÁPIDA",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                "¡Vence a 3 Pokémon seguidos usando su debilidad elemental!",
                color = Color.LightGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, GoldPoke)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Reglas:", color = GoldPoke, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("• Tienes 3 segundos por ronda.", color = Color.White, fontSize = 14.sp)
                    Text("• Identifica al Pokémon rival.", color = Color.White, fontSize = 14.sp)
                    Text("• Pulsa el botón del tipo Súper Eficaz.", color = Color.White, fontSize = 14.sp)
                    Text("• Un fallo y se acaba la racha.", color = Color.White, fontSize = 14.sp)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("COSTO", color = Color.LightGray, fontSize = 10.sp)
                            Text("-30", color = Color(0xFFEF5350), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PREMIO", color = Color.LightGray, fontSize = 10.sp)
                            Text("+150", color = Color(0xFF66BB6A), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("¡EMPEZAR DESAFÍO!", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun QuickBattleGame(round: Int, onRoundWin: () -> Unit, onGameOver: () -> Unit) {
    val opponent = remember(round) { OPPONENTS_POOL.random() }
    var timerProgress by remember { mutableFloatStateOf(1f) }
    var isAnswered by remember { mutableStateOf(false) }
    
    // Generar 4 tipos: uno correcto y 3 incorrectos
    val typeButtons = remember(round) {
        val correct = opponent.weaknesses.random()
        val incorrects = PokeType.entries.filter { it !in opponent.weaknesses }.shuffled().take(3)
        (listOf(correct) + incorrects).shuffled()
    }

    LaunchedEffect(round) {
        timerProgress = 1f
        isAnswered = false
        val startTime = System.currentTimeMillis()
        val duration = 3000L
        
        while (System.currentTimeMillis() - startTime < duration && !isAnswered) {
            timerProgress = 1f - (System.currentTimeMillis() - startTime).toFloat() / duration
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
        Text(
            "RONDA $round / 3",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 32.dp)
        )

        // Timer Bar
        LinearProgressIndicator(
            progress = { timerProgress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(8.dp)
                .clip(CircleShape),
            color = if (timerProgress > 0.3f) GoldPoke else Color.Red,
            trackColor = Color.White.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.weight(0.5f))

        // Opponent area
        Box(
            modifier = Modifier
                .size(220.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .border(4.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = opponent.imageUrl,
                contentDescription = opponent.name,
                modifier = Modifier.size(180.dp),
                contentScale = ContentScale.Fit
            )
        }
        
        Text(
            opponent.name,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Attack Buttons
        Text(
            "¡ELIGE EL TIPO EFICAZ!",
            color = GoldPoke,
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
                Button(
                    onClick = {
                        if (!isAnswered) {
                            isAnswered = true
                            if (type in opponent.weaknesses) {
                                onRoundWin()
                            } else {
                                onGameOver()
                            }
                        }
                    },
                    modifier = Modifier.height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = type.color),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(type.name, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QuickBattleResult(hasWon: Boolean, onRetry: () -> Unit, onExit: () -> Unit) {
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

        Text(
            if (hasWon) "Has vencido a los 3 rivales.\n¡Eres un maestro de los tipos!" 
            else "No has logrado completar el desafío.\n¡Sigue practicando!",
            color = Color.LightGray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp, bottom = 48.dp)
        )

        if (hasWon) {
            Text(
                "+150 MONEDAS",
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
