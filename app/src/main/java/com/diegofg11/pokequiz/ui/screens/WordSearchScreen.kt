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
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.models.RewardRequest
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.draw.rotate
import androidx.compose.animation.core.*
import com.diegofg11.pokequiz.ui.components.PokemonAlertDialog
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid

enum class WordSearchDifficulty {
    NORMAL, HARD, INFERNAL
}

@Composable
fun WordSearchScreen(
    onNavigateBack: () -> Unit,
    onStateChange: (Boolean) -> Unit = {}
) {
    var selectedDifficulty by remember { mutableStateOf<WordSearchDifficulty?>(null) }
    var globalError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedDifficulty) {
        onStateChange(selectedDifficulty == null)
    }

    if (globalError != null) {
        PokemonAlertDialog(
            title = "Error",
            message = globalError ?: "",
            onDismiss = { globalError = null }
        )
    }

    if (selectedDifficulty == null) {
        WordSearchDifficultySelection(
            onSelect = { difficulty ->
                val cost = -20
                scope.launch {
                    try {
                        val response = Network.api.rewardUser(RewardRequest(
                            userId = com.diegofg11.pokequiz.utils.SessionManager.currentUserId,
                            levelId = 0,
                            coinsEarned = cost
                        ))
                        if (response.isSuccessful) {
                            selectedDifficulty = difficulty
                        } else {
                            globalError = "Error al cobrar la entrada. Inténtalo de nuevo."
                        }
                    } catch (e: Exception) {
                        globalError = "Error de red: ${e.localizedMessage}"
                    }
                }
            }
        )
    } else {
        WordSearchGame(
            difficulty = selectedDifficulty!!,
            onGameEnd = { selectedDifficulty = null }
        )
    }
}

@Composable
fun WordSearchDifficultySelection(onSelect: (WordSearchDifficulty) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            RetroText(
                text = "SOPA DE LETRAS",
                fontSize = 38.sp,
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

            // Grid de Selección de Dificultad
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Normal Mode
                item {
                    RetroDifficultyCard(
                        title = "NORMAL",
                        subtitle = "60s | Estándar",
                        cost = "-20",
                        reward = "+30",
                        color = Color(0xFF4CAF50),
                        onClick = { onSelect(WordSearchDifficulty.NORMAL) }
                    )
                }

                // Hard Mode
                item {
                    RetroDifficultyCard(
                        title = "DIFÍCIL",
                        subtitle = "45s | Diagonal",
                        cost = "-20",
                        reward = "+60",
                        color = Color(0xFFFF9800),
                        onClick = { onSelect(WordSearchDifficulty.HARD) }
                    )
                }

                // Infernal Mode
                item(span = { GridItemSpan(2) }) {
                    RetroDifficultyCard(
                        title = "INFERNAL",
                        subtitle = "30s | Invertidas | El reto supremo",
                        cost = "-20",
                        reward = "+120",
                        color = Color(0xFFE53935),
                        onClick = { onSelect(WordSearchDifficulty.INFERNAL) }
                    )
                }
            }
        }
    }
}

// Lista de los 151 Pokémon de Kanto
private val POKEMON_LIST = listOf(
    "BULBASAUR", "IVYSAUR", "VENUSAUR", "CHARMANDER", "CHARMELEON", "CHARIZARD", "SQUIRTLE", "WARTORTLE", "BLASTOISE", "CATERPIE",
    "METAPOD", "BUTTERFREE", "WEEDLE", "KAKUNA", "BEEDRILL", "PIDGEY", "PIDGEOTTO", "PIDGEOT", "RATTATA", "RATICATE",
    "SPEAROW", "FEAROW", "EKANS", "ARBOK", "PIKACHU", "RAICHU", "SANDSHREW", "SANDSLASH", "NIDORAN", "NIDORINA",
    "NIDOQUEEN", "NIDORINO", "NIDOKING", "CLEFAIRY", "CLEFABLE", "VULPIX", "NINETALES", "JIGGLYPUFF", "WIGGLYTUFF",
    "ZUBAT", "GOLBAT", "ODDISH", "GLOOM", "VILEPLUME", "PARAS", "PARASECT", "VENONAT", "VENOMOTH", "DIGLETT",
    "DUGTRIO", "MEOWTH", "PERSIAN", "PSYDUCK", "GOLDUCK", "MANKEY", "PRIMEAPE", "GROWLITHE", "ARCANINE", "POLIWAG",
    "POLIWHIRL", "POLIWRATH", "ABRA", "KADABRA", "ALAKAZAM", "MACHOP", "MACHOKE", "MACHAMP", "BELLSPROUT", "WEEPINBELL",
    "VICTREEBEL", "TENTACOOL", "TENTACRUEL", "GEODUDE", "GRAVELER", "GOLEM", "PONYTA", "RAPIDASH", "SLOWPOKE", "SLOWBRO",
    "MAGNEMITE", "MAGNETON", "FARFETCHD", "DODUO", "DODRIO", "SEEL", "DEWGONG", "GRIMER", "MUK", "SHELLDER",
    "CLOYSTER", "GASTLY", "HAUNTER", "GENGAR", "ONIX", "DROWZEE", "HYPNO", "KRABBY", "KINGLER", "VOLTORB",
    "ELECTRODE", "EXEGGCUTE", "EXEGGUTOR", "CUBONE", "MAROWAK", "HITMONLEE", "HITMONCHAN", "LICKITUNG", "KOFFING", "WEEZING",
    "RHYHORN", "RHYDON", "CHANSEY", "TANGELA", "KANGASKHAN", "HORSEA", "SEADRA", "GOLDEEN", "SEAKING", "STARYU",
    "STARMIE", "MRMIME", "SCYTHER", "JYNX", "ELECTABUZZ", "MAGMAR", "PINSIR", "TAUROS", "MAGIKARP", "GYARADOS",
    "LAPRAS", "DITTO", "EEVEE", "VAPOREON", "JOLTEON", "FLAREON", "PORYGON", "OMANYTE", "OMASTAR", "KABUTO",
    "KABUTOPS", "AERODACTYL", "SNORLAX", "ARTICUNO", "ZAPDOS", "MOLTRES", "DRATINI", "DRAGONAIR", "DRAGONITE", "MEWTWO",
    "MEW"
)

data class GridCell(val row: Int, val col: Int, val char: Char)

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
    
    // Selection state
    var selectedCells by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var foundCells by remember { mutableStateOf<Set<Pair<Int, Int>>>(emptySet()) }

    // Init Game
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

        val filteredWords = POKEMON_LIST.filter { 
            when (difficulty) {
                WordSearchDifficulty.NORMAL -> it.length in 3..6
                WordSearchDifficulty.HARD -> it.length in 4..8
                WordSearchDifficulty.INFERNAL -> it.length >= 5
            }
        }.shuffled().take(wordsToFindCount)
        
        wordsToFind = filteredWords
        
        // Generate grid
        val newGrid = Array(gridSize) { CharArray(gridSize) { ' ' } }
        val placedCells = mutableSetOf<Pair<Int, Int>>()
        
        for (word in filteredWords) {
            var placed = false
            var attempts = 0
            while (!placed && attempts < 100) {
                val isReversed = difficulty == WordSearchDifficulty.INFERNAL && Random.nextBoolean()
                val wordToPlace = if (isReversed) word.reversed() else word
                
                // Directions: 0=Horizontal, 1=Vertical, 2=DiagonalDownRight, 3=DiagonalUpRight
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
        
        // Fill empty spaces
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
    
    // Timer
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
    
    // Check Win
    LaunchedEffect(foundWords) {
        if (foundWords.size == wordsToFindCount && wordsToFindCount > 0 && !isGameOver) {
            isGameOver = true
            hasWon = true
            
            // Give reward
            val reward = when (difficulty) {
                WordSearchDifficulty.NORMAL -> 30
                WordSearchDifficulty.HARD -> 60
                WordSearchDifficulty.INFERNAL -> 120
            }
            scope.launch {
                try {
                    Network.api.rewardUser(RewardRequest(
                        userId = com.diegofg11.pokequiz.utils.SessionManager.currentUserId,
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
            onBackClick = onGameEnd
        )

        // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RetroText(
                    text = "00:${timeRemaining.toString().padStart(2, '0')}",
                    color = if (timeRemaining <= 10) Color.Red else Color.White,
                    fontSize = 28.sp
                )
                
                RetroText(
                    text = "${foundWords.size} / $wordsToFindCount",
                    color = GoldPoke,
                    fontSize = 22.sp
                )
            }
            
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
                                    
                                    // Calculate path from start to current (must be horizontal, vertical, or diagonal)
                                    val dr = row - startCell.first
                                    val dc = col - startCell.second
                                    
                                    val steps = maxOf(kotlin.math.abs(dr), kotlin.math.abs(dc))
                                    if (steps > 0) {
                                        val stepR = dr / steps
                                        val stepC = dc / steps
                                        
                                        // Only allow straight lines
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
                                    
                                    // Efecto Infernal: Vibración de letras
                                    val infiniteTransition = rememberInfiniteTransition()
                                    val rotation by if (difficulty == WordSearchDifficulty.INFERNAL && !isFound && !isSelected) {
                                        infiniteTransition.animateFloat(
                                            initialValue = -15f,
                                            targetValue = 15f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(durationMillis = 300 + Random.nextInt(200), easing = LinearEasing),
                                                repeatMode = RepeatMode.Reverse
                                            )
                                        )
                                    } else {
                                        remember { mutableFloatStateOf(0f) }
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
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
                                            text = grid[r][c].toString(),
                                            color = if (isSelected || isFound) Color.Black else Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            modifier = Modifier.rotate(rotation),
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
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
