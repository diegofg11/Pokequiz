package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.ui.components.*
import com.diegofg11.pokequiz.ui.theme.*
import com.diegofg11.pokequiz.R
import com.diegofg11.pokequiz.utils.SoundManager
import androidx.compose.ui.platform.LocalContext
import com.diegofg11.pokequiz.utils.SafariUtils
import com.diegofg11.pokequiz.models.*
import com.diegofg11.pokequiz.api.Network
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random


@Composable
fun GuessPokemonScreen(
    onNavigateBack: () -> Unit,
    onStateChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var difficulty by remember { mutableStateOf<GuessDifficulty?>(null) }

    var globalError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(difficulty) {
        onStateChange(difficulty == null)
    }

    if (globalError != null) {
        PokemonAlertDialog(
            title = "¡Error!",
            message = globalError!!,
            isError = true,
            onDismiss = { globalError = null }
        )
    }

    if (difficulty == null) {
        SafariSelectionScreen(
            title = "¿CUÁL ES ESTE POKÉMON?",
            subtitle = "Selecciona un modo para empezar",
            cards = listOf(
                DifficultyCardData("FÁCIL", "Sin límites", "-30", "15", Color(0xFF4CAF50), { difficulty = GuessDifficulty.EASY }),
                DifficultyCardData("DIFÍCIL", "5s | Rotado", "-40", "20", Color(0xFFFF9800), { difficulty = GuessDifficulty.HARD }),
                DifficultyCardData("INFERNAL", "4s | Caos Visual", "-80", "40", Color(0xFFE53935), { difficulty = GuessDifficulty.INFERNAL }, span = 2)
            )
        )
    } else {
        GuessPokemonGame(
            difficulty = difficulty!!,
            onNavigateBack = { difficulty = null },
            onError = { globalError = it }
        )
    }
}

@Composable
fun GuessPokemonGame(difficulty: GuessDifficulty, onNavigateBack: () -> Unit, onError: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    
    var currentTargetId by remember { mutableIntStateOf(1) }
    var currentOptions by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    
    // Silhouette transformation state
    var imageRotation by remember { mutableFloatStateOf(0f) }
    var isFlipped by remember { mutableStateOf(false) }
    var stretchX by remember { mutableFloatStateOf(1f) }
    var stretchY by remember { mutableFloatStateOf(1f) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    
    var isRevealed by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf<Int?>(null) }
    var sessionCoins by remember { mutableIntStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    val maxTime = if (difficulty == GuessDifficulty.INFERNAL) 4 else 5
    var timeLeft by remember { mutableIntStateOf(maxTime) }
    var flashTimer by remember { mutableStateOf(false) }
    
    var showRewardDialog by remember { mutableStateOf(false) }

    val rewardWin = when(difficulty) {
        GuessDifficulty.EASY -> 15
        GuessDifficulty.HARD -> 20
        GuessDifficulty.INFERNAL -> 40
    }
    
    val penaltyLose = when(difficulty) {
        GuessDifficulty.EASY -> 30
        GuessDifficulty.HARD -> 40
        GuessDifficulty.INFERNAL -> 80
    }

    fun generateNewRound() {
        isRevealed = false
        selectedId = null
        isProcessing = false
        timeLeft = maxTime
        flashTimer = false
        
        when (difficulty) {
            GuessDifficulty.INFERNAL -> {
                imageRotation = Random.nextFloat() * 360f
                isFlipped = Random.nextBoolean()
                stretchX = Random.nextFloat() * 0.6f + 0.7f
                stretchY = Random.nextFloat() * 0.6f + 0.7f
                zoom = Random.nextFloat() * 1.8f + 0.7f
                offsetX = Random.nextFloat() * 160f - 80f
                offsetY = Random.nextFloat() * 160f - 80f
            }
            GuessDifficulty.HARD -> {
                imageRotation = Random.nextFloat() * 360f
                isFlipped = false
                stretchX = 1f
                stretchY = 1f
                zoom = 1f
                offsetX = 0f
                offsetY = 0f
            }
            GuessDifficulty.EASY -> {
                imageRotation = 0f
                isFlipped = false
                stretchX = 1f
                stretchY = 1f
                zoom = 1f
                offsetX = 0f
                offsetY = 0f
            }
        }
        
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    Network.api.getGuessRound(difficulty.name)
                }
                val resp: com.diegofg11.pokequiz.models.GuessRoundResponse? = response.body()
                if (response.isSuccessful && resp != null) {
                    currentTargetId = resp.targetId
                    currentOptions = resp.options.map { it.id to it.name }
                } else {
                    onError("No se pudo cargar la siguiente ronda.")
                }
            } catch (e: Exception) {
                onError("Error de conexión.")
            }
        }
    }

    LaunchedEffect(Unit) {
        isLoading = false
        generateNewRound()
    }
    
    LaunchedEffect(currentTargetId, isRevealed) {
        if (difficulty != GuessDifficulty.EASY && !isRevealed && !isProcessing) {
            while (timeLeft > 0) {
                delay(1000)
                if (!isRevealed && !isProcessing) {
                    timeLeft -= 1
                    if (timeLeft <= 2) {
                        flashTimer = !flashTimer
                    }
                }
            }
            if (!isRevealed && !isProcessing && timeLeft == 0) {
                isProcessing = true
                isRevealed = true
                selectedId = -1
                sessionCoins -= penaltyLose
                
                scope.launch {
                    delay(2000)
                    generateNewRound()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RetroHeader(isSafariStyle = true, 
            title = "¿CUÁL ES ESTE POKÉMON?",
            onBackClick = {
                if (sessionCoins != 0 && !isProcessing) {
                    isProcessing = true
                    SafariUtils.rewardUser(
                        scope = scope,
                        coins = sessionCoins,
                        gameType = "guess",
                        difficulty = difficulty.name,
                        onSuccess = { showRewardDialog = true },
                        onError = { 
                            onError(it)
                            isProcessing = false
                        }
                    )
                } else if (!isProcessing) {
                    onNavigateBack()
                }
            },
            rightContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$sessionCoins", color = GoldPoke, fontSize = 18.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("🪙", fontSize = 16.sp)
                }
            }
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldPoke)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 90.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (difficulty != GuessDifficulty.EASY && !isRevealed) {
                    RetroStatCard(
                        label = "TIEMPO",
                        value = "$timeLeft",
                        containerColor = if (timeLeft <= 2) Color(0xFFE53935) else Color(0xFF2D5A27),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

            if (difficulty == GuessDifficulty.EASY) {
                RetroText(
                    text = "¿CUÁL ES ESTE POKÉMON?",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }

            RetroMenuBox(
                modifier = Modifier.size(250.dp),
                backgroundColor = Color(0xFFF0F0F0),
                borderColor = if (isRevealed) (if(selectedId == currentTargetId) Color(0xFF4CAF50) else Color(0xFFF44336)) else Color.Black
            ) {
                Box(
                    contentAlignment = Alignment.Center, 
                    modifier = Modifier.fillMaxSize().clipToBounds()
                ) {
                    AsyncImage(
                        model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${currentTargetId}.png",
                        contentDescription = "Pokémon misterioso",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .offset(x = offsetX.dp, y = offsetY.dp)
                            .rotate(imageRotation)
                            .scale(
                                scaleX = zoom * stretchX * (if(isFlipped) -1f else 1f),
                                scaleY = zoom * stretchY
                            ),
                        contentScale = ContentScale.Fit,
                        colorFilter = if (isRevealed) null else ColorFilter.tint(Color.Black)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (currentOptions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    for (i in 0 until 4 step 2) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            repeat(2) { offset ->
                                val option = currentOptions[i + offset]
                                OptionButton(
                                    text = option.second,
                                    isSelected = selectedId == option.first,
                                    isCorrect = option.first == currentTargetId,
                                    isRevealed = isRevealed,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (!isRevealed && !isProcessing) {
                                        isProcessing = true
                                        selectedId = option.first
                                        isRevealed = true
                                        
                                        if (selectedId == currentTargetId) {
                                            sessionCoins += rewardWin
                                        } else {
                                            sessionCoins -= penaltyLose
                                        }
                                        
                                        scope.launch {
                                            delay(1500)
                                            generateNewRound()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRewardDialog) {
        SafariResultScreen(
            title = if (sessionCoins >= 0) "¡VICTORIA!" else "DERROTA",
            subtitle = "MODO ${difficulty.name}",
            description = if (sessionCoins >= 0) 
                "¡Increíble! Tienes un ojo experto para las siluetas Pokémon." 
                else "La silueta te ha confundido esta vez. ¡Sigue practicando!",
            isVictory = sessionCoins >= 0,
            coinsEarned = sessionCoins,
            onRetry = {
                sessionCoins = 0
                generateNewRound()
                showRewardDialog = false
            },
            onExit = onNavigateBack
        )
    }
}
}

@Composable
fun OptionButton(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isRevealed: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = when {
        !isRevealed -> Color.White
        isCorrect -> Color(0xFF4CAF50)
        isSelected && !isCorrect -> Color(0xFFF44336)
        else -> Color(0xFFEEEEEE)
    }
    
    val contentColor = if (isRevealed && (isCorrect || isSelected)) Color.White else Color.Black
    val borderColor = if (isRevealed && isCorrect) Color(0xFF1B5E20) else Color.Black

    RetroButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        borderColor = borderColor,
        fontSize = 12.sp,
        enabled = !isRevealed
    )
}
