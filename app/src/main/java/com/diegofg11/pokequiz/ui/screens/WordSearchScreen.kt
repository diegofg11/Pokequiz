/**
 * @authors: Gaizka, Diego y Xiker
 * Pantalla del minijuego Sopa de Letras Pokémon.
 * El usuario arrastra para seleccionar nombres de Pokémon ocultos en una cuadrícula de letras.
 */
package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
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
            title = "SOPA POKÉ",
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
    
    // Estados para el arrastre (drag)
    var dragStartCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var dragCurrentCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var gridWidthPx by remember { mutableFloatStateOf(0f) }

    fun getCellFromOffset(offset: Offset): Pair<Int, Int>? {
        if (gridWidthPx <= 0) return null
        val cellSize = gridWidthPx / gridSize
        val col = (offset.x / cellSize).toInt().coerceIn(0, gridSize - 1)
        val row = (offset.y / cellSize).toInt().coerceIn(0, gridSize - 1)
        return Pair(row, col)
    }

    fun calculateLinePath(start: Pair<Int, Int>, end: Pair<Int, Int>): List<Pair<Int, Int>> {
        var dr = end.first - start.first
        var dc = end.second - start.second
        
        // Forzar línea recta (horizontal, vertical o diagonal 45º)
        if (dr != 0 && dc != 0 && Math.abs(dr) != Math.abs(dc)) {
            if (Math.abs(dr) > Math.abs(dc)) dc = 0 else dr = 0
        }
        
        val steps = Math.max(Math.abs(dr), Math.abs(dc))
        if (steps == 0) return listOf(start)
        
        val path = mutableListOf<Pair<Int, Int>>()
        val stepR = if (dr == 0) 0 else dr / Math.abs(dr)
        val stepC = if (dc == 0) 0 else dc / Math.abs(dc)
        
        for (i in 0..steps) {
            path.add(Pair(start.first + stepR * i, start.second + stepC * i))
        }
        return path
    }

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
                    gameType = "wordsearch",
                    difficulty = difficulty.name,
                    onSuccess = { showResultDialog = true },
                    onError = { 
                        onError(it)
                        isProcessing = false
                    }
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
        RetroHeader(
            title = "SOPA POKÉ",
            onBackClick = onNavigateBack
        )

        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldPoke)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large Stats Bar outside header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RetroStatCard(
                        label = "PALABRAS",
                        value = "${foundWords.size}/${targetWords.size}",
                        containerColor = Color(0xFF1B76D2), // Blueish
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    )
                    
                    RetroStatCard(
                        label = "TIEMPO",
                        value = "$timeLeft",
                        containerColor = if (timeLeft < 10) Color(0xFFE53935) else Color(0xFF2D5A27),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    )
                }

                RetroMenuBox(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    backgroundColor = Color.White.copy(alpha = 0.1f),
                    borderColor = Color.Black.copy(alpha = 0.3f)
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        targetWords.forEach { word ->
                            val isFound = foundWords.contains(word)
                            Surface(
                                shape = androidx.compose.ui.graphics.RectangleShape,
                                color = if (isFound) Color(0xFF2D5A27) else Color.White.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, if (isFound) Color.White else Color.Black.copy(alpha = 0.3f)),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = word,
                                    color = if (isFound) Color.White else Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = if (isFound) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else androidx.compose.ui.text.TextStyle.Default
                                )
                            }
                        }
                    }
                }

                RetroMenuBox(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(bottom = 16.dp),
                    backgroundColor = Color.White,
                    borderColor = Color.Black
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .onGloballyPositioned { gridWidthPx = it.size.width.toFloat() }
                                .pointerInput(gridSize, isProcessing) {
                                    if (isProcessing) return@pointerInput
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val cell = getCellFromOffset(offset)
                                            if (cell != null) {
                                                dragStartCell = cell
                                                dragCurrentCell = cell
                                                selectedCells.clear()
                                                selectedCells.add(cell)
                                            }
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val cell = getCellFromOffset(change.position)
                                            if (cell != null && cell != dragCurrentCell) {
                                                dragCurrentCell = cell
                                                dragStartCell?.let { start ->
                                                    val newPath = calculateLinePath(start, cell)
                                                    selectedCells.clear()
                                                    selectedCells.addAll(newPath)
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            if (selectedCells.isNotEmpty()) {
                                                val word = selectedCells.map { grid[it.first][it.second] }.joinToString("")
                                                val reversedWord = word.reversed()
                                                
                                                targetWords.find { !foundWords.contains(it) && (word == it || reversedWord == it) }?.let { found ->
                                                    foundWords.add(found)
                                                }
                                                
                                                if (foundWords.size == targetWords.size) {
                                                    hasWon = true
                                                    isProcessing = true
                                                    SafariUtils.rewardUser(
                                                        scope = scope,
                                                        coins = rewardWin,
                                                        gameType = "wordsearch",
                                                        difficulty = difficulty.name,
                                                        onSuccess = { showResultDialog = true },
                                                        onError = { 
                                                            onError(it)
                                                            isProcessing = false
                                                        }
                                                    )
                                                }
                                            }
                                            selectedCells.clear()
                                            dragStartCell = null
                                            dragCurrentCell = null
                                        },
                                        onDragCancel = {
                                            selectedCells.clear()
                                            dragStartCell = null
                                            dragCurrentCell = null
                                        }
                                    )
                                }
                        ) {
                            grid.forEachIndexed { r, row ->
                                Row(modifier = Modifier.weight(1f)) {
                                    row.forEachIndexed { c, char ->
                                        WordSearchCell(
                                            char = char,
                                            isHighlighted = selectedCells.contains(Pair(r, c)),
                                            isInfernal = difficulty == WordSearchDifficulty.INFERNAL,
                                            modifier = Modifier.weight(1f).fillMaxHeight(),
                                            onClick = {} // Ya no se usa clic individual
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                RetroButton(
                    text = "LIMPIAR SELECCIÓN",
                    onClick = { selectedCells.clear() },
                    modifier = Modifier.fillMaxWidth(0.7f).height(44.dp),
                    containerColor = Color.DarkGray,
                    fontSize = 12.sp
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
