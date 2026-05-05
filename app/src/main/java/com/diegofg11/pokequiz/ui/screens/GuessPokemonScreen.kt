package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.ui.components.*
import com.diegofg11.pokequiz.ui.theme.*
import com.diegofg11.pokequiz.utils.SessionManager
import com.diegofg11.pokequiz.utils.PokemonConstants
import com.diegofg11.pokequiz.utils.SafariUtils
import com.diegofg11.pokequiz.models.GuessDifficulty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Grupos de confusión para respuestas falsas realistas
private val CONFUSION_GROUPS = listOf(
    listOf(100, 101, 92, 93, 94, 109, 110, 81, 82, 102, 103, 39, 40), // Esféricos
    listOf(23, 24, 147, 148, 95, 130, 10, 13), // Serpientes/Gusanos
    listOf(16, 17, 18, 21, 22, 83, 84, 85, 144, 145, 146, 41, 42), // Pájaros/Voladores
    listOf(11, 12, 14, 15, 46, 47, 48, 49, 123, 127), // Bichos/Arácnidos
    listOf(129, 118, 119, 116, 117, 72, 73, 90, 91, 138, 139, 140, 141, 98, 99), // Acuáticos/Cangrejos/Fósiles
    listOf(37, 38, 58, 59, 52, 53, 133, 134, 135, 136, 77, 78, 128, 111, 112, 1, 2, 3), // Cuadrúpedos Pesados/Perros/Gatos
    listOf(25, 26, 35, 36, 54, 56, 88, 89, 132, 60, 61, 62, 79, 80), // Bípedos pequeños/Amorfos
    listOf(66, 67, 68, 106, 107, 122, 124, 125, 126, 150, 63, 64, 65), // Humanoides/Luchadores
    listOf(4, 5, 6, 7, 8, 9, 31, 34, 115, 149, 151) // Bípedos con cola/Saurios
)

@Composable
fun GuessPokemonScreen(
    onNavigateBack: () -> Unit,
    onStateChange: (Boolean) -> Unit = {}
) {
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
            title = "¿CUAL ES ESTE POKÉMON?",
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
        
        val correctId = Random.nextInt(1, 152)
        currentTargetId = correctId
        
        val optionsSet = mutableSetOf<Int>()
        optionsSet.add(correctId)
        
        if (difficulty != GuessDifficulty.EASY) {
            val group = CONFUSION_GROUPS.find { it.contains(correctId) }
            if (group != null) {
                val available = group.filter { it != correctId }.shuffled()
                for (i in 0 until minOf(3, available.size)) {
                    optionsSet.add(available[i])
                }
            }
        }
        
        while (optionsSet.size < 4) {
            optionsSet.add(Random.nextInt(1, 152))
        }
        
        currentOptions = optionsSet.toList().shuffled().map { id ->
            Pair(id, PokemonConstants.KANTO_POKEMON_LIST[id - 1])
        }
    }

    LaunchedEffect(Unit) {
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

    Box(modifier = Modifier.fillMaxSize()) {
        SafariRetroHeader(
            title = "ZONA SAFARI",
            onBackClick = {
                if (sessionCoins != 0 && !isProcessing) {
                    isProcessing = true
                    SafariUtils.rewardUser(
                        scope = scope,
                        coins = sessionCoins,
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
            extraContent = {
                Box(modifier = Modifier.fillMaxWidth().padding(end = 48.dp), contentAlignment = Alignment.CenterEnd) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (difficulty != GuessDifficulty.EASY) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("SEG", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                Text("$timeLeft", color = if (timeLeft <= 2) Color.Red else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            }
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.3f)))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("COINS", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                            Text("$sessionCoins 💰", color = GoldPoke, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (difficulty == GuessDifficulty.EASY) {
                RetroText(
                    text = "¿CUAL ES ESTE POKÉMON?",
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
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
    val backgroundColor = when {
        !isRevealed -> Color.White
        isCorrect -> Color(0xFF4CAF50)
        isSelected && !isCorrect -> Color(0xFFF44336)
        else -> Color(0xFFEEEEEE)
    }
    
    val textColor = if (isRevealed && (isCorrect || isSelected)) Color.White else Color.Black

    RetroMenuBox(
        modifier = modifier
            .height(65.dp)
            .clickable(enabled = !isRevealed) { onClick() },
        backgroundColor = backgroundColor,
        borderColor = Color.Black
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = text.uppercase(),
                color = textColor,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
