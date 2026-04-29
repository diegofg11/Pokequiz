package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.models.RewardRequest
import com.diegofg11.pokequiz.ui.components.PokemonAlertDialog
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

@Composable
fun GuessPokemonScreen(onNavigateBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    
    var currentTargetId by remember { mutableIntStateOf(1) }
    var currentOptions by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    
    var isRevealed by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf<Int?>(null) }
    var sessionCoins by remember { mutableIntStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    
    var globalError by remember { mutableStateOf<String?>(null) }
    var showRewardDialog by remember { mutableStateOf(false) }

    fun generateNewRound() {
        isRevealed = false
        selectedId = null
        isProcessing = false
        
        val correctId = Random.nextInt(1, 152) // 1 to 151
        val correctName = KANTO_POKEMON[correctId - 1]
        
        currentTargetId = correctId
        
        val optionsSet = mutableSetOf<Int>()
        optionsSet.add(correctId)
        while (optionsSet.size < 4) {
            optionsSet.add(Random.nextInt(1, 152))
        }
        
        currentOptions = optionsSet.toList().shuffled().map { id ->
            Pair(id, KANTO_POKEMON[id - 1])
        }
    }

    // Inicializar la primera ronda
    LaunchedEffect(Unit) {
        generateNewRound()
    }

    if (globalError != null) {
        PokemonAlertDialog(
            title = "¡Error!",
            message = globalError!!,
            isError = true,
            onDismiss = { globalError = null }
        )
    }

    if (showRewardDialog) {
        PokemonAlertDialog(
            title = if (sessionCoins > 0) "¡Enhorabuena!" else "Fin de la partida",
            message = if (sessionCoins > 0) "Has ganado $sessionCoins monedas en total." else "Has perdido ${-sessionCoins} monedas en total.",
            isError = false,
            onDismiss = { 
                showRewardDialog = false
                onNavigateBack()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundStart, BackgroundMid, BackgroundEnd)
                )
            )
    ) {
        // Barra superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (sessionCoins != 0 && !isProcessing) {
                    isProcessing = true
                    scope.launch {
                        try {
                            Network.api.rewardUser(RewardRequest(userId = 1, levelId = 0, coinsEarned = sessionCoins))
                            withContext(Dispatchers.Main) { showRewardDialog = true }
                        } catch(e: Exception) {
                            withContext(Dispatchers.Main) {
                                globalError = "No se pudieron guardar tus monedas al salir."
                                isProcessing = false
                            }
                        }
                    }
                } else if (!isProcessing) {
                    onNavigateBack()
                }
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "ZONA SAFARI",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            // Marcador
            Surface(
                color = GoldPoke.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPoke)
            ) {
                Text(
                    text = "Monedas: $sessionCoins",
                    color = GoldPoke,
                    fontWeight = FontWeight.Bold,
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
            Text(
                text = "¿Quién es ese Pokémon?",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Contenedor de la Imagen
            Surface(
                modifier = Modifier
                    .size(250.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color(0xFFF0F0F0),
                border = androidx.compose.foundation.BorderStroke(4.dp, if (isRevealed) (if(selectedId == currentTargetId) Color(0xFF4CAF50) else Color(0xFFF44336)) else Color(0xFF333333))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${currentTargetId}.png",
                        contentDescription = "Mistery Pokemon",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
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
                                    selectedId = currentOptions[i].first
                                    isRevealed = true
                                    
                                    if (selectedId == currentTargetId) {
                                        sessionCoins += 15
                                    } else {
                                        sessionCoins -= 30
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
                                    selectedId = currentOptions[i + 1].first
                                    isRevealed = true
                                    
                                    if (selectedId == currentTargetId) {
                                        sessionCoins += 15
                                    } else {
                                        sessionCoins -= 30
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
        !isRevealed -> Color(0xFFF8F8F8)
        isCorrect -> Color(0xFF4CAF50)
        isSelected && !isCorrect -> Color(0xFFF44336)
        else -> Color(0xFFE0E0E0)
    }
    
    val textColor = if (isRevealed && (isCorrect || isSelected)) Color.White else Color.Black

    Surface(
        modifier = modifier
            .height(60.dp)
            .clickable(enabled = !isRevealed) { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, if (!isRevealed) Color(0xFF333333) else Color.Transparent)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = text.uppercase(),
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
