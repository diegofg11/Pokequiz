package com.diegofg11.pokequiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.diegofg11.pokequiz.models.Pokemon
import com.diegofg11.pokequiz.models.User

class PokemonPCActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Mock data for demonstration
            val mockUser = User(id = 1234, nombre = "Ash Ketchum", nivelProgreso = 10, monedasGacha = 500)
            val mockPokemons = listOf(
                Pokemon(1, "Bulbasaur", listOf("Planta", "Veneno"), 45, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png", "", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-vii/icons/1.png"),
                Pokemon(4, "Charmander", listOf("Fuego"), 39, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/4.png", "", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-vii/icons/4.png"),
                Pokemon(7, "Squirtle", listOf("Agua"), 44, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/7.png", "", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-vii/icons/7.png"),
                Pokemon(25, "Pikachu", listOf("Eléctrico"), 35, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png", "", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-vii/icons/25.png")
            )
            
            PokemonPCScreen(user = mockUser, pokemons = mockPokemons)
        }
    }
}

@Composable
fun PokemonPCScreen(user: User, pokemons: List<Pokemon>) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF0F0F0) // Light grey background
    ) {
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
                    items(pokemons) { pokemon ->
                        PokemonGridItem(pokemon)
                    }
                    
                    // Fill remaining slots with empty boxes to mimic PC feel
                    val totalSlots = 30
                    val emptySlots = totalSlots - pokemons.size
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

@Composable
fun PokemonGridItem(pokemon: Pokemon) {
    Card(
        modifier = Modifier
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
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
