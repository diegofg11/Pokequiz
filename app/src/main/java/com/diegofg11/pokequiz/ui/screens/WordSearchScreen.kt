package com.diegofg11.pokequiz.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegofg11.pokequiz.ui.theme.BackgroundStart
import com.diegofg11.pokequiz.ui.theme.BackgroundMid
import com.diegofg11.pokequiz.ui.components.*
import com.diegofg11.pokequiz.ui.theme.BackgroundEnd
import com.diegofg11.pokequiz.ui.theme.GoldPoke
import com.diegofg11.pokequiz.utils.SessionManager
import com.diegofg11.pokequiz.utils.PokemonConstants
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import com.diegofg11.pokequiz.utils.SafariUtils
import com.diegofg11.pokequiz.models.WordSearchDifficulty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        val words = PokemonConstants.KANTO_POKEMON_LIST.shuffled().take(wordCount).map { it.uppercase() }
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
        initializeGame()
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

@OptIn(ExperimentalLayoutApi::class)
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
            initialValue = -15f,
            targetValue = 15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 300 + Random.nextInt(200), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "rotation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                when {
                    isSelected -> Color(0xAAFFC107) // Amber
                    isFound -> Color(0xAA4CAF50) // Green
                    else -> Color.Transparent
                },
                RoundedCornerShape(2.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char.toString(),
            color = if (isSelected || isFound) Color.Black else Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            modifier = Modifier.rotate(rotation),
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun WordSearchGame(difficulty: WordSearchDifficulty, onGameEnd: () -> Unit) {
    val scope = rememberCoroutineScope()
    
    val gridSize = 10
    val wordsToFindCount = 5
    
    var timeRemaining by remember { mutableIntStateOf(
        when (difficulty) {
            WordSearchDifficulty.NORMAL -> 60
            WordSearchDifficulty.HARD -> 45
            WordSearchDifficulty.INFERNAL -> 30
        }
    ) }
    
    var wordsToFind by remember { mutableStateOf<List<String>>(emptyList()) }
    var foundWords by remember { mutableStateOf<Set<String>>(emptySet()) }
    var grid by remember { mutableStateOf<List<List<Char>>>(emptyList()) }
    
    var isGameOver by remember { mutableStateOf(false) }
    var hasWon by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var isGridReady by remember { mutableStateOf(false) }
    var gameId by remember { mutableIntStateOf(0) }
    
    // Estado de selección
    var selectedCells by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var foundCells by remember { mutableStateOf<Set<Pair<Int, Int>>>(emptySet()) }

    // Inicializar partida
    LaunchedEffect(gameId) {
        timeRemaining = when (difficulty) {
            WordSearchDifficulty.NORMAL -> 60
            WordSearchDifficulty.HARD -> 45
            WordSearchDifficulty.INFERNAL -> 30
        }
        foundWords = emptySet()
        foundCells = emptySet()
        isGameOver = false
        hasWon = false
        isGridReady = false

        val filteredWords = (0 until 151).filter { 
            val name = PokemonConstants.getCleanPokemonName(it)
            when (difficulty) {
                WordSearchDifficulty.NORMAL -> name.length in 3..6
                WordSearchDifficulty.HARD -> name.length in 4..8
                WordSearchDifficulty.INFERNAL -> name.length >= 5
            }
        }.shuffled().take(wordsToFindCount).map { PokemonConstants.getCleanPokemonName(it) }
        
        wordsToFind = filteredWords
        
        // Generar cuadrícula
        val newGrid = Array(gridSize) { CharArray(gridSize) { ' ' } }
        
        for (word in filteredWords) {
            var placed = false
            var attempts = 0
            while (!placed && attempts < 100) {
                val isReversed = difficulty == WordSearchDifficulty.INFERNAL && Random.nextBoolean()
                val wordToPlace = if (isReversed) word.reversed() else word
                
                // Direcciones: 0=Horizontal, 1=Vertical, 2=DiagonalAbajoDerecha, 3=DiagonalArribaDerecha
                val maxDir = if (difficulty == WordSearchDifficulty.NORMAL) 1 else 3
                val dir = Random.nextInt(maxDir + 1)
                
                val startRow = Random.nextInt(gridSize)
                val startCol = Random.nextInt(gridSize)
                
                var canPlace = true
                val tempCells = mutableListOf<Pair<Int, Int>>()
                
                for (i in wordToPlace.indices) {
                    val r = startRow + when(dir) { 1 -> i; 2 -> i; 3 -> -i; else -> 0 }
                    val c = startCol + when(dir) { 0 -> i; 2 -> i; 3 -> i; else -> 0 }
                    
                    if (r !in 0 until gridSize || c !in 0 until gridSize) {
                        canPlace = false
                        break
                    }
                    if (newGrid[r][c] != ' ' && newGrid[r][c] != wordToPlace[i]) {
                        canPlace = false
                        break
                    }
                    tempCells.add(Pair(r, c))
                }
                
                if (canPlace) {
                    for (i in wordToPlace.indices) {
                        val pos = tempCells[i]
                        newGrid[pos.first][pos.second] = wordToPlace[i]
                    }
                    placed = true
                }
                attempts++
            }
        }
        
        // Rellenar espacios vacíos
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (newGrid[r][c] == ' ') {
                    newGrid[r][c] = ('A'..'Z').random()
                }
            }
        }
        
        grid = newGrid.map { it.toList() }
        isGridReady = true
    }
    
    // Temporizador
    LaunchedEffect(isGameOver, isGridReady) {
        if (!isGameOver && isGridReady) {
            while (timeRemaining > 0) {
                delay(1000L)
                timeRemaining--
            }
            if (timeRemaining <= 0) {
                isGameOver = true
                hasWon = false
                showResultDialog = true
            }
        }
    }
    
    // Comprobar victoria
    LaunchedEffect(foundWords) {
        if (foundWords.size == wordsToFindCount && wordsToFindCount > 0 && !isGameOver) {
            isGameOver = true
            hasWon = true
            
            // Dar recompensa
            val reward = when (difficulty) {
                WordSearchDifficulty.NORMAL -> 30
                WordSearchDifficulty.HARD -> 60
                WordSearchDifficulty.INFERNAL -> 120
            }
            scope.launch {
                try {
                    Network.api.rewardUser(RewardRequest(
                        userId = SessionManager.currentUserId,
                        levelId = 0,
                        coinsEarned = reward
                    ))
                } catch (e: Exception) {
                    // Ignore for now
                }
            }
            showResultDialog = true
        }
    }

    RetroBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Header Retro
        SafariRetroHeader(
            title = "SOPA DE LETRAS",
            onBackClick = onGameEnd,
            extraContent = {
                Box(modifier = Modifier.fillMaxWidth().padding(end = 48.dp), contentAlignment = Alignment.CenterEnd) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TIME", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                            Text("00:${timeRemaining.toString().padStart(2, '0')}", color = if (timeRemaining <= 10) Color.Red else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.3f)))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("WORDS", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                            Text("${foundWords.size}/$wordsToFindCount", color = GoldPoke, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        )

            PixelDivider(modifier = Modifier.padding(top = 12.dp, bottom = 12.dp))
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Words List
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                wordsToFind.forEach { word ->
                    Text(
                        text = word,
                        color = if (foundWords.contains(word)) Color.Gray else Color.White,
                        textDecoration = if (foundWords.contains(word)) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                        fontWeight = if (foundWords.contains(word)) FontWeight.Normal else FontWeight.Bold,
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Grid
            if (grid.isNotEmpty()) {
                var gridWidth by remember { mutableStateOf(0f) }
                var gridHeight by remember { mutableStateOf(0f) }
                
                RetroMenuBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .onGloballyPositioned { layoutCoordinates ->
                            gridWidth = layoutCoordinates.size.width.toFloat()
                            gridHeight = layoutCoordinates.size.height.toFloat()
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (isGameOver) return@detectDragGestures
                                    val cellWidth = gridWidth / gridSize
                                    val cellHeight = gridHeight / gridSize
                                    val col = (offset.x / cellWidth).toInt().coerceIn(0, gridSize - 1)
                                    val row = (offset.y / cellHeight).toInt().coerceIn(0, gridSize - 1)
                                    selectedCells = listOf(Pair(row, col))
                                },
                                onDrag = { change, _ ->
                                    if (isGameOver || selectedCells.isEmpty()) return@detectDragGestures
                                    val cellWidth = gridWidth / gridSize
                                    val cellHeight = gridHeight / gridSize
                                    val col = (change.position.x / cellWidth).toInt().coerceIn(0, gridSize - 1)
                                    val row = (change.position.y / cellHeight).toInt().coerceIn(0, gridSize - 1)
                                    
                                    val startCell = selectedCells.first()
                                    val currentCells = mutableListOf<Pair<Int, Int>>()
                                    
                                    // Calcular el camino desde el inicio al punto actual (debe ser horizontal, vertical o diagonal)
                                    val dr = row - startCell.first
                                    val dc = col - startCell.second
                                    
                                    val steps = maxOf(kotlin.math.abs(dr), kotlin.math.abs(dc))
                                    if (steps > 0) {
                                        val stepR = dr / steps
                                        val stepC = dc / steps
                                        
                                        // Solo permitir líneas rectas
                                        if ((dr == 0 || dc == 0 || kotlin.math.abs(dr) == kotlin.math.abs(dc)) && 
                                            (kotlin.math.abs(stepR) <= 1 && kotlin.math.abs(stepC) <= 1)) {
                                            for (i in 0..steps) {
                                                currentCells.add(Pair(startCell.first + i * stepR, startCell.second + i * stepC))
                                            }
                                            selectedCells = currentCells
                                        }
                                    } else {
                                        selectedCells = listOf(startCell)
                                    }
                                },
                                onDragEnd = {
                                    if (isGameOver) return@detectDragGestures
                                    val wordFormed = selectedCells.map { grid[it.first][it.second] }.joinToString("")
                                    val reversedWordFormed = wordFormed.reversed()
                                    
                                    val matchedWord = wordsToFind.find { it == wordFormed || it == reversedWordFormed }
                                    
                                    if (matchedWord != null && !foundWords.contains(matchedWord)) {
                                        foundWords = foundWords + matchedWord
                                        foundCells = foundCells + selectedCells.toSet()
                                    }
                                    selectedCells = emptyList()
                                }
                            )
                        },
                    backgroundColor = Color.White.copy(alpha = 0.1f),
                    borderColor = GoldPoke
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        for (r in 0 until gridSize) {
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                for (c in 0 until gridSize) {
                                    val isSelected = selectedCells.contains(Pair(r, c))
                                    val isFound = foundCells.contains(Pair(r, c))
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    ) {
                                        WordSearchCell(
                                            char = grid[r][c],
                                            isSelected = isSelected,
                                            isFound = isFound,
                                            difficulty = difficulty
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onGameEnd,
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Abandonar", color = Color.White)
            }
        }
    }
    
    if (showResultDialog) {
        SafariResultScreen(
            title = if (hasWon) "¡VICTORIA!" else "DERROTA",
            subtitle = "SOPA DE LETRAS - ${difficulty.name}",
            description = if (hasWon) 
                "¡Increíble! Has encontrado todos los Pokémon ocultos." 
                else "Las palabras estaban bien escondidas... ¡Inténtalo de nuevo!",
            isVictory = hasWon,
            coinsEarned = if (hasWon) (when(difficulty) {
                WordSearchDifficulty.NORMAL -> 30
                WordSearchDifficulty.HARD -> 60
                WordSearchDifficulty.INFERNAL -> 120
            }) else -20,
            onRetry = {
                gameId++
                showResultDialog = false
            },
            onExit = onGameEnd
        )
    }
}
