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
import androidx.compose.ui.res.stringResource
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
                    globalError = context.getString(R.string.quickbattle_load_error)
                }
            } catch (e: Exception) {
                globalError = "${context.getString(R.string.connection_error_prefix)} ${e.localizedMessage}"
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
            title = stringResource(R.string.error_title),
            message = globalError ?: "",
            onDismiss = { globalError = null },
            onConfirm = { globalError = null }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (gameState != SafariGameState.START) {
                RetroHeader(
                    title = if (isInverse) stringResource(R.string.quickbattle_inverse_title) else stringResource(R.string.quickbattle_help_title),
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
                    .fillMaxWidth(),
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
                                label = stringResource(R.string.quickbattle_round),
                                value = "${roundCount + 1}/3",
                                containerColor = Color(0xFF673AB7), // Purple
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                            )
                            
                            RetroStatCard(
                                label = stringResource(R.string.quickbattle_victories),
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
                            title = stringResource(R.string.quickbattle_help_title),
                            subtitle = stringResource(R.string.quickbattle_subtitle),
                            cards = listOf(
                                DifficultyCardData(
                                    stringResource(R.string.quickbattle_classic), 
                                    stringResource(R.string.quickbattle_classic_desc), 
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
                                    stringResource(R.string.quickbattle_inverse), 
                                    stringResource(R.string.quickbattle_inverse_desc), 
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
                title = stringResource(R.string.notice_title),
                message = stringResource(R.string.exit_warning_msg, penalty),
                isError = true,
                confirmText = stringResource(R.string.abandon),
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
    val context = LocalContext.current
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
            if (isInverse) stringResource(R.string.quickbattle_question_not) else stringResource(R.string.quickbattle_question_super),
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
                                        if (isWin) context.getString(R.string.quickbattle_effect_not_good) else context.getString(R.string.quickbattle_effect_super_bad)
                                    } else {
                                        if (isWin) context.getString(R.string.quickbattle_effect_super) else context.getString(R.string.quickbattle_effect_not)
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
        text = stringResource(type.stringResId),
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
        title = if (victories >= 2) stringResource(R.string.quickbattle_master) else stringResource(R.string.dojo_session_ended),
        subtitle = stringResource(R.string.quickbattle_result_subtitle, if (isInverse) stringResource(R.string.quickbattle_inverse) else stringResource(R.string.quickbattle_classic), victories),
        description = if (isInverse) stringResource(R.string.quickbattle_result_desc_inverse) else stringResource(R.string.quickbattle_result_desc_classic),
        isVictory = victories >= 2,
        coinsEarned = reward,
        onRetry = onRetry,
        onExit = onExit
    )
}
