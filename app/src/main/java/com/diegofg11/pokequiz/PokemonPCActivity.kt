package com.diegofg11.pokequiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.models.Pokemon
import com.diegofg11.pokequiz.models.User
import com.diegofg11.pokequiz.ui.components.RetroButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.*
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.grid.items


class PokemonPCActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val userState = remember { mutableStateOf<User?>(User(1, "Usuario Entrenador", 1, 0)) }
            val pokemonsState = remember { mutableStateListOf<Pokemon>() }
            val isLoading = remember { mutableStateOf(true) }
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                scope.launch {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            Network.api.getPc(userId = com.diegofg11.pokequiz.utils.SessionManager.currentUserId) // Using dynamic SessionManager
                        }
                        if (response.isSuccessful && response.body() != null) {
                            val body = response.body()!!
                            pokemonsState.clear()
                            val baseUrl = com.diegofg11.pokequiz.api.Network.BASE_URL.dropLast(1)
                            val mappedList = body.map { 
                                it.copy(
                                    spriteFront = if (it.spriteFront.startsWith("/")) baseUrl + it.spriteFront else it.spriteFront,
                                    spriteBack = if (it.spriteBack.startsWith("/")) baseUrl + it.spriteBack else it.spriteBack,
                                    spriteIcon = if (it.spriteIcon.startsWith("/")) baseUrl + it.spriteIcon else it.spriteIcon
                                )
                            }
                            pokemonsState.addAll(mappedList)
                        } else {
                            Log.e("PCActivity", "Error PC: ${response.code()}")
                            Toast.makeText(context, "Error al cargar PC", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("PCActivity", "Exception: ${e.message}")
                        Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading.value = false
                    }
                }
            }
            
            if (isLoading.value) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                var selectedPokemon by remember { mutableStateOf<Pokemon?>(null) }
                
                PokemonPCScreen(user = userState.value!!, pokemonsState = pokemonsState) { pokemon ->
                    selectedPokemon = pokemon
                }

                selectedPokemon?.let { pokemon ->
                    PokedexDialog(
                        pokemon = pokemon,
                        onDismiss = { selectedPokemon = null },
                        onToggleParty = {
                            scope.launch {
                                try {
                                    val newInParty = !pokemon.inParty
                                    val res = withContext(Dispatchers.IO) {
                                        Network.api.toggleParty(com.diegofg11.pokequiz.models.TogglePartyRequest(com.diegofg11.pokequiz.utils.SessionManager.currentUserId, pokemon.inventoryId ?: 0, newInParty))
                                    }
                                    if (res.isSuccessful) {
                                        withContext(Dispatchers.Main) {
                                            val index = pokemonsState.indexOfFirst { it.inventoryId == pokemon.inventoryId }
                                            if (index != -1) {
                                                pokemonsState[index] = pokemon.copy(inParty = newInParty)
                                            }
                                            selectedPokemon = null
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) { Toast.makeText(context, "Equipo lleno (Máx 3)", Toast.LENGTH_SHORT).show() }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) { Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show() }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PokedexDialog(pokemon: Pokemon, onDismiss: () -> Unit, onToggleParty: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            RetroButton(
                text = if (pokemon.inParty) "Quitar del Equipo" else "Añadir al Equipo",
                onClick = onToggleParty,
                fontSize = 12.sp,
                containerColor = if (pokemon.inParty) Color(0xFFD32F2F) else Color(0xFF2D5A27)
            )
        },
        dismissButton = {
            RetroButton(
                text = "Cerrar",
                onClick = onDismiss,
                fontSize = 12.sp,
                containerColor = Color.Gray
            )
        },
        title = {
            Text(text = "Datos de la Pokédex", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = pokemon.spriteFront,
                    contentDescription = pokemon.nombre,
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = "#${pokemon.idPokedex} ${pokemon.nombre.replaceFirstChar { it.uppercase() }}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    pokemon.tipos.forEach { tipo ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF6C63FF),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = tipo,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = pokemon.pokedexDescription ?: "Sin descripción disponible.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Nivel: ${pokemon.level}", fontWeight = FontWeight.SemiBold)
                    Text("HP: ${pokemon.hpBase}", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
fun PokemonPCScreen(user: User, pokemonsState: androidx.compose.runtime.snapshots.SnapshotStateList<Pokemon>, onPokemonClick: (Pokemon) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF0F0F0) // Light grey background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar circular del usuario
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6C63FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.nombre
                            .split(" ")
                            .take(2)
                            .joinToString("") { it.first().uppercase() },
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nombre del usuario
                Text(
                    text = user.nombre,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Código del usuario
                Text(
                    text = "Código: #${user.id}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Tu Colección de Pokémon",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 12.dp),
                    color = Color(0xFF444444)
                )

                // Pokemon PC Grid
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8E8E8))
                        .border(2.dp, Color(0xFFCCCCCC), RoundedCornerShape(12.dp))
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3), // 3 columns like a standard PC grid
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pokemonsState.toList()) { pokemon ->
                            PokemonGridItem(pokemon) {
                                onPokemonClick(pokemon)
                            }
                        }
                        
                        // Fill remaining slots with empty boxes to mimic PC feel
                        val totalSlots = 30
                        val emptySlots = totalSlots - pokemonsState.size
                        if (emptySlots > 0) {
                            items(emptySlots) {
                                EmptySlot()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PokemonGridItem(pokemon: Pokemon, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = if (pokemon.inParty) Color(0xFFFFFBE6) else Color.White),
        border = if (pokemon.inParty) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700)) else null
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (pokemon.inParty) {
                Text(
                    "⭐",
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                    fontSize = 12.sp
                )
            }
            // Muestra Nivel y Experiencia arriba a la izquierda
            Text(
                "Nv${pokemon.level}",
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
            )

            AsyncImage(
                model = pokemon.spriteFront,
                contentDescription = pokemon.nombre,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Fit
            )
            
            Text(
                text = pokemon.nombre,
                fontSize = 10.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
fun EmptySlot() {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(Color(0xFFD1D1D1), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFC0C0C0), RoundedCornerShape(8.dp))
    )
}
