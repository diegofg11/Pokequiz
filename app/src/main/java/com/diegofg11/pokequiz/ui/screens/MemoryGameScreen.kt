package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

data class MemoryCardData(
    val id: Int,
    val pokemonId: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

enum class MemoryDifficulty {
    NORMAL, INFERNAL
}

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
        MemoryDifficultySelectionScreen(
            onSelect = { difficulty = it }
        )
    } else {
        MemoryGameBoard(
            difficulty = difficulty!!,
            onNavigateBack = { difficulty = null }
        )
    }
}

@Composable
fun MemoryDifficultySelectionScreen(onSelect: (MemoryDifficulty) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            RetroText(
                text = "MEMORAMA",
                fontSize = 42.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Selecciona un modo para empezar",
                color = Color(0xFF333333),
                fontSize = 12.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
            )

            // Modo Selección de Tarjetas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Normal Mode Card
                RetroDifficultyCard(
                    title = "NORMAL",
                    subtitle = "5 Vidas | Relajado",
                    cost = "-20",
                    reward = "+80",
                    color = Color(0xFF4CAF50),
                    onClick = { onSelect(MemoryDifficulty.NORMAL) },
                    modifier = Modifier.weight(1f)
                )

                // Infernal Mode Card
                RetroDifficultyCard(
                    title = "INFERNAL",
                    subtitle = "Caótico | 20s",
                    cost = "-50",
                    reward = "+200",
                    color = Color(0xFFE53935),
                    onClick = { onSelect(MemoryDifficulty.INFERNAL) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MemoryGameBoard(difficulty: MemoryDifficulty, onNavigateBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    
    var cards by remember { mutableStateOf<List<MemoryCardData>>(emptyList()) }
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

    fun initializeGame() {
        val uniquePokemonIds = mutableSetOf<Int>()
        while (uniquePokemonIds.size < 6) { // 3x4 grid = 12 cards = 6 pairs
            uniquePokemonIds.add(Random.nextInt(1, 152))
        }
        
        val deck = mutableListOf<MemoryCardData>()
        var cardId = 0
        uniquePokemonIds.forEach { pokeId ->
            deck.add(MemoryCardData(id = cardId++, pokemonId = pokeId))
            deck.add(MemoryCardData(id = cardId++, pokemonId = pokeId))
        }
        
        cards = deck.shuffled()
        lives = if (difficulty == MemoryDifficulty.INFERNAL) 999 else 5
        timeLeft = maxTime
        selectedIndices = emptyList()
        isProcessing = false
        gameStarted = true
    }

    LaunchedEffect(Unit) {
        initializeGame()
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
                // Time over
                lives = 0
                isProcessing = true
                hasWon = false
                try {
                    Network.api.rewardUser(RewardRequest(userId = SessionManager.currentUserId, levelId = 0, coinsEarned = -losePenalty))
                    withContext(Dispatchers.Main) { showResultDialog = true }
                } catch(e: Exception) {
                    withContext(Dispatchers.Main) {
                        globalError = "Error de red al cobrar la entrada."
                        isProcessing = false
                    }
                }
            }
        }
    }

    if (globalError != null) {
        PokemonAlertDialog(
            title = "¡Error!",
            message = globalError!!,
            isError = true,
            onDismiss = { globalError = null }
        )
    }

    if (showResultDialog) {
        val title = if (hasWon) "¡VICTORIA!" else "DERROTA"
        val message = if (hasWon) "¡Has encontrado todas las parejas!\nPremio gordo: +$winReward Monedas." else "Te has quedado sin oportunidades...\nEntrada cobrada: -$losePenalty Monedas."
        
        PokemonAlertDialog(
            title = title,
            message = message,
            isError = !hasWon,
            onDismiss = { 
                showResultDialog = false
                onNavigateBack()
            }
        )
    }

    if (showExitWarning) {
        PokemonAlertDialog(
            title = "¡Atención!",
            message = "Si abandonas ahora se te cobrará la entrada de $losePenalty monedas. ¿Seguro que quieres salir?",
            isError = true,
            confirmText = "Abandonar",
            onConfirm = {
                showExitWarning = false
                isProcessing = true
                scope.launch {
                    try {
                        Network.api.rewardUser(RewardRequest(userId = SessionManager.currentUserId, levelId = 0, coinsEarned = -losePenalty))
                        withContext(Dispatchers.Main) { onNavigateBack() }
                    } catch(e: Exception) {
                        withContext(Dispatchers.Main) { onNavigateBack() }
                    }
                }
            },
            onDismiss = { showExitWarning = false }
        )
    }

    fun handleCardClick(index: Int) {
        if (isProcessing || cards[index].isFlipped || cards[index].isMatched || lives <= 0) return
        
        // Flip the card
        val newCards = cards.toMutableList()
        newCards[index] = newCards[index].copy(isFlipped = true)
        cards = newCards
        
        val newSelected = selectedIndices.toMutableList()
        newSelected.add(index)
        selectedIndices = newSelected
        
        if (selectedIndices.size == 2) {
            isProcessing = true
            scope.launch {
                delay(800) // Wait to let user see the cards
                
                val idx1 = selectedIndices[0]
                val idx2 = selectedIndices[1]
                
                var updatedCards = cards.toMutableList()
                
                if (updatedCards[idx1].pokemonId == updatedCards[idx2].pokemonId) {
                    // Match found
                    updatedCards[idx1] = updatedCards[idx1].copy(isMatched = true)
                    updatedCards[idx2] = updatedCards[idx2].copy(isMatched = true)
                    
                    // Tablero Caótico en Infernal
                    if (difficulty == MemoryDifficulty.INFERNAL && !updatedCards.all { it.isMatched }) {
                        val unmatchedCards = updatedCards.filter { !it.isMatched }.shuffled()
                        var unmatchIdx = 0
                        for (i in updatedCards.indices) {
                            if (!updatedCards[i].isMatched) {
                                updatedCards[i] = unmatchedCards[unmatchIdx++]
                            }
                        }
                    }
                    
                    cards = updatedCards
                    
                    // Check win condition
                    if (cards.all { it.isMatched }) {
                        hasWon = true
                        try {
                            Network.api.rewardUser(RewardRequest(userId = SessionManager.currentUserId, levelId = 0, coinsEarned = winReward))
                            withContext(Dispatchers.Main) { showResultDialog = true }
                        } catch(e: Exception) {
                            withContext(Dispatchers.Main) {
                                globalError = "Error de red al guardar tu premio."
                                isProcessing = false
                            }
                        }
                    } else {
                        isProcessing = false
                    }
                } else {
                    // No match
                    updatedCards[idx1] = updatedCards[idx1].copy(isFlipped = false)
                    updatedCards[idx2] = updatedCards[idx2].copy(isFlipped = false)
                    cards = updatedCards
                    
                    if (difficulty == MemoryDifficulty.NORMAL) {
                        lives -= 1
                    }
                    
                    if (lives <= 0 && difficulty == MemoryDifficulty.NORMAL) {
                        hasWon = false
                        try {
                            Network.api.rewardUser(RewardRequest(userId = SessionManager.currentUserId, levelId = 0, coinsEarned = -losePenalty))
                            withContext(Dispatchers.Main) { showResultDialog = true }
                        } catch(e: Exception) {
                            withContext(Dispatchers.Main) {
                                globalError = "Error de red al cobrar la entrada."
                                isProcessing = false
                            }
                        }
                    } else {
                        isProcessing = false
                    }
                }
                
                selectedIndices = emptyList()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Barra superior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (gameStarted && !hasWon && lives > 0 && !isProcessing) {
                        showExitWarning = true
                    } else if (!isProcessing) {
                        onNavigateBack()
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                RetroText(
                    text = if (difficulty == MemoryDifficulty.INFERNAL) "MODO INFERNAL" else "MEMORAMA",
                    color = if (difficulty == MemoryDifficulty.INFERNAL) Color(0xFFE53935) else Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                
                // Vidas
                RetroMenuBox(
                    backgroundColor = Color(0x33000000),
                    borderColor = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = "Lives", tint = Color(0xFFE53935), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (difficulty == MemoryDifficulty.INFERNAL) "∞" else "x $lives",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            // Timer Bar for Infernal
            if (difficulty == MemoryDifficulty.INFERNAL) {
                val timerColor = if (timeLeft <= 5) {
                    if (flashTimer) Color.Red else Color.Yellow
                } else {
                    Color.Green
                }
                
                val timerProgress by animateFloatAsState(
                    targetValue = timeLeft.toFloat() / maxTime.toFloat(),
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
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, Color.White, RoundedCornerShape(4.dp)),
                        color = timerColor,
                        trackColor = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cuadrícula de Cartas (3x4)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(cards.size) { index ->
                    val card = cards[index]
                    MemoryCard(
                        cardData = card,
                        onClick = { handleCardClick(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun MemoryCard(cardData: MemoryCardData, onClick: () -> Unit) {
    // Animación de rotación 3D
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
            // Cara Frontal (Pokémon)
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }, // Rotar de vuelta para que el contenido no se vea en espejo
                shape = RoundedCornerShape(8.dp),
                color = if (cardData.isMatched) Color(0xFFE8F5E9) else Color.White,
                border = androidx.compose.foundation.BorderStroke(2.dp, if (cardData.isMatched) Color(0xFF4CAF50) else Color(0xFFB0BEC5))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    AsyncImage(
                        model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${cardData.pokemonId}.png",
                        contentDescription = "Pokemon Sprite",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    if (cardData.isMatched) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x664CAF50))
                        )
                    }
                }
            }
        } else {
            // Reverso de la Carta (Pokéball pattern)
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE53935), // Rojo Pokéball
                border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFB71C1C))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    // Círculo central blanco estilo Pokéball
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = RoundedCornerShape(50),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black)
                    ) {}
                    // Círculo interno
                    Surface(
                        modifier = Modifier.size(16.dp),
                        shape = RoundedCornerShape(50),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
                    ) {}
                }
            }
        }
    }
}
