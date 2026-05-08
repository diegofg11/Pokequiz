package com.diegofg11.pokequiz.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.ui.tooling.preview.Preview
import com.diegofg11.pokequiz.ui.theme.PokequizTheme
import com.diegofg11.pokequiz.ui.components.PokemonAlertDialog
import androidx.compose.animation.core.*
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
import androidx.compose.ui.res.stringResource
import com.diegofg11.pokequiz.R
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
import com.diegofg11.pokequiz.utils.PokemonUtils
import com.diegofg11.pokequiz.models.PokeType
import com.diegofg11.pokequiz.utils.AccessibilityManager

private enum class GachaAnimState { IDLE, SHAKING, OPENING, REVEALED }

@Composable
fun GachaScreen(onBack: () -> Unit, onNavigateToPC: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showHelp by remember { mutableStateOf(false) }

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
            errorMessage = context.getString(R.string.coins_load_error)
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

    // Lgica de tirada
    fun doRoll() {
        if (coins < costPerRoll || gachaState != GachaAnimState.IDLE) return
        gachaState = GachaAnimState.SHAKING
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    Log.d("POKEQUIZ_DEBUG", "Language being sent: ${com.diegofg11.pokequiz.utils.SessionManager.currentLanguage}")
                    Network.api.rollGacha(GachaRequest(userId = com.diegofg11.pokequiz.utils.SessionManager.currentUserId))
                }
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    coins = body.user.monedasGacha
                    val fixed = body.pulled.copy(
                        spriteFront = PokemonUtils.fixSpriteUrl(body.pulled.spriteFront),
                        spriteBack = PokemonUtils.fixSpriteUrl(body.pulled.spriteBack),
                        spriteIcon = PokemonUtils.fixSpriteUrl(body.pulled.spriteIcon)
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
                    errorMessage = context.getString(R.string.error_generic)
                }
                gachaState = GachaAnimState.IDLE
            }
        }
    }

    RetroBackground {
        if (errorMessage != null) {
            PokemonAlertDialog(
                title = stringResource(R.string.error_title),
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
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RetroHeader(
                title = stringResource(R.string.bazaar_title),
                onBackClick = onBack,
                onHelpClick = { showHelp = true }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.1f))

                val isHighContrast = AccessibilityManager.isHighContrastEnabled
                RetroText(
                    text = if (gachaState == GachaAnimState.REVEALED) stringResource(R.string.magnificent) else stringResource(R.string.collect_all),
                    fontSize = if (gachaState == GachaAnimState.REVEALED) 38.sp else 28.sp,
                    color = if (isHighContrast) Color.Black else (if (gachaState == GachaAnimState.REVEALED) GoldPoke else Color.White),
                    textAlign = TextAlign.Center
                )

                if (gachaState != GachaAnimState.REVEALED) {
                    Text(
                        text = stringResource(R.string.tap_pokeball),
                        fontSize = 14.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = if (isHighContrast) Color.Black else Color.Black.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.obtained_new_partner),
                        fontSize = 12.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Monedas (Ahora debajo del título)
                RetroMenuBox(
                    backgroundColor = Color.Black.copy(alpha = 0.05f),
                    borderColor = GoldPoke
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        RetroText(
                            text = "$coins",
                            color = GoldPoke,
                            fontSize = 18.sp,
                            showShadow = false
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.05f))

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
                                    modifier = Modifier.size(180.dp),
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
                                text = if (isNewPull) stringResource(R.string.new_pokemon) else stringResource(R.string.repeated_pokemon),
                                fontSize = 12.sp,
                                color = if (isNewPull) GoldPoke else Color(0xFF2D5A27)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            RetroText(
                                text = revealedPokemon!!.nombre.uppercase(),
                                fontSize = 22.sp,
                                color = Color(0xFF1B3022)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                revealedPokemon!!.tipos.forEach { tipo ->
                                    val pokeType = PokeType.getByString(tipo)
                                    Surface(
                                        shape = androidx.compose.ui.graphics.RectangleShape,
                                        color = pokeType?.color ?: Color.Gray,
                                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.2f))
                                    ) {
                                        Text(
                                            text = pokeType?.let { stringResource(it.stringResId) } ?: tipo.uppercase(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 2.dp
                                            )
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
                                fontSize = 13.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.05f))

                // Botones
                when (gachaState) {
                    GachaAnimState.IDLE -> {
                        RetroButton(
                            text = stringResource(R.string.open_pokeball, costPerRoll),
                            onClick = { doRoll() },
                            enabled = coins >= costPerRoll,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = RedPoke,
                            fontSize = 18.sp
                        )
                        if (coins < costPerRoll) {
                            Text(
                                text = stringResource(R.string.not_enough_coins),
                                color = Color(0xFFB71C1C),
                                fontSize = 12.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    GachaAnimState.REVEALED -> {
                        RetroButton(
                            text = stringResource(R.string.roll_again),
                            onClick = {
                                revealedPokemon = null
                                gachaState = GachaAnimState.IDLE
                                doRoll()
                            },
                            enabled = coins >= costPerRoll,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = RedPoke,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RetroButton(
                                text = stringResource(R.string.go_back),
                                onClick = {
                                    revealedPokemon = null
                                    gachaState = GachaAnimState.IDLE
                                },
                                modifier = Modifier.weight(1f),
                                containerColor = Color.Gray,
                                fontSize = 16.sp
                            )
                            RetroButton(
                                text = stringResource(R.string.see_my_pc),
                                onClick = onNavigateToPC,
                                modifier = Modifier.weight(1f),
                                containerColor = Color(0xFF2D5A27),
                                fontSize = 16.sp
                            )
                        }
                    }

                    else -> {}
                }

                Spacer(modifier = Modifier.weight(0.1f))
                if (gachaState != GachaAnimState.REVEALED) {
                    Text(
                        text = stringResource(R.string.cost_label, costPerRoll),
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color.Black.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (showHelp) {
            PokemonHelpDialog(
                title = stringResource(R.string.bazaar_title),
                onDismiss = { showHelp = false }
            ) {
                Column {
                    HelpSection(
                        stringResource(R.string.help_collect_title),
                        stringResource(R.string.help_collect_desc)
                    )
                    HelpSection(
                        stringResource(R.string.help_pokeballs_title),
                        stringResource(R.string.help_pokeballs_desc)
                    )
                    HelpSection(
                        stringResource(R.string.help_repeated_title),
                        stringResource(R.string.help_repeated_desc)
                    )
                    HelpSection(
                        stringResource(R.string.help_pc_title),
                        stringResource(R.string.help_pc_desc)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GachaScreenPreview() {
    PokequizTheme {
        GachaScreen(onBack = {}, onNavigateToPC = {})
    }
}

