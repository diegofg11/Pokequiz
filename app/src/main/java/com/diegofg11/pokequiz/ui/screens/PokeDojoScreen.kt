package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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

// --- Modelos y Enums ---

enum class MoleType(val score: Int, val imageUrl: String, val duration: Long) {
    EMPTY(0, "", 0L),
    DIGLETT(10, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/50.png", 1200L),
    DUGTRIO(25, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/51.png", 900L),
    PIKACHU(50, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png", 700L),
    VOLTORB(-20, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/100.png", 1000L)
}

data class HoleState(
    val id: Int,
    var type: MoleType = MoleType.EMPTY,
    var isHit: Boolean = false
)

// --- Pantalla Principal ---

@Composable
fun PokeDojoScreen(onNavigateBack: () -> Unit) {
    var gameState by remember { mutableStateOf("START") } // START, PLAYING, RESULT
    var score by remember { mutableIntStateOf(0) }
    var globalError by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundStart, BackgroundMid, BackgroundEnd)))
    ) {
        when (gameState) {
            "START" -> PokeDojoStart(
                onBack = onNavigateBack,
                onStart = {
                    val cost = -20
                    scope.launch {
                        try {
                            val response = Network.api.rewardUser(RewardRequest(
                                userId = SessionManager.currentUserId,
                                levelId = 0,
                                coinsEarned = cost
                            ))
                            if (response.isSuccessful) {
                                score = 0
                                gameState = "PLAYING"
                            } else {
                                globalError = "No tienes suficientes monedas (-20)."
                            }
                        } catch (e: Exception) {
                            globalError = "Error de conexión: ${e.localizedMessage}"
                        }
                    }
                }
            )
            "PLAYING" -> PokeDojoGame(
                onGameEnd = { finalScore ->
                    score = finalScore
                    gameState = "RESULT"
                    
                    // Calcular premio por rangos
                    val reward = when {
                        finalScore >= 250 -> 120
                        finalScore >= 150 -> 80
                        finalScore >= 50 -> 40
                        else -> 0
                    }
                    
                    if (reward > 0) {
                        scope.launch {
                            try {
                                Network.api.rewardUser(RewardRequest(
                                    userId = SessionManager.currentUserId,
                                    levelId = 0,
                                    coinsEarned = reward
                                ))
                            } catch (e: Exception) { /* ignore */ }
                        }
                    }
                }
            )
            "RESULT" -> PokeDojoResult(
                score = score,
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
fun PokeDojoStart(onBack: () -> Unit, onStart: () -> Unit) {
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
                "POKÉ-DOJO",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                "¡Golpea a los Pokémon que salgan de los agujeros!",
                color = Color.LightGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // Reward Cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    DojoRankCard("BRONCE", "50 pts", "+40 💰", Color(0xFFCD7F32))
                }
                item {
                    DojoRankCard("PLATA", "150 pts", "+80 💰", Color(0xFFC0C0C0))
                }
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    DojoRankCard("ORO", "250 pts", "+120 💰", GoldPoke)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "¡CUIDADO CON LOS VOLTORB! (-20 pts)",
                color = Color(0xFFEF5350),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF795548)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Text("EMPEZAR (-20 💰)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DojoRankCard(rank: String, pts: String, reward: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, color)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(rank, color = color, fontWeight = FontWeight.Black, fontSize = 14.sp)
            Text(pts, color = Color.White, fontSize = 12.sp)
            Text(reward, color = GoldPoke, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun PokeDojoGame(onGameEnd: (Int) -> Unit) {
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) }
    
    // Estado de los 9 agujeros
    val holes = remember { mutableStateListOf<HoleState>().apply { 
        repeat(9) { add(HoleState(it)) }
    } }

    val scope = rememberCoroutineScope()

    // Timer de juego
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        onGameEnd(score)
    }

    // Lógica de aparición aleatoria
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            // Decidir cuántos aparecen a la vez (1 a 3)
            val count = Random.nextInt(1, 4)
            repeat(count) {
                val emptyHoles = holes.indices.filter { holes[it].type == MoleType.EMPTY }
                if (emptyHoles.isNotEmpty()) {
                    val index = emptyHoles.random()
                    
                    // Decidir tipo
                    val rand = Random.nextFloat()
                    val type = when {
                        rand < 0.15f -> MoleType.VOLTORB
                        rand < 0.25f -> MoleType.PIKACHU
                        rand < 0.40f -> MoleType.DUGTRIO
                        else -> MoleType.DIGLETT
                    }
                    
                    holes[index] = holes[index].copy(type = type, isHit = false)
                    
                    // Desaparecer después de un tiempo
                    scope.launch {
                        delay(type.duration)
                        if (holes[index].type == type) {
                            holes[index] = holes[index].copy(type = MoleType.EMPTY)
                        }
                    }
                }
            }
            // Esperar antes de la siguiente oleada
            delay(Random.nextLong(600, 1200))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // HUD
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("PUNTOS", color = Color.LightGray, fontSize = 12.sp)
                Text("$score", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("TIEMPO", color = Color.LightGray, fontSize = 12.sp)
                Text("$timeLeft s", color = if (timeLeft < 10) Color.Red else Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Grid de Agujeros
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false
            ) {
                items(9) { index ->
                    DojoHole(
                        state = holes[index],
                        onClick = {
                            if (holes[index].type != MoleType.EMPTY && !holes[index].isHit) {
                                score += holes[index].type.score
                                holes[index] = holes[index].copy(isHit = true)
                                // Desaparecer al ser golpeado
                                scope.launch {
                                    delay(200)
                                    holes[index] = holes[index].copy(type = MoleType.EMPTY)
                                }
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1.5f))
    }
}

@Composable
fun DojoHole(state: HoleState, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(Color(0xFF3E2723)) // Marrón oscuro del agujero
            .border(4.dp, Color(0xFF5D4037), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = state.type != MoleType.EMPTY,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = state.type.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .scale(if (state.isHit) 1.2f else 1f),
                    contentScale = ContentScale.Fit
                )
                
                if (state.isHit) {
                    Text(
                        if (state.type.score > 0) "+${state.type.score}" else "${state.type.score}",
                        color = if (state.type.score > 0) Color.Yellow else Color.Red,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        modifier = Modifier.offset(y = (-20).dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PokeDojoResult(score: Int, onRetry: () -> Unit, onExit: () -> Unit) {
    val rank = when {
        score >= 250 -> "ORO"
        score >= 150 -> "PLATA"
        score >= 50 -> "BRONCE"
        else -> "NINGUNO"
    }
    
    val reward = when (rank) {
        "ORO" -> 120
        "PLATA" -> 80
        "BRONCE" -> 40
        else -> 0
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("¡FIN DE LA SESIÓN!", color = Color.LightGray, fontSize = 16.sp)
        Text("$score PTS", color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Black)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, if (reward > 0) GoldPoke else Color.Gray)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("RANGO ALCANZADO:", color = Color.LightGray, fontSize = 12.sp)
                Text(rank, color = when(rank) {
                    "ORO" -> GoldPoke
                    "PLATA" -> Color(0xFFC0C0C0)
                    "BRONCE" -> Color(0xFFCD7F32)
                    else -> Color.Gray
                }, fontSize = 32.sp, fontWeight = FontWeight.Black)
                
                if (reward > 0) {
                    Text("RECOMPENSA: +$reward 💰", color = Color.Green, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                } else {
                    Text("¡Sigue practicando!", color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

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
