package com.diegofg11.pokequiz.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.diegofg11.pokequiz.R
import com.diegofg11.pokequiz.ui.theme.GoldPoke
import androidx.compose.ui.tooling.preview.Preview
import com.diegofg11.pokequiz.ui.theme.PokequizTheme

data class MinigameItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val reward: String,
    val color: Color
)

@Composable
fun MinigamesScreen(navController: NavController? = null) {
    val dummyGames = listOf(
        MinigameItem("1", "¿Quién es ese Pokémon?", "Adivina la silueta. 3 niveles de dificultad.", Icons.Default.PlayArrow, "Hasta 40 Monedas", Color(0xFF4CAF50)),
        MinigameItem("2", "Memorama", "Encuentra las parejas. ¡Cuidado con el modo Infernal!", Icons.Default.Star, "Hasta 200 Monedas", Color(0xFF2196F3)),
        MinigameItem("3", "Sopa de Letras", "Busca nombres de Pokémon ocultos", Icons.Default.PlayArrow, "75 Monedas", Color(0xFFFF9800)),
        MinigameItem("4", "Batalla Rápida", "Vence a 3 rivales seguidos", Icons.Default.Star, "150 Monedas", Color(0xFFE91E63))
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Full Screen Background Image
        Image(
            painter = painterResource(id = R.drawable.fondo_zona_safari),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Dark Overlay to improve readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Stylized Header with semi-transparent background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "ZONA SAFARI",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Entrena y gana fortuna",
                        color = GoldPoke,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Grid Section
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(dummyGames) { game ->
                    MinigameCard(game) {
                        if (game.id == "1") {
                            navController?.navigate("guess_pokemon")
                        } else if (game.id == "2") {
                            navController?.navigate("memory_game")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MinigameCard(game: MinigameItem, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f)
            .clip(RoundedCornerShape(28.dp))
            .clickable { onClick() },
        color = Color.Black.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.5f),
                    Color.Transparent
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon with glowing background
            Box(
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .blur(15.dp)
                        .background(game.color.copy(alpha = 0.6f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(game.color.copy(alpha = 0.4f), game.color.copy(alpha = 0.1f))
                            ), 
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = game.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = game.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = game.description,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp,
                    maxLines = 2
                )
            }
            
            // Reward Tag Premium
            Surface(
                color = game.color,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = game.reward,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MinigamesScreenPreview() {
    PokequizTheme {
        MinigamesScreen()
    }
}
