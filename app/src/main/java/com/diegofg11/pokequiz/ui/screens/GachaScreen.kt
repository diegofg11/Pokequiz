package com.diegofg11.pokequiz.ui.screens

import android.util.Log
import android.widget.Toast
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

    // Cargar monedas al entrar
    LaunchedEffect(Unit) {
        try {
            val response = withContext(Dispatchers.IO) { Network.api.getUser(1) }
            if (response.isSuccessful && response.body() != null) {
                coins = response.body()!!.monedasGacha
            }
        } catch (e: Exception) {
            Log.e("GachaScreen", "Error loading coins", e)
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
                    Network.api.rollGacha(GachaRequest(userId = 1))
                }
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    coins = body.user.monedasGacha
                    val baseUrl = "https://pokequizbackend-production.up.railway.app"
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
                        Toast.makeText(context, "Error: ${err ?: response.message()}", Toast.LENGTH_SHORT).show()
                    }
                    gachaState = GachaAnimState.IDLE
                }
            } catch (e: Exception) {
                Log.e("GachaScreen", "Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
                }
                gachaState = GachaAnimState.IDLE
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
                )
            )
    ) {
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
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A4A))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$coins",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.3f))

            Text(
                text = "Gacha Pokémon",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "¡Toca la Pokéball para abrirla!",
                fontSize = 14.sp,
                color = Color(0xFFAABBCC),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.weight(0.3f))

            // Zona central
            Box(modifier = Modifier.size(250.dp), contentAlignment = Alignment.Center) {
                if (gachaState != GachaAnimState.REVEALED) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .scale(pokeballScale)
                            .rotate(shakeAnim.value)
                            .shadow(16.dp, CircleShape)
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
                                .background(Color(0xFFE53935))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .align(Alignment.Center)
                                .background(Color(0xFF333333))
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF333333)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
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
                Text(
                    text = if (isNewPull) "¡NUEVO POKÉMON!" else "¡REPETIDO! (+50 EXP)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isNewPull) Color(0xFFFFD700) else Color(0xFF48D0B0)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = revealedPokemon!!.nombre,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    revealedPokemon!!.tipos.forEach { tipo ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = getPokeTypeColor(tipo))
                        ) {
                            Text(
                                text = tipo,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Botones
            when (gachaState) {
                GachaAnimState.IDLE -> {
                    Button(
                        onClick = { doRoll() },
                        enabled = coins >= costPerRoll,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935),
                            disabledContainerColor = Color(0xFF555555)
                        )
                    ) {
                        Text("🎰 Abrir Pokéball  •  $costPerRoll 🪙", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    if (coins < costPerRoll) {
                        Text(
                            text = "No tienes suficientes monedas. ¡Gana batallas para conseguir más!",
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                GachaAnimState.REVEALED -> {
                    Button(
                        onClick = { gachaState = GachaAnimState.IDLE; revealedPokemon = null },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Text("🔄 Tirar otra vez", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onNavigateToPC,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4CAF50)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("📦 Ver mi PC", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Coste por tirada: $costPerRoll monedas",
                fontSize = 12.sp,
                color = Color(0xFF667788),
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
