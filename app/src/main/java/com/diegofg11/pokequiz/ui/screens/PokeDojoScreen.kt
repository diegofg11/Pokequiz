package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import com.diegofg11.pokequiz.ui.components.PokemonHelpDialog
import com.diegofg11.pokequiz.ui.components.HelpSection
import com.diegofg11.pokequiz.ui.theme.*
import com.diegofg11.pokequiz.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- Modelos y Enums ---

enum class DojoDifficulty {
    NORMAL, INFERNAL
}

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
    var difficulty by remember { mutableStateOf(DojoDifficulty.NORMAL) }
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
                onStart = { selectedDifficulty ->
                    difficulty = selectedDifficulty
                    val cost = if (difficulty == DojoDifficulty.INFERNAL) -50 else -20
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
                                globalError = "No tienes suficientes monedas ($cost)."
                            }
                        } catch (e: Exception) {
                            globalError = "Error de conexión: ${e.localizedMessage}"
                        }
                    }
                }
            )
            "PLAYING" -> PokeDojoGame(
                difficulty = difficulty,
                onGameEnd = { finalScore ->
                    score = finalScore
                    gameState = "RESULT"
                    
                    // Calcular premio por rangos
                    val reward = if (difficulty == DojoDifficulty.INFERNAL) {
                        when {
                            finalScore >= 500 -> 400
                            finalScore >= 300 -> 200
                            finalScore >= 100 -> 100
                            else -> 0
                        }
                    } else {
                        when {
                            finalScore >= 250 -> 120
                            finalScore >= 150 -> 80
                            finalScore >= 50 -> 40
                            else -> 0
                        }
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
                difficulty = difficulty,
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
fun PokeDojoStart(onBack: () -> Unit, onStart: (DojoDifficulty) -> Unit) {
    var showHelp by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }

            // Botón de Ayuda
            Surface(
                onClick = { showHelp = true },
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }

        if (showHelp) {
            PokemonHelpDialog(
                title = "INSTRUCCIONES",
                onDismiss = { showHelp = false }
            ) {
                Column {
                    HelpSection("OBJETIVO", "Golpea a los Pokémon que salen de los agujeros para ganar puntos antes de que acabe el tiempo.")
                    HelpSection("PUNTUACIÓN", "• Diglett: +10\n• Dugtrio: +25\n• Pikachu: +50\n• Voltorb: -20 (¡Evítalo!)")
                    HelpSection("MODO NORMAL", "30 segundos de juego con aparición estándar de Pokémon.")
                    HelpSection("MODO INFERNAL", "Solo 20 segundos. Los Voltorb aparecen mucho más a menudo y los Pokémon desaparecen en un abrir y cerrar de ojos.")
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
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
                "Selecciona un modo para empezar",
                color = Color.LightGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp, bottom = 48.dp)
            )

            // Dificultad Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Normal Mode Card
                Card(
                    modifier = Modifier.weight(1f).clickable { onStart(DojoDifficulty.NORMAL) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF795548).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF795548))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("NORMAL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("30s | Estándar", color = Color.LightGray, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("-20 💰", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("+120 💰", color = GoldPoke, fontWeight = FontWeight.Bold)
                    }
                }

                // Infernal Mode Card
                Card(
                    modifier = Modifier.weight(1f).clickable { onStart(DojoDifficulty.INFERNAL) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF212121).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE53935))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("INFERNAL", color = Color(0xFFE53935), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("20s | ¡Caos!", color = Color.LightGray, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("-50 💰", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("+400 💰", color = GoldPoke, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "RANGOS Y PUNTUACIÓN",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Reward Table (Mini)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("RANGO", color = Color.LightGray, fontSize = 10.sp)
                        Text("NORMAL", color = Color.LightGray, fontSize = 10.sp)
                        Text("INFERNAL", color = Color.LightGray, fontSize = 10.sp)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.1f))
                    RankRow("Bronce", "50", "100")
                    RankRow("Plata", "150", "300")
                    RankRow("Oro", "250", "500")
                }
            }
        }
    }
}

@Composable
fun RankRow(rank: String, normal: String, infernal: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(rank, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("$normal pts", color = Color.LightGray, fontSize = 12.sp)
        Text("$infernal pts", color = Color(0xFFE53935), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PokeDojoGame(difficulty: DojoDifficulty, onGameEnd: (Int) -> Unit) {
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(if (difficulty == DojoDifficulty.INFERNAL) 20 else 30) }
    
    val holes = remember { mutableStateListOf<HoleState>().apply { 
        repeat(9) { add(HoleState(it)) }
    } }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        onGameEnd(score)
    }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            val count = if (difficulty == DojoDifficulty.INFERNAL) Random.nextInt(2, 5) else Random.nextInt(1, 4)
            repeat(count) {
                val emptyHoles = holes.indices.filter { holes[it].type == MoleType.EMPTY }
                if (emptyHoles.isNotEmpty()) {
                    val index = emptyHoles.random()
                    
                    val voltorbChance = if (difficulty == DojoDifficulty.INFERNAL) 0.35f else 0.15f
                    val pikachuChance = voltorbChance + 0.10f
                    val dugtrioChance = pikachuChance + 0.15f
                    
                    val rand = Random.nextFloat()
                    val type = when {
                        rand < voltorbChance -> MoleType.VOLTORB
                        rand < pikachuChance -> MoleType.PIKACHU
                        rand < dugtrioChance -> MoleType.DUGTRIO
                        else -> MoleType.DIGLETT
                    }
                    
                    // En infernal desaparecen mucho más rápido
                    val duration = if (difficulty == DojoDifficulty.INFERNAL) (type.duration * 0.6).toLong() else type.duration
                    
                    holes[index] = holes[index].copy(type = type, isHit = false)
                    
                    scope.launch {
                        delay(duration)
                        if (holes[index].type == type) {
                            holes[index] = holes[index].copy(type = MoleType.EMPTY)
                        }
                    }
                }
            }
            val spawnDelay = if (difficulty == DojoDifficulty.INFERNAL) Random.nextLong(400, 800) else Random.nextLong(600, 1200)
            delay(spawnDelay)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                Text("$timeLeft s", color = if (timeLeft < 5) Color.Red else Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }
        }

        if (difficulty == DojoDifficulty.INFERNAL) {
            Text("¡MODO INFERNAL!", color = Color(0xFFE53935), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

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
            .background(Color(0xFF3E2723))
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
fun PokeDojoResult(score: Int, difficulty: DojoDifficulty, onRetry: () -> Unit, onExit: () -> Unit) {
    val rank = if (difficulty == DojoDifficulty.INFERNAL) {
        when {
            score >= 500 -> "ORO"
            score >= 300 -> "PLATA"
            score >= 100 -> "BRONCE"
            else -> "NINGUNO"
        }
    } else {
        when {
            score >= 250 -> "ORO"
            score >= 150 -> "PLATA"
            score >= 50 -> "BRONCE"
            else -> "NINGUNO"
        }
    }
    
    val reward = if (difficulty == DojoDifficulty.INFERNAL) {
        when (rank) {
            "ORO" -> 400
            "PLATA" -> 200
            "BRONCE" -> 100
            else -> 0
        }
    } else {
        when (rank) {
            "ORO" -> 120
            "PLATA" -> 80
            "BRONCE" -> 40
            else -> 0
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("¡FIN DE LA SESIÓN!", color = Color.LightGray, fontSize = 16.sp)
        Text("$score PTS", color = if (difficulty == DojoDifficulty.INFERNAL) Color(0xFFE53935) else Color.White, fontSize = 56.sp, fontWeight = FontWeight.Black)

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
                Text("RANGO ALCANZADO (${difficulty.name}):", color = Color.LightGray, fontSize = 10.sp)
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
