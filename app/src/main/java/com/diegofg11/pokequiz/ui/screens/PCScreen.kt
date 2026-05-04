package com.diegofg11.pokequiz.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.ui.tooling.preview.Preview
import com.diegofg11.pokequiz.ui.theme.PokequizTheme
import com.diegofg11.pokequiz.ui.components.PokemonAlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.models.Pokemon
import com.diegofg11.pokequiz.models.TogglePartyRequest
import com.diegofg11.pokequiz.models.User
import com.diegofg11.pokequiz.ui.theme.BackgroundEnd
import com.diegofg11.pokequiz.ui.theme.BackgroundMid
import com.diegofg11.pokequiz.ui.theme.BackgroundStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.diegofg11.pokequiz.utils.WallpaperManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
    var showWallpaperDialog by remember { mutableStateOf(false) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundStart, BackgroundMid, BackgroundEnd)
                )
            )
    ) {
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
                isError = false, // Info/Warning
                onDismiss = { warningMessage = null }
            )
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // --- WALLPAPER SELECTION DIALOG ---
                if (showWallpaperDialog) {
                    AlertDialog(
                        onDismissRequest = { showWallpaperDialog = false },
                        title = { Text("Seleccionar Fondo del Mapa", color = Color.White, fontWeight = FontWeight.Bold) },
                        containerColor = Color(0xFF1A1A2E),
                        text = {
                            val wallpapers = WallpaperManager.getAllWallpapers()
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.height(300.dp),
                                contentPadding = PaddingValues(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(wallpapers) { index, resId ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .clickable {
                                                WallpaperManager.setSelectedWallpaper(context, index)
                                                showWallpaperDialog = false
                                                Toast.makeText(context, "Fondo actualizado", Toast.LENGTH_SHORT).show()
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                                    ) {
                                        Image(
                                            painter = painterResource(id = resId),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showWallpaperDialog = false }) {
                                Text("Cerrar", color = Color.White)
                            }
                        }
                    )
                }

                // --- TOP BAR WITH WALLPAPER BUTTON ---
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = { showWallpaperDialog = true },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cambiar Fondo",
                            tint = Color.White
                        )
                    }
                }

                // Avatar del usuario
                val displayUser = user
                if (displayUser != null) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6C63FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayUser.nombre.split(" ").take(2).joinToString("") { it.first().uppercase() },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = displayUser.nombre,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Nivel: ${displayUser.nivelProgreso}",
                            fontSize = 13.sp,
                            color = Color(0xFFAABBCC)
                        )
                        Text(
                            text = "🪙 ${displayUser.monedasGacha}",
                            fontSize = 13.sp,
                            color = Color(0xFFFFD700)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Equipo actual (in-party)
                val partyPokemon = pokemonList.filter { it.inParty }
                if (partyPokemon.isNotEmpty()) {
                    Text(
                        text = "⭐ Equipo (${partyPokemon.size}/3)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        partyPokemon.forEach { pkmn ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A4A)),
                                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = pkmn.spriteFront,
                                        contentDescription = pkmn.nombre,
                                        modifier = Modifier.size(56.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Text(
                                        text = pkmn.nombre,
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Nv${pkmn.level}",
                                        fontSize = 9.sp,
                                        color = Color(0xFFAABBCC)
                                    )
                                }
                            }
                        }
                        // Slots vacíos del equipo
                        repeat(3 - partyPokemon.size) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(90.dp)
                                    .background(Color(0xFF1A1A2E), RoundedCornerShape(12.dp))
                                    .border(2.dp, Color(0xFF444466), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("?", color = Color(0xFF444466), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Colección completa
                Text(
                    text = "📦 Mi Colección (${pokemonList.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1A1A2E))
                        .border(1.dp, Color(0xFF2A2A4A), RoundedCornerShape(16.dp))
                ) {
                    if (pokemonList.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("¡Tu PC está vacío!", color = Color.White, fontSize = 16.sp)
                            Text(
                                "Ve al Gacha para conseguir Pokémon",
                                color = Color(0xFFAABBCC),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pokemonList.toList()) { pokemon ->
                                PCPokemonCard(pokemon = pokemon) { toggleTo ->
                                    scope.launch {
                                        try {
                                            val res = withContext(Dispatchers.IO) {
                                                Network.api.toggleParty(
                                                    TogglePartyRequest(com.diegofg11.pokequiz.utils.SessionManager.currentUserId, pokemon.inventoryId ?: 0, toggleTo)
                                                )
                                            }
                                            if (res.isSuccessful) {
                                                val idx = pokemonList.indexOfFirst { it.inventoryId == pokemon.inventoryId }
                                                if (idx != -1) pokemonList[idx] = pokemon.copy(inParty = toggleTo)
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
                            }
                            // Slots vacíos
                            val totalSlots = 30
                            val empty = (totalSlots - pokemonList.size).coerceAtLeast(0)
                            items(empty) { PCEmptySlot() }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PCPokemonCard(pokemon: Pokemon, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onToggle(!pokemon.inParty) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pokemon.inParty) Color(0xFF2A2040) else Color(0xFF16213E)
        ),
        border = if (pokemon.inParty)
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700))
        else
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A4A))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (pokemon.inParty) {
                Text(
                    "⭐",
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                    fontSize = 10.sp
                )
            }
            Text(
                "Nv${pokemon.level}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFAABBCC),
                fontSize = 9.sp,
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
            )
            AsyncImage(
                model = pokemon.spriteFront,
                contentDescription = pokemon.nombre,
                modifier = Modifier.size(72.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = pokemon.nombre,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun PCEmptySlot() {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(Color(0xFF0D0D1A), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF1A1A2E), RoundedCornerShape(8.dp))
    )
}

@Preview(showBackground = true)
@Composable
fun PCScreenPreview() {
    PokequizTheme {
        PCScreen()
    }
}
