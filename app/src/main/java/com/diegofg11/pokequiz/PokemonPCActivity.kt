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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.*
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.grid.items
import com.diegofg11.pokequiz.ui.components.PokeMenu

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
                            Network.api.getPc(userId = 1) // Hardcoded userId=1
                        }
                        if (response.isSuccessful && response.body() != null) {
                            val body = response.body()!!
                            pokemonsState.clear()
                            val baseUrl = "http://10.0.2.2:3001"
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
                PokemonPCScreen(user = userState.value!!, pokemonsState = pokemonsState)
            }
        }
    }
}

@Composable
fun PokemonPCScreen(user: User, pokemonsState: androidx.compose.runtime.snapshots.SnapshotStateList<Pokemon>) {
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
                            PokemonGridItem(pokemon, context, scope) { updated ->
                                val index = pokemonsState.indexOfFirst { it.inventoryId == updated.inventoryId }
                                if (index != -1) {
                                    pokemonsState[index] = updated
                                }
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

            PokeMenu(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            )
        }
    }
}

@Composable
fun PokemonGridItem(pokemon: Pokemon, context: android.content.Context, scope: kotlinx.coroutines.CoroutineScope, onToggle: (Pokemon) -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable {
                scope.launch {
                    try {
                        val newInParty = !pokemon.inParty
                        val res = withContext(Dispatchers.IO) {
                            Network.api.toggleParty(com.diegofg11.pokequiz.models.TogglePartyRequest(1, pokemon.inventoryId ?: 0, newInParty))
                        }
                        if (res.isSuccessful) {
                            withContext(Dispatchers.Main) { onToggle(pokemon.copy(inParty = newInParty)) }
                        } else {
                            withContext(Dispatchers.Main) { Toast.makeText(context, "Equipo lleno (Máx 3)", Toast.LENGTH_SHORT).show() }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) { Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show() }
                    }
                }
            },
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
