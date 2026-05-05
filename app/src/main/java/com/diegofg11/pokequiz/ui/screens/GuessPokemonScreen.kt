package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

// Lista de los 151 Pokémon de Kanto
private val KANTO_POKEMON = listOf(
    "Bulbasaur", "Ivysaur", "Venusaur", "Charmander", "Charmeleon", "Charizard", "Squirtle", "Wartortle", "Blastoise", "Caterpie", 
    "Metapod", "Butterfree", "Weedle", "Kakuna", "Beedrill", "Pidgey", "Pidgeotto", "Pidgeot", "Rattata", "Raticate", 
    "Spearow", "Fearow", "Ekans", "Arbok", "Pikachu", "Raichu", "Sandshrew", "Sandslash", "Nidoran♀", "Nidorina", 
    "Nidoqueen", "Nidoran♂", "Nidorino", "Nidoking", "Clefairy", "Clefable", "Vulpix", "Ninetales", "Jigglypuff", "Wigglytuff", 
    "Zubat", "Golbat", "Oddish", "Gloom", "Vileplume", "Paras", "Parasect", "Venonat", "Venomoth", "Diglett", 
    "Dugtrio", "Meowth", "Persian", "Psyduck", "Golduck", "Mankey", "Primeape", "Growlithe", "Arcanine", "Poliwag", 
    "Poliwhirl", "Poliwrath", "Abra", "Kadabra", "Alakazam", "Machop", "Machoke", "Machamp", "Bellsprout", "Weepinbell", 
    "Victreebel", "Tentacool", "Tentacruel", "Geodude", "Graveler", "Golem", "Ponyta", "Rapidash", "Slowpoke", "Slowbro", 
    "Magnemite", "Magneton", "Farfetch'd", "Doduo", "Dodrio", "Seel", "Dewgong", "Grimer", "Muk", "Shellder", 
    "Cloyster", "Gastly", "Haunter", "Gengar", "Onix", "Drowzee", "Hypno", "Krabby", "Kingler", "Voltorb", 
    "Electrode", "Exeggcute", "Exeggutor", "Cubone", "Marowak", "Hitmonlee", "Hitmonchan", "Lickitung", "Koffing", "Weezing", 
    "Rhyhorn", "Rhydon", "Chansey", "Tangela", "Kangaskhan", "Horsea", "Seadra", "Goldeen", "Seaking", "Staryu", 
    "Starmie", "Mr. Mime", "Scyther", "Jynx", "Electabuzz", "Magmar", "Pinsir", "Tauros", "Magikarp", "Gyarados", 
    "Lapras", "Ditto", "Eevee", "Vaporeon", "Jolteon", "Flareon", "Porygon", "Omanyte", "Omastar", "Kabuto", 
    "Kabutops", "Aerodactyl", "Snorlax", "Articuno", "Zapdos", "Moltres", "Dratini", "Dragonair", "Dragonite", "Mewtwo", 
    "Mew"
)

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

enum class Difficulty {
    EASY, HARD, INFERNAL
}

@Composable
fun GuessPokemonScreen(
    onNavigateBack: () -> Unit,
    onStateChange: (Boolean) -> Unit = {}
) {
    var difficulty by remember { mutableStateOf<Difficulty?>(null) }
    var globalError by remember { mutableStateOf<String?>(null) }

    // Notificar al contenedor si estamos en modo selección o jugando
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
        DifficultySelectionScreen(
            onSelect = { difficulty = it }
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
fun DifficultySelectionScreen(onSelect: (Difficulty) -> Unit) {
    RetroBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            RetroText(
                text = "¿QUIÉN ES ESE POKÉMON?",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Selecciona un modo para empezar",
                color = Color.LightGray,
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
                // Easy Mode
                item {
                    RetroDifficultyCard(
                        title = "FÁCIL",
                        subtitle = "Sin límites",
                        cost = "-30 💰",
                        reward = "+15 💰",
                        color = Color(0xFF4CAF50),
                        onClick = { onSelect(Difficulty.EASY) }
                    )
                }

                // Hard Mode
                item {
                    RetroDifficultyCard(
                        title = "DIFÍCIL",
                        subtitle = "5s | Rotado",
                        cost = "-40 💰",
                        reward = "+20 💰",
                        color = Color(0xFFFF9800),
                        onClick = { onSelect(Difficulty.HARD) }
                    )
                }

                // Infernal Mode
                item(span = { GridItemSpan(2) }) {
                    RetroDifficultyCard(
                        title = "INFERNAL",
                        subtitle = "4s | Caos Visual | El reto definitivo",
                        cost = "-80 💰",
                        reward = "+40 💰",
                        color = Color(0xFFE53935),
                        onClick = { onSelect(Difficulty.INFERNAL) }
                    )
                }
            }
        }
    }
}

@Composable
fun GuessPokemonGame(difficulty: Difficulty, onNavigateBack: () -> Unit, onError: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    
    var currentTargetId by remember { mutableIntStateOf(1) }
    var currentOptions by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    
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
    
    val maxTime = if (difficulty == Difficulty.INFERNAL) 4 else 5
    var timeLeft by remember { mutableIntStateOf(maxTime) }
    var flashTimer by remember { mutableStateOf(false) }
    
    var showRewardDialog by remember { mutableStateOf(false) }

    val rewardWin = when(difficulty) {
        Difficulty.EASY -> 15
        Difficulty.HARD -> 20
        Difficulty.INFERNAL -> 40
    }
    
    val penaltyLose = when(difficulty) {
        Difficulty.EASY -> 30
        Difficulty.HARD -> 40
        Difficulty.INFERNAL -> 80
    }

    fun generateNewRound() {
        isRevealed = false
        selectedId = null
        isProcessing = false
        timeLeft = maxTime
        flashTimer = false
        
        when (difficulty) {
            Difficulty.INFERNAL -> {
                imageRotation = Random.nextFloat() * 360f
                isFlipped = Random.nextBoolean()
                stretchX = Random.nextFloat() * 0.6f + 0.7f // entre 0.7 y 1.3
                stretchY = Random.nextFloat() * 0.6f + 0.7f
                zoom = Random.nextFloat() * 1.8f + 0.7f // Zoom entre 0.7x (lejos) y 2.5x (cerca)
                offsetX = Random.nextFloat() * 160f - 80f // Desplazamiento X para cortar imagen
                offsetY = Random.nextFloat() * 160f - 80f // Desplazamiento Y para cortar imagen
            }
            Difficulty.HARD -> {
                imageRotation = Random.nextFloat() * 360f
                isFlipped = false
                stretchX = 1f
                stretchY = 1f
                zoom = 1f
                offsetX = 0f
                offsetY = 0f
            }
            Difficulty.EASY -> {
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
        
        // Buscar el grupo de confusión para rellenar respuestas trampa
        if (difficulty == Difficulty.HARD || difficulty == Difficulty.INFERNAL) {
            val group = CONFUSION_GROUPS.find { it.contains(correctId) }
            if (group != null) {
                val available = group.filter { it != correctId }.shuffled()
                for (i in 0 until minOf(3, available.size)) {
                    optionsSet.add(available[i])
                }
            }
        }
        
        // Rellenar con aleatorios si faltan
        while (optionsSet.size < 4) {
            optionsSet.add(Random.nextInt(1, 152))
        }
        
        currentOptions = optionsSet.toList().shuffled().map { id ->
            Pair(id, KANTO_POKEMON[id - 1])
        }
    }

    LaunchedEffect(Unit) {
        generateNewRound()
    }
    
    LaunchedEffect(currentTargetId, isRevealed) {
        if (difficulty != Difficulty.EASY && !isRevealed && !isProcessing) {
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
        PokemonAlertDialog(
            title = if (sessionCoins > 0) "¡Fin de la partida!" else "Fin de la partida",
            message = if (sessionCoins > 0) "Has ganado $sessionCoins monedas en total." else "Has perdido ${-sessionCoins} monedas en total.",
            isError = false,
            onDismiss = { 
                showRewardDialog = false
                onNavigateBack()
            }
        )
    }

    RetroBackground {
        // Barra superior
        SafariRetroHeader(
            title = "ZONA SAFARI",
            onBackClick = {
                if (sessionCoins != 0 && !isProcessing) {
                    isProcessing = true
                    scope.launch {
                        try {
                            Network.api.rewardUser(RewardRequest(userId = com.diegofg11.pokequiz.utils.SessionManager.currentUserId, levelId = 0, coinsEarned = sessionCoins))
                            withContext(Dispatchers.Main) { showRewardDialog = true }
                        } catch(e: Exception) {
                            withContext(Dispatchers.Main) {
                                onError("No se pudieron guardar tus monedas al salir.")
                                isProcessing = false
                            }
                        }
                    }
                } else if (!isProcessing) {
                    onNavigateBack()
                }
            }
        )

        // Monedas (Flotante a la derecha)
        Box(modifier = Modifier.fillMaxWidth().padding(top = 80.dp, end = 16.dp), contentAlignment = Alignment.TopEnd) {
            Surface(
                color = Color.Black,
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
            ) {
                Text(
                    text = "💰 $sessionCoins",
                    color = GoldPoke,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            if (difficulty != Difficulty.EASY) {
                val timerColor = if (timeLeft <= 2) {
                    if (flashTimer) Color.Red else Color.Yellow
                } else {
                    Color.Green
                }
                
                val timerProgress by animateFloatAsState(
                    targetValue = timeLeft.toFloat() / maxTime.toFloat(),
                    animationSpec = tween(1000)
                )
                
                LinearProgressIndicator(
                    progress = { timerProgress },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(2.dp)),
                    color = timerColor,
                    trackColor = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                RetroText(
                    text = "¿QUIÉN ES ESE POKÉMON?",
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // Contenedor de la Imagen
            RetroMenuBox(
                modifier = Modifier.size(250.dp),
                backgroundColor = Color(0xFFF0F0F0),
                borderColor = if (isRevealed) (if(selectedId == currentTargetId) Color(0xFF4CAF50) else Color(0xFFF44336)) else Color.Black
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${currentTargetId}.png",
                        contentDescription = "Mistery Pokemon",
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

            // Botones de Opciones
            if (currentOptions.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 0 until 4 step 2) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OptionButton(
                                text = currentOptions[i].second,
                                isSelected = selectedId == currentOptions[i].first,
                                isCorrect = currentOptions[i].first == currentTargetId,
                                isRevealed = isRevealed,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (!isRevealed && !isProcessing) {
                                    isProcessing = true
                                    selectedId = currentOptions[i].first
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
                            OptionButton(
                                text = currentOptions[i + 1].second,
                                isSelected = selectedId == currentOptions[i + 1].first,
                                isCorrect = currentOptions[i + 1].first == currentTargetId,
                                isRevealed = isRevealed,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (!isRevealed && !isProcessing) {
                                    isProcessing = true
                                    selectedId = currentOptions[i + 1].first
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
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}
