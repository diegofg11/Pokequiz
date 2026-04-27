package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import com.diegofg11.pokequiz.ui.components.PokemonAlertDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.models.PokemonBattle
import com.diegofg11.pokequiz.models.Question
import com.diegofg11.pokequiz.ui.theme.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.models.LevelResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.diegofg11.pokequiz.models.Pokemon

@Composable
fun BattleScreen(
    levelId: Int,
    onBattleWin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var levelData by remember { mutableStateOf<LevelResponse?>(null) }
    var currentParty by remember { mutableStateOf<List<Pokemon>>(emptyList()) }
    var currentPlayerIndex by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var opponentHp by remember { mutableIntStateOf(100) }
    var userHp by remember { mutableIntStateOf(100) }
    var showGameOver by remember { mutableStateOf(false) }
    var gameOverMessage by remember { mutableStateOf("") }
    var isVictory by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val baseUrl = "https://pokequizbackend-production.up.railway.app"

    LaunchedEffect(levelId) {
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) { Network.api.getLevelData(levelId.toString()) }
                val pcResponse = withContext(Dispatchers.IO) { Network.api.getPc(1) }

                if (response.isSuccessful && response.body() != null) {
                    levelData = response.body()
                    if (levelData != null && levelData!!.enemy != null) {
                        val fixedEnemy = levelData!!.enemy!!.copy(
                            spriteFront = if (levelData!!.enemy!!.spriteFront.startsWith("/")) baseUrl + levelData!!.enemy!!.spriteFront else levelData!!.enemy!!.spriteFront
                        )
                        levelData = levelData!!.copy(enemy = fixedEnemy)
                        val eLevel = fixedEnemy.level ?: 1
                        opponentHp = fixedEnemy.hpBase + (eLevel * 5)
                    }
                }

                if (pcResponse.isSuccessful && pcResponse.body()?.isNotEmpty() == true) {
                    val pc = pcResponse.body()!!
                    var party = pc.filter { it.inParty }
                    if (party.isEmpty()) party = listOf(pc.first()) // Fallback si no hay equipo

                    currentParty = party.map { p ->
                        p.copy(
                            spriteBack = if (p.spriteBack.startsWith("/")) baseUrl + p.spriteBack else p.spriteBack,
                            spriteFront = if (p.spriteFront.startsWith("/")) baseUrl + p.spriteFront else p.spriteFront
                        )
                    }
                    currentPlayerIndex = 0
                    val first = currentParty.first()
                    userHp = first.hpBase + (first.level * 5)
                } else {
                    userHp = 100 // Fallback
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { errorMessage = "Error de red" }
            } finally {
                isLoading = false
            }
        }
    }

    if (showGameOver) {
        GameOverDialog(
            message = gameOverMessage,
            isVictory = isVictory,
            onDismiss = if (isVictory) {
                {
                    scope.launch {
                        try { Network.api.rewardUser(com.diegofg11.pokequiz.models.RewardRequest(1, levelId, 100)) } catch (e: Exception) {}
                        withContext(Dispatchers.Main) { onBattleWin() }
                    }
                    Unit
                }
            } else onNavigateBack
        )
    }

    if (isLoading || levelData == null) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(BackgroundStart, BackgroundMid, BackgroundEnd))), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            }
            if (errorMessage != null) {
                PokemonAlertDialog(
                    title = "¡Error!",
                    message = errorMessage!!,
                    isError = true,
                    onDismiss = {
                        errorMessage = null
                        onNavigateBack()
                    }
                )
            }
        }
        return
    }

    val currentQuestion = levelData!!.questions.getOrNull(currentQuestionIndex) ?: return
    val playerPokemon = currentParty.getOrNull(currentPlayerIndex)
    val pLevelVal = playerPokemon?.level ?: 1
    val pName = playerPokemon?.nombre ?: "Sin Pokémon"
    val pSprite = playerPokemon?.spriteBack ?: "" // Coil handles empty gracefully
    val pMaxHp = (playerPokemon?.hpBase ?: 100) + (pLevelVal * 5)
    val pLevel = "Nv$pLevelVal"

    val checkAnswer: (Int) -> Unit = { index ->
        val enemyLevel = levelData!!.enemy?.level ?: 1
        val maxEnemyHp = (levelData!!.enemy?.hpBase ?: 100) + (enemyLevel * 5)
        val damagePerHit = kotlin.math.ceil(maxEnemyHp.toDouble() / 10.0).toInt()

        if (index == currentQuestion.correctAnswerIndex) {
            opponentHp = (opponentHp - damagePerHit).coerceAtLeast(0)
            if (opponentHp == 0) {
                    gameOverMessage = "¡HAS GANADO!"
                    isVictory = true
                    showGameOver = true
                }
        } else {
            // Recibir daño escalado: Base 25 + (Nivel Enemigo * 2) - (Nivel Jugador * 2)
            val damageTaken = (25 + (enemyLevel * 2) - (pLevelVal * 2)).coerceIn(10, 60)
            userHp = (userHp - damageTaken).coerceAtLeast(0)
            if (userHp == 0) {
                if (currentPlayerIndex < currentParty.size - 1) {
                    // Siguiente Pokémon
                    currentPlayerIndex++
                    val nextPkmn = currentParty[currentPlayerIndex]
                    userHp = nextPkmn.hpBase + (nextPkmn.level * 5)
                } else {
                    gameOverMessage = "HAS PERDIDO..."
                    isVictory = false
                    showGameOver = true
                }
            }
        }
        if (!showGameOver) {
            if (currentQuestionIndex < levelData!!.questions.size - 1) currentQuestionIndex++
            else { showGameOver = true; gameOverMessage = "¡TE QUEDASTE SIN PREGUNTAS!" }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Cielo con gradiente azul
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.62f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF5BA3D8), // Azul cielo
                            Color(0xFF88C8F0), // Celeste suave
                            Color(0xFFB0DDF8)  // Casi blanco-azulado
                        )
                    )
                )
        )
        // Suelo: hierba
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.38f)
                .align(Alignment.BottomStart)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4A8F3F), // Verde hierba oscuro
                            Color(0xFF5EAD50), // Verde medio
                            Color(0xFF72C464)  // Verde claro
                        )
                    )
                )
        )
        // Franja de tierra/separador
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .align(Alignment.Center)
                .offset(y = 80.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF8B6914),
                            Color(0xFFA0791A),
                            Color(0xFF8B6914)
                        )
                    )
                )
        )
        Column(Modifier.fillMaxSize()) {
            // CAMPO DE BATALLA (Arriba)
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                // Info Enemigo (Arriba Izquierda)
                Box(Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 32.dp)) {
                    val eLevel = levelData!!.enemy?.level ?: 1
                    PokemonStatusBox(levelData!!.enemy?.nombre ?: "Enemigo", "Nv$eLevel", opponentHp, levelData!!.enemy?.hpBase ?: 100, false)
                }
                // Sprite Enemigo (Arriba Derecha)
                AsyncImage(
                    model = levelData!!.enemy?.spriteFront,
                    contentDescription = "Enemy",
                    modifier = Modifier.align(Alignment.TopEnd).padding(end = 24.dp, top = 24.dp).size(160.dp)
                )

                // Sprite Jugador (Abajo Izquierda)
                AsyncImage(
                    model = pSprite,
                    contentDescription = "Player",
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 48.dp).size(180.dp)
                )
                // Info Jugador (Abajo Derecha)
                Box(Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp)) {
                    PokemonStatusBox(pName, pLevel, userHp, pMaxHp, true)
                }
            }

            // CUADRO DE DIÁLOGO Y OPCIONES (Abajo)
            Surface(
                modifier = Modifier.fillMaxWidth().height(250.dp), // Height increased from 200 to 250
                color = Color(0xFF282828), // Borde exterior negro clásico
                border = androidx.compose.foundation.BorderStroke(4.dp, Color(0xFFE8E8E8))
            ) {
                // Background interior
                Box(Modifier.fillMaxSize().padding(6.dp).background(Color(0xFF485058))) {
                    Column(Modifier.fillMaxSize()) {
                        // Texto (Arriba)
                        Surface(
                            modifier = Modifier.fillMaxWidth().weight(0.7f).padding(8.dp), // Darle más espacio con 0.7f
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFB06868))
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = currentQuestion.text, 
                                    color = Color.Black, 
                                    fontSize = 17.sp, 
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                )
                            }
                        }

                        // Botones de Opciones (Abajo)
                        Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp).padding(bottom = 8.dp)) {
                            Row(Modifier.weight(1f)) {
                                BattleOptionBtn(currentQuestion.options.getOrElse(0) { "" }, Modifier.weight(1f)) { checkAnswer(0) }
                                Spacer(Modifier.width(8.dp))
                                BattleOptionBtn(currentQuestion.options.getOrElse(1) { "" }, Modifier.weight(1f)) { checkAnswer(1) }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.weight(1f)) {
                                BattleOptionBtn(currentQuestion.options.getOrElse(2) { "" }, Modifier.weight(1f)) { checkAnswer(2) }
                                Spacer(Modifier.width(8.dp))
                                BattleOptionBtn(currentQuestion.options.getOrElse(3) { "" }, Modifier.weight(1f)) { checkAnswer(3) }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PokemonStatusBox(name: String, level: String, currentHp: Int, maxHp: Int, isPlayer: Boolean) {
    Surface(
        shape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 16.dp),
        color = Color(0xFFF8F8D8),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF506858)),
        modifier = Modifier.width(180.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name.uppercase(), fontWeight = FontWeight.Bold, color = Color.Black)
                Text(level, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            
            // Barra de PS
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("PS", color = Color(0xFFE8B040), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp))
                val progress by animateFloatAsState(targetValue = currentHp.toFloat() / maxHp)
                Box(
                    modifier = Modifier.fillMaxWidth().height(8.dp).background(Color.DarkGray).border(1.dp, Color.Black)
                ) {
                    Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(
                        when {
                            progress > 0.5f -> Color(0xFF48D0B0)
                            progress > 0.2f -> Color(0xFFF8C848)
                            else -> Color(0xFFF85838)
                        }
                    ))
                }
            }

            if (isPlayer) {
                Text("$currentHp / $maxHp", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}

@Composable
fun BattleOptionBtn(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.fillMaxHeight().clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8F8F8),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFD06860))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun GameOverDialog(message: String, isVictory: Boolean, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                shape = RoundedCornerShape(24.dp),
                color = if (isVictory) Color(0xFFFFFBE6) else Color(0xFFFBE6E6),
                border = androidx.compose.foundation.BorderStroke(4.dp, if (isVictory) Color(0xFFFFD700) else Color(0xFFD32F2F)),
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isVictory) "¡VICTORIA!" else "DERROTA",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = if (isVictory) Color(0xFFB8860B) else Color(0xFFB71C1C)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = message,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVictory) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(56.dp)
                    ) {
                        Text(
                            text = "VOLVER AL MAPA",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
            
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopCenter)
                    .border(
                        4.dp,
                        if (isVictory) Color(0xFFFFD700) else Color(0xFFD32F2F),
                        androidx.compose.foundation.shape.CircleShape
                    ),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (isVictory) Color(0xFFFFD700) else Color(0xFFD32F2F),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isVictory) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Victoria",
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Derrota",
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BattleScreenPreview() {
    PokequizTheme {
        BattleScreen(levelId = 1, onBattleWin = {}, onNavigateBack = {})
    }
}
