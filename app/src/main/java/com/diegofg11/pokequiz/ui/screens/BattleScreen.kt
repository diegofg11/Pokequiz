package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.tooling.preview.Preview
import com.diegofg11.pokequiz.ui.components.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.diegofg11.pokequiz.R
import com.diegofg11.pokequiz.utils.SoundManager
import com.diegofg11.pokequiz.utils.PokemonUtils

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
    
    // Estados para animaciones de daño
    var isEnemyTakingDamage by remember { mutableStateOf(false) }
    var isPlayerTakingDamage by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val baseUrl = com.diegofg11.pokequiz.api.Network.BASE_URL.dropLast(1)

    LaunchedEffect(levelId) {
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) { Network.api.getLevelData(levelId.toString()) }
                val pcResponse = withContext(Dispatchers.IO) { Network.api.getPc(com.diegofg11.pokequiz.utils.SessionManager.currentUserId) }

                if (response.isSuccessful && response.body() != null) {
                    levelData = response.body()
                    if (levelData != null && levelData!!.enemy != null) {
                        val fixedEnemy = levelData!!.enemy!!.copy(
                            spriteFront = PokemonUtils.fixSpriteUrl(levelData!!.enemy!!.spriteFront)
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
                            spriteBack = PokemonUtils.fixSpriteUrl(p.spriteBack),
                            spriteFront = PokemonUtils.fixSpriteUrl(p.spriteFront)
                        )
                    }
                    currentPlayerIndex = 0
                    val first = currentParty.first()
                    userHp = first.hpBase + (first.level * 5)
                } else {
                    userHp = 100 // Fallback
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { errorMessage = context.getString(R.string.error_generic) }
            } finally {
                isLoading = false
            }
        }
    }


    if (errorMessage != null) {
        PokemonAlertDialog(
            title = stringResource(R.string.error_title),
            message = errorMessage!!,
            isError = true,
            onDismiss = {
                errorMessage = null
                if (isLoading || levelData == null) {
                    onNavigateBack()
                }
            }
        )
    }

    if (isLoading || levelData == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1B3022)), 
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = GoldPoke)
        }
        return
    }

    val currentQuestion = levelData!!.questions.getOrNull(currentQuestionIndex) ?: return
    val playerPokemon = currentParty.getOrNull(currentPlayerIndex)
    val pLevelVal = playerPokemon?.level ?: 1
    val pName = playerPokemon?.nombre ?: stringResource(R.string.no_pokemon)
    val pSprite = playerPokemon?.spriteBack ?: "" // Coil handles empty gracefully
    val pMaxHp = (playerPokemon?.hpBase ?: 100) + (pLevelVal * 5)
    val pLevel = "${stringResource(R.string.level_prefix)}$pLevelVal"

    val checkAnswer: (Int) -> Unit = { index ->
        val isCorrect = index == currentQuestion.correctAnswerIndex
        val enemyLevel = levelData!!.enemy?.level ?: 1
        val enemyHpBase = levelData!!.enemy?.hpBase ?: 100

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    Network.api.calculateBattleDamage(
                        com.diegofg11.pokequiz.models.BattleDamageRequest(
                            playerLevel = pLevelVal,
                            enemyLevel = enemyLevel,
                            isCorrect = isCorrect,
                            enemyHpBase = enemyHpBase
                        )
                    )
                }

                if (response.isSuccessful && response.body() != null) {
                    val damage = response.body()!!
                    var gameOverThisTurn = false

                    if (isCorrect) {
                        // Efecto de daño al enemigo
                        isEnemyTakingDamage = true
                        kotlinx.coroutines.delay(500)
                        isEnemyTakingDamage = false
                        opponentHp = (opponentHp - damage.damageDealt).coerceAtLeast(0)
                        
                        if (opponentHp <= 0) {
                            gameOverMessage = context.getString(R.string.you_won)
                            isVictory = true
                            showGameOver = true
                            gameOverThisTurn = true
                        }
                    } else {
                        // Efecto de daño al jugador
                        isPlayerTakingDamage = true
                        kotlinx.coroutines.delay(500)
                        isPlayerTakingDamage = false
                        userHp = (userHp - damage.damageReceived).coerceAtLeast(0)
                        
                        if (userHp <= 0) {
                            if (currentPlayerIndex < currentParty.size - 1) {
                                // Siguiente Pokémon
                                currentPlayerIndex++
                                val nextPkmn = currentParty[currentPlayerIndex]
                                userHp = nextPkmn.hpBase + (nextPkmn.level * 5)
                            } else {
                                gameOverMessage = context.getString(R.string.you_lost)
                                isVictory = false
                                showGameOver = true
                                gameOverThisTurn = true
                            }
                        }
                    }

                    if (!gameOverThisTurn) {
                        val questions = levelData!!.questions
                        if (currentQuestionIndex < questions.size - 1) {
                            currentQuestionIndex++
                            
                            // Si quedan pocas preguntas, pedir más
                            if (questions.size - currentQuestionIndex <= 5) {
                                val nextId = questions.maxOfOrNull { it.id } ?: 100
                                val moreResp = withContext(Dispatchers.IO) { 
                                    Network.api.getMoreQuestions(level = levelId, count = 10, startId = nextId + 1) 
                                }
                                if (moreResp.isSuccessful && moreResp.body() != null) {
                                    val newQuestions = moreResp.body()!!
                                    val currentIds = questions.map { it.id }.toSet()
                                    val filteredNew = newQuestions.filter { it.id !in currentIds }
                                    if (filteredNew.isNotEmpty()) {
                                        levelData = levelData!!.copy(questions = questions + filteredNew)
                                    }
                                }
                            }
                        } else {
                            currentQuestionIndex = 0
                            levelData = levelData!!.copy(questions = levelData!!.questions.shuffled())
                        }
                    }
                }
            } catch (e: Exception) {
                // Error de red
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Cielo con gradiente azul (Capa base)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.62f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF5BA3D8), Color(0xFFB0DDF8))
                    )
                )
        )
        // Suelo: hierba
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.38f)
                .align(Alignment.BottomStart)
                .background(Color(0xFF4A8F3F))
        )
        
        Column(Modifier.fillMaxSize()) {
            // CAMPO DE BATALLA (Arriba)
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                // Info Enemigo (Arriba Izquierda)
                Box(Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 32.dp)) {
                    val eLevel = levelData!!.enemy?.level ?: 1
                    PokemonStatusBox(levelData!!.enemy?.nombre ?: stringResource(R.string.enemy_label), "${stringResource(R.string.level_prefix)}$eLevel", opponentHp, (levelData!!.enemy?.hpBase ?: 100) + (eLevel * 5), false)
                }
                // Sprite Enemigo (Arriba Derecha)
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(end = 24.dp, top = 24.dp).size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.battle_base),
                        contentDescription = null,
                        modifier = Modifier.width(250.dp).height(120.dp).align(Alignment.BottomCenter).offset(x = 0.dp, y = 30.dp),
                        contentScale = ContentScale.Fit
                    )
                    val enemyShake by animateFloatAsState(
                        targetValue = if (isEnemyTakingDamage) 10f else 0f,
                        animationSpec = repeatable(iterations = 5, animation = tween(50), repeatMode = RepeatMode.Reverse)
                    )
                    AsyncImage(
                        model = levelData!!.enemy?.spriteFront,
                        contentDescription = stringResource(R.string.desc_enemy),
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = enemyShake.dp)
                            .graphicsLayer(alpha = if (isEnemyTakingDamage) 0.5f else 1f)
                    )
                }

                // Sprite Jugador (Abajo Izquierda)
                Box(
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 48.dp).size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.battle_base),
                        contentDescription = null,
                        modifier = Modifier.width(350.dp).height(180.dp).align(Alignment.BottomCenter).offset(x = (-20).dp, y = 50.dp),
                        contentScale = ContentScale.Fit
                    )
                    val playerShake by animateFloatAsState(
                        targetValue = if (isPlayerTakingDamage) 10f else 0f,
                        animationSpec = repeatable(iterations = 5, animation = tween(50), repeatMode = RepeatMode.Reverse)
                    )
                    AsyncImage(
                        model = pSprite,
                        contentDescription = stringResource(R.string.desc_player),
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = playerShake.dp)
                            .graphicsLayer(alpha = if (isPlayerTakingDamage) 0.5f else 1f)
                    )
                }
                // Info Jugador (Abajo Derecha)
                Box(Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp)) {
                    PokemonStatusBox(pName, pLevel.uppercase(), userHp, pMaxHp, true)
                }
            }

            // CUADRO DE DIÁLOGO Y OPCIONES (Abajo)
            RetroMenuBox(
                modifier = Modifier.fillMaxWidth().height(260.dp),
                backgroundColor = Color(0xFF485058), // Color gris oscuro batalla GBA
                borderColor = Color.Black
            ) {
                Column(Modifier.fillMaxSize()) {
                    // Texto / Pregunta
                    RetroMenuBox(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        backgroundColor = Color.White,
                        borderColor = Color(0xFFB06868)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = currentQuestion.text, 
                                color = Color.Black, 
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botones de Opciones
                    Column(modifier = Modifier.fillMaxWidth().weight(1.2f)) {
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BattleOptionBtn(currentQuestion.options.getOrElse(0) { "" }, Modifier.weight(1f)) { checkAnswer(0) }
                            BattleOptionBtn(currentQuestion.options.getOrElse(1) { "" }, Modifier.weight(1f)) { checkAnswer(1) }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BattleOptionBtn(currentQuestion.options.getOrElse(2) { "" }, Modifier.weight(1f)) { checkAnswer(2) }
                            BattleOptionBtn(currentQuestion.options.getOrElse(3) { "" }, Modifier.weight(1f)) { checkAnswer(3) }
                        }
                    }
                }
            }
        }

        // Overlay de Fin de Juego (Sustituye al Dialog para ser más inmersivo)
        if (showGameOver) {
            GameOverOverlay(
                message = gameOverMessage,
                isVictory = isVictory,
                onDismiss = if (isVictory) {
                    {
                        scope.launch {
                            try { 
                                Network.api.rewardUser(com.diegofg11.pokequiz.models.RewardRequest(com.diegofg11.pokequiz.utils.SessionManager.currentUserId, levelId)) 
                                withContext(Dispatchers.Main) { 
                                    showGameOver = false
                                    onBattleWin() 
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    errorMessage = context.getString(R.string.reward_claim_error)
                                }
                            }
                        }
                        Unit
                    }
                } else onNavigateBack
            )
        }
    }
}


@Composable
fun PokemonStatusBox(name: String, level: String, currentHp: Int, maxHp: Int, isPlayer: Boolean) {
    RetroMenuBox(
        modifier = Modifier.width(200.dp),
        backgroundColor = Color(0xFFF8F8D8), // Amarillo pálido clásico
        borderColor = Color(0xFF506858)
    ) {
        Column(modifier = Modifier.padding(2.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RetroText(text = name.uppercase(), fontSize = 12.sp, color = Color.Black, showShadow = false)
                Text(text = level, fontWeight = FontWeight.Bold, color = Color(0xFF506858), fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(6.dp))
            
            // Barra de PS Retro
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.hp_label), 
                    color = Color(0xFFE8B040), 
                    fontWeight = FontWeight.Black, 
                    fontSize = 10.sp, 
                    modifier = Modifier.padding(end = 4.dp),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                val progress by animateFloatAsState(targetValue = currentHp.toFloat() / maxHp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .background(Color.Black)
                        .padding(1.dp)
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
                Text(
                    text = "$currentHp / $maxHp", 
                    color = Color.Black, 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.align(Alignment.End).padding(top = 2.dp),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun BattleOptionBtn(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    RetroButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        containerColor = Color.White,
        contentColor = Color.Black,
        borderColor = Color(0xFFD06860),
        fontSize = 12.sp
    )
}

@Composable
fun GameOverOverlay(message: String, isVictory: Boolean, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .pointerInput(Unit) { detectTapGestures {} },
        contentAlignment = Alignment.Center
    ) {
        RetroMenuBox(
            modifier = Modifier.fillMaxWidth(0.85f),
            backgroundColor = if (isVictory) Color(0xFFFFFBE6) else Color(0xFFFBE6E6),
            borderColor = if (isVictory) GoldPoke else RedPoke
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RetroText(
                    text = if (isVictory) stringResource(R.string.victory) else stringResource(R.string.defeat),
                    fontSize = 32.sp,
                    color = if (isVictory) Color(0xFFB8860B) else Color(0xFFB71C1C)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                PixelDivider()
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                RetroButton(
                    text = stringResource(R.string.back_to_map),
                    onClick = onDismiss,
                    containerColor = if (isVictory) Color(0xFF2D5A27) else RedPoke,
                    modifier = Modifier.fillMaxWidth()
                )
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
