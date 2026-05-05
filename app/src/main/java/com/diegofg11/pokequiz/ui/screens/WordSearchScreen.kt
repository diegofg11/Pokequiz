package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegofg11.pokequiz.ui.components.*
import com.diegofg11.pokequiz.ui.theme.*
import com.diegofg11.pokequiz.utils.SafariUtils
import com.diegofg11.pokequiz.models.WordSearchDifficulty
import com.diegofg11.pokequiz.models.MinigamePokemon
import com.diegofg11.pokequiz.api.Network
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

@Composable
fun WordSearchScreen(
    onNavigateBack: () -> Unit,
    onStateChange: (Boolean) -> Unit = {}
) {
    var difficulty by remember { mutableStateOf<WordSearchDifficulty?>(null) }
    var globalError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(difficulty) {
        onStateChange(difficulty == null)
    }

    if (globalError != null) {
        PokemonAlertDialog(
            title = "Error",
            message = globalError ?: "",
            onDismiss = { globalError = null },
            onConfirm = { globalError = null }
        )
    }

    if (difficulty == null) {
        SafariSelectionScreen(
            title = "SOPA DE LETRAS",
            subtitle = "Encuentra los Pokémon ocultos",
            cards = listOf(
                DifficultyCardData("NORMAL", "8x8 | 3 Palabras", "-20", "60", Color(0xFF4CAF50), { difficulty = WordSearchDifficulty.NORMAL }),
                DifficultyCardData("DIFÍCIL", "10x10 | 5 Palabras", "-40", "120", Color(0xFFFF9800), { difficulty = WordSearchDifficulty.HARD }),
                DifficultyCardData("INFERNAL", "¡Caos Visual! | 10x10", "-60", "250", Color(0xFFE53935), { difficulty = WordSearchDifficulty.INFERNAL }, span = 2)
            )
        )
    } else {
        WordSearchGame(
            difficulty = difficulty!!,
            onNavigateBack = { difficulty = null },
            onError = { globalError = it }
        )
    }
}

@Composable
fun WordSearchGame(
    difficulty: WordSearchDifficulty,
    onNavigateBack: () -> Unit,
    onError: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    val gridSize = if (difficulty == WordSearchDifficulty.NORMAL) 8 else 10
    val wordCount = when(difficulty) {
        WordSearchDifficulty.NORMAL -> 3
        WordSearchDifficulty.HARD -> 5
        WordSearchDifficulty.INFERNAL -> 6
    }
    
    var grid by remember { mutableStateOf<List<List<Char>>>(emptyList()) }
    var targetWords by remember { mutableStateOf<List<String>>(emptyList()) }
    var foundWords = remember { mutableStateListOf<String>() }
    var selectedCells = remember { mutableStateListOf<Pair<Int, Int>>() }
    
    var timeLeft by remember { mutableIntStateOf(120) }
    var isProcessing by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var hasWon by remember { mutableStateOf(false) }
    
    var pokemonList by remember { mutableStateOf<List<MinigamePokemon>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val rewardWin = when(difficulty) {
        WordSearchDifficulty.NORMAL -> 60
        WordSearchDifficulty.HARD -> 120
        WordSearchDifficulty.INFERNAL -> 250
    }
    
    val cost = when(difficulty) {
        WordSearchDifficulty.NORMAL -> 20
        WordSearchDifficulty.HARD -> 40
        WordSearchDifficulty.INFERNAL -> 60
    }

    fun initializeGame() {
        if (pokemonList.isEmpty()) return
        
        val words = pokemonList.shuffled().take(wordCount).map { it.nombre.uppercase()
            .replace("♂", "")
            .replace("♀", "")
            .replace("'", "")
            .replace(".", "")
            .replace(" ", "") 
        }
        targetWords = words
        foundWords.clear()
        selectedCells.clear()
        
        grid = SafariUtils.generateWordSearchGrid(
            gridSize = gridSize,
            words = words,
            allowReverse = difficulty != WordSearchDifficulty.NORMAL,
            maxDirections = if (difficulty == WordSearchDifficulty.NORMAL) 1 else 3
        )
        
        timeLeft = if (difficulty == WordSearchDifficulty.INFERNAL) 60 else 120
        isProcessing = false
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
                    onError("No se pudo cargar la lista de Pokémon.")
                }
            } catch (e: Exception) {
                onError("Error de conexión: ${e.localizedMessage}")
            }
        }
    }

    LaunchedEffect(timeLeft, hasWon) {
        if (timeLeft > 0 && !hasWon) {
            delay(1000)
            timeLeft -= 1
            if (timeLeft == 0) {
                isProcessing = true
                hasWon = false
                SafariUtils.rewardUser(
                    scope = scope,
                    coins = -cost,
                    onSuccess = { showResultDialog = true },
                    onError = { 
                        onError(it)
                        isProcessing = false
                    }
                )
            }
        }
    }

    if (showResultDialog) {
        SafariResultScreen(
            title = if (hasWon) "¡VICTORIA!" else "TIEMPO AGOTADO",
            subtitle = "SOPA POKÉMON - ${difficulty.name}",
            description = if (hasWon) "¡Increíble! Has encontrado todos los nombres Pokémon." else "¡Casi los tienes! Inténtalo de nuevo.",
            isVictory = hasWon,
            coinsEarned = if (hasWon) rewardWin else -cost,
            onRetry = {
                initializeGame()
                showResultDialog = false
            },
            onExit = onNavigateBack
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SafariRetroHeader(
                title = "SOPA POKÉ",
                onBackClick = onNavigateBack,
                extraContent = {
                    Box(modifier = Modifier.fillMaxWidth().padding(end = 48.dp), contentAlignment = Alignment.CenterEnd) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("PALABRAS", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                Text("${foundWords.size}/${targetWords.size}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            }
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.3f)))
                            Column(horizontalAlignment = Alignment.End) {
                                Text("TIEMPO", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                Text("$timeLeft", color = if (timeLeft < 10) Color.Red else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPoke)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                RetroMenuBox(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    backgroundColor = Color.White.copy(alpha = 0.1f),
                    borderColor = Color.Black.copy(alpha = 0.3f)
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        targetWords.forEach { word ->
                            val isFound = foundWords.contains(word)
                            Text(
                                text = word,
                                color = if (isFound) Color(0xFF2D5A27) else Color.Black,
                                fontSize = 10.sp,
                                fontWeight = if (isFound) FontWeight.ExtraBold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(4.dp),
                                style = if (isFound) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else androidx.compose.ui.text.TextStyle.Default
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        grid.forEachIndexed { r, row ->
                            Row(modifier = Modifier.weight(1f)) {
                                row.forEachIndexed { c, char ->
                                    WordSearchCell(
                                        char = char,
                                        isHighlighted = selectedCells.contains(Pair(r, c)),
                                        isInfernal = difficulty == WordSearchDifficulty.INFERNAL,
                                        modifier = Modifier.weight(1f).fillMaxHeight()
                                    ) {
                                        if (!isProcessing) {
                                            val cell = Pair(r, c)
                                            if (selectedCells.contains(cell)) {
                                                selectedCells.remove(cell)
                                            } else {
                                                selectedCells.add(cell)
                                                val word = selectedCells.map { grid[it.first][it.second] }.joinToString("")
                                                val reversedWord = word.reversed()
                                                
                                                targetWords.find { !foundWords.contains(it) && (word == it || reversedWord == it) }?.let { found ->
                                                    foundWords.add(found)
                                                    selectedCells.clear()
                                                    if (foundWords.size == targetWords.size) {
                                                        hasWon = true
                                                        isProcessing = true
                                                        SafariUtils.rewardUser(
                                                            scope = scope,
                                                            coins = rewardWin,
                                                            onSuccess = { showResultDialog = true },
                                                            onError = { 
                                                                onError(it)
                                                                isProcessing = false
                                                            }
                                                        )
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { selectedCells.clear() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.1f)),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("LIMPIAR SELECCIÓN", fontSize = 10.sp, color = Color.Black, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = { content() }
    )
}

@Composable
fun WordSearchCell(
    char: Char,
    isHighlighted: Boolean,
    isInfernal: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cell_anim")
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isHighlighted) Color(0xFF4CAF50).copy(alpha = 0.6f) else Color.Transparent,
        animationSpec = tween(200)
    )

    val infernalScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val infernalColor by infiniteTransition.animateColor(
        initialValue = Color.Black,
        targetValue = Color.Red,
        animationSpec = infiniteRepeatable(
            animation = tween(200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color"
    )

    Box(
        modifier = modifier
            .clickable { onClick() }
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char.toString(),
            fontSize = if (isInfernal) 18.sp else 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = if (isInfernal) infernalColor else Color.Black,
            modifier = if (isInfernal) Modifier.graphicsLayer(scaleX = infernalScale, scaleY = infernalScale) else Modifier
        )
    }
}
