/**
 * @authors: Gaizka, Diego y Xiker
 * Pantalla del minijuego de Memoria (Memorama).
 * El usuario debe emparejar cartas con sprites de Pokémon.
 * Modo Normal: vidas limitadas. Modo Infernal: tiempo limitado y cartas se mezclan.
 */
package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.ui.components.*
import com.diegofg11.pokequiz.ui.theme.*
import com.diegofg11.pokequiz.utils.SafariUtils
import com.diegofg11.pokequiz.models.MemoryDifficulty
import com.diegofg11.pokequiz.models.MemoryCardData
import com.diegofg11.pokequiz.models.MinigamePokemon
import com.diegofg11.pokequiz.api.Network
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.diegofg11.pokequiz.R

@Composable
fun MemoryGameScreen(
    onNavigateBack: () -> Unit,
    onStateChange: (Boolean) -> Unit = {}
) {
    var difficulty by remember { mutableStateOf<MemoryDifficulty?>(null) }

    LaunchedEffect(difficulty) {
        onStateChange(difficulty == null)
    }
    
    if (difficulty == null) {
        SafariSelectionScreen(
            title = stringResource(R.string.memorama),
            subtitle = stringResource(R.string.select_mode),
            cards = listOf(
                DifficultyCardData(stringResource(R.string.normal), stringResource(R.string.memory_desc_normal), "-20", "80", Color(0xFF4CAF50), { difficulty = MemoryDifficulty.NORMAL }),
                DifficultyCardData(stringResource(R.string.infernal), stringResource(R.string.memory_desc_infernal), "-50", "200", Color(0xFFE53935), { difficulty = MemoryDifficulty.INFERNAL })
            )
        )
    } else {
        MemoryGameBoard(
            difficulty = difficulty!!,
            onNavigateBack = { difficulty = null }
        )
    }
}

@Composable
fun MemoryGameBoard(difficulty: MemoryDifficulty, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val cards = remember { mutableStateListOf<MemoryCardData>() }
    var selectedIndices by remember { mutableStateOf<List<Int>>(emptyList()) }
    var lives by remember { mutableIntStateOf(5) }
    var isProcessing by remember { mutableStateOf(false) }
    
    var globalError by remember { mutableStateOf<String?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }
    var showExitWarning by remember { mutableStateOf(false) }
    var hasWon by remember { mutableStateOf(false) }
    
    var gameStarted by remember { mutableStateOf(false) }
    
    val winReward = if (difficulty == MemoryDifficulty.INFERNAL) 200 else 80
    val losePenalty = if (difficulty == MemoryDifficulty.INFERNAL) 50 else 20
    val maxTime = 20
    var timeLeft by remember { mutableIntStateOf(maxTime) }
    var flashTimer by remember { mutableStateOf(false) }
    
    var pokemonList by remember { mutableStateOf<List<MinigamePokemon>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun initializeGame() {
        if (pokemonList.isEmpty()) return
        
        cards.clear()
        cards.addAll(SafariUtils.generateMemoryDeck(pokemonList.map { it.id }, 6))
        lives = if (difficulty == MemoryDifficulty.INFERNAL) 999 else 5
        timeLeft = maxTime
        selectedIndices = emptyList()
        isProcessing = false
        gameStarted = true
        hasWon = false
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = Network.api.getMinigamePokemon(limit = 151)
                if (response.isSuccessful && response.body() != null) {
                    pokemonList = response.body()!!
                    isLoading = false
                    initializeGame()
                } else {
                    globalError = context.getString(R.string.memory_load_error)
                }
            } catch (e: Exception) {
                globalError = "${context.getString(R.string.connection_error_prefix)} ${e.localizedMessage}"
            }
        }
    }
    
    LaunchedEffect(gameStarted, lives, hasWon) {
        if (difficulty == MemoryDifficulty.INFERNAL && gameStarted && lives > 0 && !hasWon) {
            while (timeLeft > 0) {
                delay(1000)
                if (lives > 0 && !hasWon) {
                    timeLeft -= 1
                    if (timeLeft <= 5) {
                        flashTimer = !flashTimer
                    }
                }
            }
            if (timeLeft == 0 && lives > 0 && !hasWon) {
                lives = 0
                isProcessing = true
                hasWon = false
                SafariUtils.rewardUser(
                    scope = scope,
                    coins = -losePenalty,
                    gameType = "memory",
                    difficulty = difficulty.name,
                    onSuccess = { showResultDialog = true },
                    onError = { 
                        globalError = it
                        isProcessing = false
                    }
                )
            }
        }
    }

    if (globalError != null) {
        PokemonAlertDialog(
            title = stringResource(R.string.error_title),
            message = globalError!!,
            isError = true,
            onDismiss = { globalError = null }
        )
    }

    fun handleCardClick(index: Int) {
        if (isProcessing || cards[index].isFlipped || cards[index].isMatched || lives <= 0) return
        
        cards[index] = cards[index].copy(isFlipped = true)
        selectedIndices = selectedIndices + index
        
        if (selectedIndices.size == 2) {
            isProcessing = true
            scope.launch {
                delay(800)
                
                val idx1 = selectedIndices[0]
                val idx2 = selectedIndices[1]
                
                if (cards[idx1].pokemonId == cards[idx2].pokemonId) {
                    cards[idx1] = cards[idx1].copy(isMatched = true)
                    cards[idx2] = cards[idx2].copy(isMatched = true)
                    
                    if (difficulty == MemoryDifficulty.INFERNAL && !cards.all { it.isMatched }) {
                        val unmatchedCards = cards.filter { !it.isMatched }.shuffled()
                        var unmatchIdx = 0
                        for (i in cards.indices) {
                            if (!cards[i].isMatched) {
                                cards[i] = unmatchedCards[unmatchIdx++]
                            }
                        }
                    }
                    
                    if (cards.all { it.isMatched }) {
                        hasWon = true
                        SafariUtils.rewardUser(
                            scope = scope,
                            coins = winReward,
                            gameType = "memory",
                            difficulty = difficulty.name,
                            onSuccess = { showResultDialog = true },
                            onError = { 
                                globalError = it
                                isProcessing = false
                            }
                        )
                    } else {
                        isProcessing = false
                    }
                } else {
                    cards[idx1] = cards[idx1].copy(isFlipped = false)
                    cards[idx2] = cards[idx2].copy(isFlipped = false)
                    
                    if (difficulty == MemoryDifficulty.NORMAL) {
                        lives -= 1
                    }
                    
                    if (lives <= 0 && difficulty == MemoryDifficulty.NORMAL) {
                        hasWon = false
                        SafariUtils.rewardUser(
                            scope = scope,
                            coins = -losePenalty,
                            gameType = "memory",
                            difficulty = difficulty.name,
                            onSuccess = { showResultDialog = true },
                            onError = { 
                                globalError = it
                                isProcessing = false
                            }
                        )
                    } else {
                        isProcessing = false
                    }
                }
                selectedIndices = emptyList()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RetroHeader(
            title = if (difficulty == MemoryDifficulty.INFERNAL) stringResource(R.string.mode_infernal) else stringResource(R.string.memorama),
            isSafariStyle = true,
            onBackClick = {
                if (gameStarted && !hasWon && lives > 0 && !isProcessing) {
                    showExitWarning = true
                } else if (!isProcessing) {
                    onNavigateBack()
                }
            },
            rightContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (difficulty == MemoryDifficulty.INFERNAL) "∞" else "$lives",
                        color = if (lives <= 2 && difficulty != MemoryDifficulty.INFERNAL) Color(0xFFFF5252) else Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Favorite, contentDescription = stringResource(R.string.desc_lives), tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
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
                    .padding(top = 120.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (difficulty == MemoryDifficulty.INFERNAL) {
                    val timerProgress by animateFloatAsState(
                        targetValue = timeLeft.toFloat() / maxTime.toFloat(),
                        label = "timer",
                        animationSpec = tween(1000)
                    )
                    
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { timerProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(androidx.compose.ui.graphics.RectangleShape)
                                .border(2.dp, Color.White, androidx.compose.ui.graphics.RectangleShape),
                            color = if (timeLeft <= 5) (if (flashTimer) Color.Red else Color.Yellow) else Color(0xFF4CAF50),
                            trackColor = Color.DarkGray
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        userScrollEnabled = false,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(cards.size) { index ->
                            MemoryCard(
                                cardData = cards[index],
                                onClick = { handleCardClick(index) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showResultDialog) {
        SafariResultScreen(
            title = if (hasWon) stringResource(R.string.victory) else stringResource(R.string.defeat),
            subtitle = "${stringResource(R.string.memorama)} - ${difficulty.name}",
            description = if (hasWon) 
                stringResource(R.string.memory_victory_desc) 
                else stringResource(R.string.memory_defeat_desc),
            isVictory = hasWon,
            coinsEarned = if (hasWon) winReward else -losePenalty,
            onRetry = {
                initializeGame()
                showResultDialog = false
            },
            onExit = onNavigateBack
        )
    }

    if (showExitWarning) {
        PokemonAlertDialog(
            title = stringResource(R.string.notice_title),
            message = stringResource(R.string.exit_warning_msg, losePenalty),
            isError = true,
            confirmText = stringResource(R.string.abandon),
            onConfirm = {
                showExitWarning = false
                isProcessing = true
                SafariUtils.rewardUser(
                    scope = scope,
                    coins = -losePenalty,
                    gameType = "memory",
                    difficulty = difficulty.name,
                    onSuccess = { onNavigateBack() },
                    onError = { onNavigateBack() }
                )
            },
            onDismiss = { showExitWarning = false }
        )
    }
}

@Composable
fun MemoryCard(cardData: MemoryCardData, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (cardData.isFlipped || cardData.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 400)
    )
    
    val isFrontVisible = rotation > 90f

    Box(
        modifier = Modifier
            .aspectRatio(0.75f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !cardData.isMatched) { onClick() }
    ) {
        if (isFrontVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .border(3.dp, Color.Black, androidx.compose.ui.graphics.RectangleShape)
                    .padding(2.dp)
                    .background(if (cardData.isMatched) Color(0xFFE8F5E9) else Color.White, androidx.compose.ui.graphics.RectangleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${cardData.pokemonId}.png",
                    contentDescription = stringResource(R.string.desc_pokemon_sprite),
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    contentScale = ContentScale.Fit
                )
                if (cardData.isMatched) {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0x664CAF50)))
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(3.dp, Color.Black, androidx.compose.ui.graphics.RectangleShape)
                    .padding(2.dp)
                    .background(Color(0xFFE53935), androidx.compose.ui.graphics.RectangleShape),
                contentAlignment = Alignment.Center
            ) {
                // Pokéball pattern
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(30.dp, 15.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp)))
                    Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.Black.copy(alpha = 0.2f)))
                    Box(modifier = Modifier.size(30.dp, 15.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(bottomStart = 15.dp, bottomEnd = 15.dp)))
                }
                
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .border(2.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}
