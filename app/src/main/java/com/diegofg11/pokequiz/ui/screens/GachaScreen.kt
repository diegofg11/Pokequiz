package com.diegofg11.pokequiz.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.ui.tooling.preview.Preview
import com.diegofg11.pokequiz.ui.theme.PokequizTheme
import com.diegofg11.pokequiz.ui.components.PokemonAlertDialog
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.models.GachaRequest
import com.diegofg11.pokequiz.models.GachaResponse
import com.diegofg11.pokequiz.models.Pokemon
import com.diegofg11.pokequiz.ui.components.*
import com.diegofg11.pokequiz.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class GachaAnimState { IDLE, SHAKING, OPENING, REVEALED }

@Composable
fun GachaScreen(onNavigateToPC: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var coins by remember { mutableIntStateOf(0) }
    val costPerRoll = 100

    var gachaState by remember { mutableStateOf(GachaAnimState.IDLE) }
    var revealedPokemon by remember { mutableStateOf<Pokemon?>(null) }
    var isNewPull by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Cargar monedas al entrar
    LaunchedEffect(Unit) {
        try {
            val response = withContext(Dispatchers.IO) { Network.api.getUser(com.diegofg11.pokequiz.utils.SessionManager.currentUserId) }
            if (response.isSuccessful && response.body() != null) {
                coins = response.body()!!.monedasGacha
            }
        } catch (e: Exception) {
            Log.e("GachaScreen", "Error loading coins", e)
            errorMessage = "No se pudo conectar con el servidor para cargar tus monedas."
        }
    }

    // Animación de temblor
    val shakeAnim = remember { Animatable(0f) }

    val pokeballScale by animateFloatAsState(
        targetValue = when (gachaState) {
            GachaAnimState.IDLE -> 1f
            GachaAnimState.SHAKING -> 1.1f
            GachaAnimState.OPENING -> 0f
            GachaAnimState.REVEALED -> 0f
        },
        animationSpec = tween(durationMillis = if (gachaState == GachaAnimState.OPENING) 400 else 200),
        label = "pokeballScale"
    )

    val pokemonScale by animateFloatAsState(
        targetValue = if (gachaState == GachaAnimState.REVEALED) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pokemonScale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (gachaState == GachaAnimState.REVEALED) 0.3f else 0f,
        animationSpec = tween(600),
        label = "glowAlpha"
    )

    LaunchedEffect(gachaState) {
        if (gachaState == GachaAnimState.SHAKING) {
            repeat(12) {
                shakeAnim.animateTo(
                    targetValue = if (it % 2 == 0) 15f else -15f,
                    animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
                )
            }
            shakeAnim.animateTo(0f, animationSpec = tween(50))
            gachaState = GachaAnimState.OPENING
        }
    }

    LaunchedEffect(gachaState, revealedPokemon) {
        if (gachaState == GachaAnimState.OPENING && revealedPokemon != null) {
            delay(1200)
            gachaState = GachaAnimState.REVEALED
        }
    }

    // Lógica de tirada
    fun doRoll() {
        if (coins < costPerRoll || gachaState != GachaAnimState.IDLE) return
        gachaState = GachaAnimState.SHAKING
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    Network.api.rollGacha(GachaRequest(userId = com.diegofg11.pokequiz.utils.SessionManager.currentUserId))
                }
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    coins = body.user.monedasGacha
                    val baseUrl = com.diegofg11.pokequiz.api.Network.BASE_URL.dropLast(1)
                    val fixed = body.pulled.copy(
                        spriteFront = if (body.pulled.spriteFront.startsWith("/")) baseUrl + body.pulled.spriteFront else body.pulled.spriteFront,
                        spriteBack = if (body.pulled.spriteBack.startsWith("/")) baseUrl + body.pulled.spriteBack else body.pulled.spriteBack,
                        spriteIcon = if (body.pulled.spriteIcon.startsWith("/")) baseUrl + body.pulled.spriteIcon else body.pulled.spriteIcon
                    )
                    revealedPokemon = fixed
                    isNewPull = body.isNew
                } else {
                    val err = response.errorBody()?.string()
                    withContext(Dispatchers.Main) {
                        errorMessage = "Error: ${err ?: response.message()}"
                    }
                    gachaState = GachaAnimState.IDLE
                }
            } catch (e: Exception) {
                Log.e("GachaScreen", "Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    errorMessage = "Error de red"
                }
                gachaState = GachaAnimState.IDLE
            }
        }
    }

    RetroBackground {
        if (errorMessage != null) {
            PokemonAlertDialog(
                title = "¡Error!",
                message = errorMessage!!,
                isError = true,
                onDismiss = { errorMessage = null }
            )
        }
        if (glowAlpha > 0f) {
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .align(Alignment.Center)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFFD700).copy(alpha = glowAlpha), Color.Transparent)
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Monedas
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RetroMenuBox(
                    backgroundColor = Color.Black.copy(alpha = 0.05f),
                    borderColor = GoldPoke
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        RetroText(
                            text = "$coins",
                            color = GoldPoke,
                            fontSize = 16.sp,
                            showShadow = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            RetroText(
                text = "TIENDA GACHA",
                fontSize = 32.sp,
                color = Color.White
            )
            Text(
                text = "¡Toca la Pokéball para abrirla!",
                fontSize = 12.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.weight(0.3f))

            // Zona central
            Box(modifier = Modifier.size(250.dp), contentAlignment = Alignment.Center) {
                if (gachaState != GachaAnimState.REVEALED) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(pokeballScale)
                            .rotate(shakeAnim.value)
                            .border(4.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(enabled = gachaState == GachaAnimState.IDLE && coins >= costPerRoll) { doRoll() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.5f)
                                .align(Alignment.TopCenter)
                                .background(RedPoke)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .align(Alignment.Center)
                                .background(Color.Black)
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }

                if (gachaState == GachaAnimState.REVEALED && revealedPokemon != null) {
                    Column(
                        modifier = Modifier.scale(pokemonScale),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(Color(0xFFFFD700).copy(alpha = 0.2f), Color.Transparent)
                                        )
                                    )
                            )
                            AsyncImage(
                                model = revealedPokemon!!.spriteFront,
                                contentDescription = revealedPokemon!!.nombre,
                                modifier = Modifier.size(160.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }

            // Info del Pokémon revelado
            if (gachaState == GachaAnimState.REVEALED && revealedPokemon != null) {
                RetroMenuBox(
                    backgroundColor = Color.White.copy(alpha = 0.9f),
                    borderColor = if (isNewPull) GoldPoke else Color(0xFF2D5A27)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RetroText(
                            text = if (isNewPull) "¡NUEVO POKÉMON!" else "¡REPETIDO! (+50 EXP)",
                            fontSize = 12.sp,
                            color = if (isNewPull) GoldPoke else Color(0xFF2D5A27)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        RetroText(
                            text = revealedPokemon!!.nombre.uppercase(),
                            fontSize = 20.sp,
                            color = Color(0xFF1B3022)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            revealedPokemon!!.tipos.forEach { tipo ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = getPokeTypeColor(tipo),
                                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = tipo.uppercase(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        PixelDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = revealedPokemon!!.pokedexDescription ?: "",
                            color = Color(0xFF1B3022),
                            fontSize = 12.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Botones
            when (gachaState) {
                GachaAnimState.IDLE -> {
                    RetroButton(
                        text = "ABRIR POKÉBALL ($costPerRoll 🪙)",
                        onClick = { doRoll() },
                        enabled = coins >= costPerRoll,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = RedPoke
                    )
                    if (coins < costPerRoll) {
                        Text(
                            text = "No tienes suficientes monedas.",
                            color = Color(0xFFB71C1C),
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                GachaAnimState.REVEALED -> {
                    RetroButton(
                        text = "TIRAR OTRA VEZ",
                        onClick = { gachaState = GachaAnimState.IDLE; revealedPokemon = null },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = RedPoke
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    RetroButton(
                        text = "VER MI PC",
                        onClick = onNavigateToPC,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color(0xFF2D5A27)
                    )
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "COSTE: $costPerRoll MONEDAS",
                fontSize = 11.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = Color.Black.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

fun getPokeTypeColor(tipo: String): Color {
    return when (tipo.lowercase()) {
        "planta", "grass" -> Color(0xFF4CAF50)
        "fuego", "fire" -> Color(0xFFFF5722)
        "agua", "water" -> Color(0xFF2196F3)
        "eléctrico", "electric" -> Color(0xFFFFC107)
        "normal" -> Color(0xFF9E9E9E)
        "hielo", "ice" -> Color(0xFF00BCD4)
        "lucha", "fighting" -> Color(0xFFC62828)
        "veneno", "poison" -> Color(0xFF7B1FA2)
        "tierra", "ground" -> Color(0xFF8D6E63)
        "volador", "flying" -> Color(0xFF90CAF9)
        "psíquico", "psychic" -> Color(0xFFE91E63)
        "bicho", "bug" -> Color(0xFF8BC34A)
        "roca", "rock" -> Color(0xFF795548)
        "fantasma", "ghost" -> Color(0xFF5C6BC0)
        "dragón", "dragon" -> Color(0xFF7C4DFF)
        "siniestro", "dark" -> Color(0xFF424242)
        "acero", "steel" -> Color(0xFFB0BEC5)
        "hada", "fairy" -> Color(0xFFF48FB1)
        else -> Color(0xFF757575)
    }
}

@Preview(showBackground = true)
@Composable
fun GachaScreenPreview() {
    PokequizTheme {
        GachaScreen(onNavigateToPC = {})
    }
}
