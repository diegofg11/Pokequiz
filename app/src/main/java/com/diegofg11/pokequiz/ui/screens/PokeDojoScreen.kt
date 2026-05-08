package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.*
import androidx.compose.ui.res.stringResource
import com.diegofg11.pokequiz.R
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.diegofg11.pokequiz.models.DojoDifficulty
import com.diegofg11.pokequiz.models.MoleType
import com.diegofg11.pokequiz.models.HoleState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun PokeDojoScreen(
    onNavigateBack: () -> Unit,
    onStateChange: (Boolean) -> Unit = {}
) {
    var gameState by remember { mutableStateOf(SafariGameState.START) }
    var difficulty by remember { mutableStateOf(DojoDifficulty.NORMAL) }
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(0) }
    var globalError by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()

    LaunchedEffect(gameState) {
        onStateChange(gameState == SafariGameState.START)
    }

    if (globalError != null) {
        PokemonAlertDialog(
            title = stringResource(R.string.error_title),
            message = globalError ?: "",
            onDismiss = { globalError = null },
            onConfirm = { globalError = null }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (gameState != SafariGameState.START) {
                SafariRetroHeader(
                    title = stringResource(R.string.dojo_title),
                    onBackClick = {
                        if (gameState == SafariGameState.PLAYING) {
                            gameState = SafariGameState.RESULT
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
                                label = stringResource(R.string.dojo_points),
                                value = "$score",
                                containerColor = Color(0xFFFB8C00), // Orange
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                            )
                            
                            RetroStatCard(
                                label = stringResource(R.string.dojo_time),
                                value = "$timeLeft",
                                containerColor = if (timeLeft < 5) Color(0xFFE53935) else Color(0xFF2D5A27),
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                            )
                        }
                        
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            PokeDojoGame(
                                difficulty = difficulty,
                                timeLeft = timeLeft,
                                score = score,
                                onScoreUpdate = { score = it },
                                onTimeUpdate = { timeLeft = it },
                                onGameEnd = { finalScore ->
                                    score = finalScore
                                    gameState = SafariGameState.RESULT
                                    
                                    val reward = if (difficulty == DojoDifficulty.INFERNAL) {
                                        when {
                                            finalScore >= 1000 -> 750
                                            finalScore >= 600 -> 350
                                            finalScore >= 200 -> 120
                                            else -> 0
                                        }
                                    } else {
                                        when {
                                            finalScore >= 500 -> 350
                                            finalScore >= 300 -> 180
                                            finalScore >= 100 -> 60
                                            else -> 0
                                        }
                                    }
                                    SafariUtils.rewardUser(scope = scope, coins = reward, gameType = "dojo", difficulty = difficulty.name)
                                }
                            )
                        }
                    }
                } else {
                    when (gameState) {
                        SafariGameState.START -> SafariSelectionScreen(
                            title = stringResource(R.string.dojo_title),
                            subtitle = stringResource(R.string.dojo_subtitle),
                            cards = listOf(
                                DifficultyCardData(stringResource(R.string.normal), stringResource(R.string.dojo_normal_desc), "-20", "350", Color(0xFF795548), {
                                    difficulty = DojoDifficulty.NORMAL
                                    SafariUtils.rewardUser(
                                        scope = scope,
                                        coins = -20,
                                        gameType = "dojo",
                                        difficulty = "NORMAL",
                                        onSuccess = {
                                            score = 0
                                            timeLeft = 30
                                            gameState = SafariGameState.PLAYING
                                        },
                                        onError = { globalError = it }
                                    )
                                }),
                                DifficultyCardData(stringResource(R.string.infernal), stringResource(R.string.dojo_infernal_desc), "-50", "750", Color(0xFFE53935), {
                                    difficulty = DojoDifficulty.INFERNAL
                                    SafariUtils.rewardUser(
                                        scope = scope,
                                        coins = -50,
                                        gameType = "dojo",
                                        difficulty = "INFERNAL",
                                        onSuccess = {
                                            score = 0
                                            timeLeft = 20
                                            gameState = SafariGameState.PLAYING
                                        },
                                        onError = { globalError = it }
                                    )
                                })
                            )
                        )
                        SafariGameState.RESULT -> PokeDojoResult(
                            score = score,
                            difficulty = difficulty,
                            onRetry = { gameState = SafariGameState.START },
                            onExit = onNavigateBack
                        )
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun PokeDojoGame(
    difficulty: DojoDifficulty, 
    timeLeft: Int, 
    score: Int,
    onScoreUpdate: (Int) -> Unit,
    onTimeUpdate: (Int) -> Unit, 
    onGameEnd: (Int) -> Unit
) {
    val holes = remember { mutableStateListOf<HoleState>().apply { 
        repeat(9) { add(HoleState(it)) }
    } }

    val scope = rememberCoroutineScope()

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            onTimeUpdate(timeLeft - 1)
            if (timeLeft - 1 == 0) {
                onGameEnd(score)
            }
        }
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
        PixelDivider(modifier = Modifier.padding(top = 12.dp, bottom = 12.dp))

        if (difficulty == DojoDifficulty.INFERNAL) {
            Text("¡${stringResource(R.string.mode_infernal)}!", color = Color(0xFFE53935), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        RetroMenuBox(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth(),
            backgroundColor = Color(0xFF4E342E), // Marrón tierra más oscuro
            borderColor = Color(0xFF3E2723)
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
                                onScoreUpdate(score + holes[index].type.score)
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
            .border(2.dp, Color.Black, CircleShape)
            .padding(1.dp)
            .border(2.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
            .clip(CircleShape)
            .background(Color(0xFF3E2723))
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
    val (rank, reward) = when (difficulty) {
        DojoDifficulty.INFERNAL -> when {
            score >= 1000 -> stringResource(R.string.dojo_rank_gold) to 750
            score >= 600 -> stringResource(R.string.dojo_rank_silver) to 350
            score >= 200 -> stringResource(R.string.dojo_rank_bronze) to 120
            else -> stringResource(R.string.dojo_rank_none) to 0
        }
        DojoDifficulty.NORMAL -> when {
            score >= 500 -> stringResource(R.string.dojo_rank_gold) to 350
            score >= 300 -> stringResource(R.string.dojo_rank_silver) to 180
            score >= 100 -> stringResource(R.string.dojo_rank_bronze) to 60
            else -> stringResource(R.string.dojo_rank_none) to 0
        }
    }

    val difficultyStr = if (difficulty == DojoDifficulty.INFERNAL) stringResource(R.string.infernal) else stringResource(R.string.normal)

    SafariResultScreen(
        title = if (reward > 0) stringResource(R.string.dojo_session_ended_victory) else stringResource(R.string.dojo_session_ended),
        subtitle = stringResource(R.string.dojo_result_subtitle, rank),
        description = stringResource(R.string.dojo_result_desc, score, difficultyStr),
        isVictory = reward > 0,
        coinsEarned = reward,
        onRetry = onRetry,
        onExit = onExit
    )
}
