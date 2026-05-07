package com.diegofg11.pokequiz.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.ui.tooling.preview.Preview
import com.diegofg11.pokequiz.ui.theme.PokequizTheme
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.diegofg11.pokequiz.ui.components.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.models.Pokemon
import com.diegofg11.pokequiz.models.TogglePartyRequest
import com.diegofg11.pokequiz.models.User
import com.diegofg11.pokequiz.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.diegofg11.pokequiz.utils.WallpaperManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

@Composable
fun PCScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var user by remember { mutableStateOf<User?>(null) }
    val pokemonList = remember { mutableStateListOf<Pokemon>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var warningMessage by remember { mutableStateOf<String?>(null) }
    var selectedIndex by remember { mutableStateOf<Int?>( null) }

    LaunchedEffect(Unit) {
        try {
            val userId = com.diegofg11.pokequiz.utils.SessionManager.currentUserId
            val userResp = withContext(Dispatchers.IO) { Network.api.getUser(userId) }
            val pcResp = withContext(Dispatchers.IO) { Network.api.getPc(userId) }

            if (userResp.isSuccessful) user = userResp.body()

            if (pcResp.isSuccessful && pcResp.body() != null) {
                val baseUrl = com.diegofg11.pokequiz.api.Network.BASE_URL.dropLast(1)
                val mapped = pcResp.body()!!.map {
                    it.copy(
                        spriteFront = if (it.spriteFront.startsWith("/")) baseUrl + it.spriteFront else it.spriteFront,
                        spriteBack = if (it.spriteBack.startsWith("/")) baseUrl + it.spriteBack else it.spriteBack,
                        spriteIcon = if (it.spriteIcon.startsWith("/")) baseUrl + it.spriteIcon else it.spriteIcon
                    )
                }
                pokemonList.clear()
                pokemonList.addAll(mapped)
            }
        } catch (e: Exception) {
            Log.e("PCScreen", "Error: ${e.message}")
            errorMessage = "Error de red"
        } finally {
            isLoading = false
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
        if (warningMessage != null) {
            PokemonAlertDialog(
                title = "Aviso",
                message = warningMessage!!,
                isError = false,
                onDismiss = { warningMessage = null }
            )
        }



        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldPoke)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Spacer(modifier = Modifier.height(20.dp))

                // Equipo actual
                val partyPokemon = pokemonList.filter { it.inParty }
                Spacer(modifier = Modifier.height(20.dp))
                
                RetroText(
                    text = "EQUIPO ACTUAL",
                    fontSize = 16.sp,
                    color = GoldPoke,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, bottom = 8.dp)
                )
                
                RetroMenuBox(
                    backgroundColor = Color.White.copy(alpha = 0.1f),
                    borderColor = Color(0xFF2D5A27)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) { index ->
                            val pkmn = partyPokemon.getOrNull(index)
                            Box(modifier = Modifier.weight(1f)) {
                                if (pkmn != null) {
                                        PCPokemonCard(pokemon = pkmn, isParty = true) {
                                            selectedIndex = pokemonList.indexOf(pkmn)
                                        }
                                } else {
                                    PCEmptySlot(isParty = true)
                                }
                            }
                        }
                    }
                }

                // Mi Colección
                Spacer(modifier = Modifier.height(20.dp))
                RetroText(
                    text = "COLECCIÓN PC",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, bottom = 8.dp)
                )

                RetroMenuBox(
                    backgroundColor = Color.White.copy(alpha = 0.9f),
                    borderColor = Color(0xFF1B3022)
                ) {
                    if (pokemonList.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¡EL PC ESTÁ VACÍO!",
                                color = Color(0xFF1B3022),
                                fontSize = 16.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.height(400.dp), // Altura fija para el grid en el PC
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pokemonList.toList()) { pokemon ->
                                    PCPokemonCard(pokemon = pokemon) {
                                        selectedIndex = pokemonList.indexOf(pokemon)
                                    }
                            }
                            // Slots vacíos para completar la estética
                            val totalSlots = 21
                            val empty = (totalSlots - pokemonList.size).coerceAtLeast(0)
                            items(empty) { PCEmptySlot() }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "TOTAL: ${pokemonList.size} POKÉMON",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        selectedIndex?.let { idx ->
            val safeIdx = idx.coerceIn(0, pokemonList.lastIndex)
            PokedexDialog(
                pokemonList = pokemonList.toList(),
                initialIndex = safeIdx,
                onDismiss = { selectedIndex = null },
                onToggleParty = { targetPokemon, toggleTo ->
                    scope.launch {
                        try {
                            val res = withContext(Dispatchers.IO) {
                                Network.api.toggleParty(
                                    TogglePartyRequest(com.diegofg11.pokequiz.utils.SessionManager.currentUserId, targetPokemon.inventoryId ?: 0, toggleTo)
                                )
                            }
                            if (res.isSuccessful) {
                                val listIdx = pokemonList.indexOfFirst { it.inventoryId == targetPokemon.inventoryId }
                                if (listIdx != -1) pokemonList[listIdx] = targetPokemon.copy(inParty = toggleTo)
                            } else {
                                withContext(Dispatchers.Main) {
                                    warningMessage = "Equipo lleno (Máx 3)"
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                errorMessage = "Error de red"
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun PokedexDialog(
    pokemonList: List<Pokemon>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    onToggleParty: (Pokemon, Boolean) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { pokemonList.size }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .pointerInput(Unit) { detectTapGestures {} },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera con flechas de navegación y contador
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationArrow(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    modifier = Modifier.size(40.dp),
                    onClick = {
                        if (pagerState.currentPage > 0) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        }
                    }
                )

                Text(
                    text = "${pagerState.currentPage + 1} / ${pokemonList.size}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                NavigationArrow(
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    modifier = Modifier.size(40.dp),
                    onClick = {
                        if (pagerState.currentPage < pokemonList.lastIndex) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    }
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val pokemon = pokemonList[page]
                RetroMenuBox(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White,
                    borderColor = GoldPoke
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RetroText(text = "DATOS POKÉDEX", fontSize = 14.sp, color = Color(0xFF1B3022))
                            Text(
                                "Nº ${pokemon.idPokedex}",
                                fontSize = 12.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                        }

                        PixelDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                                .border(2.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = pokemon.spriteFront,
                                contentDescription = pokemon.nombre,
                                modifier = Modifier.size(120.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        RetroText(
                            text = pokemon.nombre.uppercase(),
                            fontSize = 24.sp,
                            color = Color(0xFF1B3022)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pokemon.tipos.forEach { tipo ->
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
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        PixelDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = pokemon.pokedexDescription ?: "Sin datos registrados.",
                            color = Color(0xFF1B3022),
                            fontSize = 13.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatInfo("NIV", "${pokemon.level}")
                            StatInfo("PS", "${pokemon.hpBase}")
                            StatInfo("EXP", "${pokemon.exp}/100")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        RetroButton(
                            text = if (pokemon.inParty) "DEJAR EN PC" else "A EQUIPO",
                            onClick = { onToggleParty(pokemon, !pokemon.inParty) },
                            containerColor = if (pokemon.inParty) RedPoke else Color(0xFF2D5A27),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        RetroButton(
                            text = "CERRAR",
                            onClick = onDismiss,
                            containerColor = Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = Color.Gray, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
    }
}

@Composable
private fun PCPokemonCard(pokemon: Pokemon, isParty: Boolean = false, onClick: () -> Unit) {
    RetroMenuBox(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        backgroundColor = if (pokemon.inParty) Color(0xFFFFF9C4) else Color.White,
        borderColor = if (pokemon.inParty) GoldPoke else Color(0xFF1B3022)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (pokemon.inParty && !isParty) {
                Text(
                    "⭐",
                    modifier = Modifier.align(Alignment.TopEnd).padding(2.dp),
                    fontSize = 10.sp
                )
            }
            AsyncImage(
                model = pokemon.spriteFront,
                contentDescription = pokemon.nombre,
                modifier = Modifier.size(if (isParty) 64.dp else 90.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun PCEmptySlot(isParty: Boolean = false) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
            .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "?",
            color = Color.Black.copy(alpha = 0.2f),
            fontSize = 16.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PCScreenPreview() {
    PokequizTheme {
        PCScreen()
    }
}
