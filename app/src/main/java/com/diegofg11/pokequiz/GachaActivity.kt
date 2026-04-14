package com.diegofg11.pokequiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.models.GachaRequest
import com.diegofg11.pokequiz.models.Pokemon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import android.widget.Toast
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext

// Estados del Gacha
enum class GachaState {
    IDLE,       // Esperando a que el usuario pulse
    SHAKING,    // La Pokéball tiembla
    OPENING,    // La Pokéball se abre
    REVEALED    // El Pokémon se muestra
}

class GachaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Datos del usuario - hardcodeado por ahora (ejercicio)
            val userCoins = remember { mutableIntStateOf(500) }
            val costPerRoll = 100
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            GachaScreen(
                coins = userCoins.intValue,
                costPerRoll = costPerRoll,
                onRoll = { onPokemonRevealed ->
                    scope.launch {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                Network.api.rollGacha(GachaRequest(userId = 1)) // Hardcoded userId=1
                            }
                            if (response.isSuccessful && response.body() != null) {
                                val body = response.body()!!
                                if (body.pulled == null) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Error: No se recibió ningún Pokémon", Toast.LENGTH_SHORT).show()
                                    }
                                    onPokemonRevealed(null)
                                    return@launch
                                }
                                userCoins.intValue = body.user.monedasGacha
                                // Corregir URLs relativas del backend
                                val baseUrl = "http://10.0.2.2:3001"
                                val fixedPokemon = body.pulled.copy(
                                    spriteFront = if (body.pulled.spriteFront.startsWith("/")) baseUrl + body.pulled.spriteFront else body.pulled.spriteFront,
                                    spriteBack = if (body.pulled.spriteBack.startsWith("/")) baseUrl + body.pulled.spriteBack else body.pulled.spriteBack,
                                    spriteIcon = if (body.pulled.spriteIcon.startsWith("/")) baseUrl + body.pulled.spriteIcon else body.pulled.spriteIcon
                                )
                                onPokemonRevealed(fixedPokemon)
                            } else {
                                Log.e("GachaActivity", "Error roll: ${response.code()}")
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Error al tirar: ${response.message()}", Toast.LENGTH_SHORT).show()
                                }
                                onPokemonRevealed(null)
                            }
                        } catch (e: Exception) {
                            Log.e("GachaActivity", "Exception: ${e.message}")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                            onPokemonRevealed(null)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun GachaScreen(
    coins: Int,
    costPerRoll: Int,
    onRoll: ((Pokemon?) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var gachaState by remember { mutableStateOf(GachaState.IDLE) }
    var revealedPokemon by remember { mutableStateOf<Pokemon?>(null) }

    // Animación de temblor de la Pokéball
    val shakeAnim = remember { Animatable(0f) }

    // Animación de escala de la Pokéball
    val pokeballScale by animateFloatAsState(
        targetValue = when (gachaState) {
            GachaState.IDLE -> 1f
            GachaState.SHAKING -> 1.1f
            GachaState.OPENING -> 0f
            GachaState.REVEALED -> 0f
        },
        animationSpec = tween(
            durationMillis = if (gachaState == GachaState.OPENING) 400 else 200
        ),
        label = "pokeballScale"
    )

    // Animación del Pokémon revelado
    val pokemonScale by animateFloatAsState(
        targetValue = if (gachaState == GachaState.REVEALED) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pokemonScale"
    )

    // Animación de brillo de fondo al revelar
    val glowAlpha by animateFloatAsState(
        targetValue = if (gachaState == GachaState.REVEALED) 0.3f else 0f,
        animationSpec = tween(600),
        label = "glowAlpha"
    )

    // Efecto de temblor y espera del resultado
    LaunchedEffect(gachaState) {
        if (gachaState == GachaState.SHAKING) {
            // Temblor durante 1.5 segundos
            repeat(12) {
                shakeAnim.animateTo(
                    targetValue = if (it % 2 == 0) 15f else -15f,
                    animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
                )
            }
            shakeAnim.animateTo(0f, animationSpec = tween(50))
            
            // Si ya tenemos el pokemon, pasamos a abrirlo
            gachaState = GachaState.OPENING
        }
    }

    // Una vez en OPENING, esperamos a tener el pokemon para pasar a REVEALED
    LaunchedEffect(gachaState, revealedPokemon) {
        if (gachaState == GachaState.OPENING && revealedPokemon != null) {
            delay(800) // Tiempo para la animación de apertura
            gachaState = GachaState.REVEALED
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF0F3460)
                        )
                    )
                )
        ) {
            // Brillo radial al revelar
            if (glowAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .size(400.dp)
                        .align(Alignment.Center)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700).copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Barra superior con monedas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2A4A)
                        )
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

                // Título
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

                // Zona central: Pokéball o Pokémon revelado
                Box(
                    modifier = Modifier
                        .size(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Pokéball
                    if (gachaState != GachaState.REVEALED) {
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .scale(pokeballScale)
                                .rotate(shakeAnim.value)
                                .shadow(16.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable(enabled = gachaState == GachaState.IDLE && coins >= costPerRoll) {
                                    gachaState = GachaState.SHAKING
                                    onRoll { pokemon ->
                                        if (pokemon != null) {
                                            revealedPokemon = pokemon
                                        } else {
                                            gachaState = GachaState.IDLE
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Parte superior roja
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.5f)
                                    .align(Alignment.TopCenter)
                                    .background(Color(0xFFE53935))
                            )
                            // Línea central negra
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .align(Alignment.Center)
                                    .background(Color(0xFF333333))
                            )
                            // Botón central
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

                    // Pokémon revelado
                    if (gachaState == GachaState.REVEALED && revealedPokemon != null) {
                        Column(
                            modifier = Modifier.scale(pokemonScale),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Brillo detrás del sprite
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFFFFD700).copy(alpha = 0.2f),
                                                    Color.Transparent
                                                )
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
                if (gachaState == GachaState.REVEALED && revealedPokemon != null) {
                    Text(
                        text = "¡Has obtenido!",
                        fontSize = 16.sp,
                        color = Color(0xFFAABBCC)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = revealedPokemon!!.nombre,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Tipos
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        revealedPokemon!!.tipos.forEach { tipo ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = getTypeColor(tipo)
                                )
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

                // Botón de tirada / reintentar
                if (gachaState == GachaState.IDLE) {
                    Button(
                        onClick = {
                            if (coins >= costPerRoll) {
                                gachaState = GachaState.SHAKING
                                onRoll { pokemon ->
                                    if (pokemon != null) {
                                        revealedPokemon = pokemon
                                    } else {
                                        gachaState = GachaState.IDLE
                                    }
                                }
                            }
                        },
                        enabled = coins >= costPerRoll,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935),
                            disabledContainerColor = Color(0xFF555555)
                        )
                    ) {
                        Text(
                            text = "🎰 Abrir Pokéball  •  $costPerRoll 🪙",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (coins < costPerRoll) {
                        Text(
                            text = "No tienes suficientes monedas",
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else if (gachaState == GachaState.REVEALED) {
                    Button(
                        onClick = {
                            gachaState = GachaState.IDLE
                            revealedPokemon = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text(
                            text = "🔄 Tirar otra vez",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(context, PokemonPCActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, Color(0xFF4CAF50)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "📦 Ir al PC",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Coste por tirada
                Text(
                    text = "Coste por tirada: $costPerRoll monedas",
                    fontSize = 12.sp,
                    color = Color(0xFF667788),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// Colores por tipo de Pokémon
fun getTypeColor(tipo: String): Color {
    return when (tipo.lowercase()) {
        "planta" -> Color(0xFF4CAF50)
        "fuego" -> Color(0xFFFF5722)
        "agua" -> Color(0xFF2196F3)
        "eléctrico" -> Color(0xFFFFC107)
        "normal" -> Color(0xFF9E9E9E)
        "hielo" -> Color(0xFF00BCD4)
        "lucha" -> Color(0xFFC62828)
        "veneno" -> Color(0xFF7B1FA2)
        "tierra" -> Color(0xFF8D6E63)
        "volador" -> Color(0xFF90CAF9)
        "psíquico" -> Color(0xFFE91E63)
        "bicho" -> Color(0xFF8BC34A)
        "roca" -> Color(0xFF795548)
        "fantasma" -> Color(0xFF5C6BC0)
        "dragón" -> Color(0xFF7C4DFF)
        "siniestro" -> Color(0xFF424242)
        "acero" -> Color(0xFFB0BEC5)
        "hada" -> Color(0xFFF48FB1)
        else -> Color(0xFF757575)
    }
}
