package com.diegofg11.pokequiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.ui.theme.BackgroundStart
import com.diegofg11.pokequiz.ui.theme.BackgroundMid
import com.diegofg11.pokequiz.ui.theme.BackgroundEnd
import com.diegofg11.pokequiz.ui.theme.GoldPoke
import com.diegofg11.pokequiz.ui.theme.CardBackground
import androidx.compose.ui.tooling.preview.Preview
import com.diegofg11.pokequiz.ui.theme.PokequizTheme

data class MinigameItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val reward: String
)

@Composable
fun MinigamesScreen(navController: NavController? = null) {
    val dummyGames = listOf(
        MinigameItem("1", "¿Quién es ese Pokémon?", "Adivina la silueta. 3 niveles de dificultad.", Icons.Default.PlayArrow, "Hasta 40 Monedas"),
        MinigameItem("2", "Memorama", "Encuentra las parejas. ¡Cuidado con el modo Infernal!", Icons.Default.Star, "Hasta 200 Monedas"),
        MinigameItem("3", "Sopa de Letras", "Busca nombres de Pokémon ocultos", Icons.Default.PlayArrow, "75 Monedas"),
        MinigameItem("4", "Batalla Rápida", "Vence a 3 rivales seguidos", Icons.Default.Star, "150 Monedas")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundStart, BackgroundMid, BackgroundEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                AsyncImage(
                    model = "file:///C:/Users/diieg/.gemini/antigravity/brain/28a55183-06db-4560-be03-037c821f069a/safari_zone_header_1777466068396.png",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Overlay Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    BackgroundStart.copy(alpha = 0.9f)
                                )
                            )
                        )
                )
                
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "ZONA SAFARI",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Minijuegos & Recompensas",
                        color = Color.LightGray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
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
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        color = Color.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
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
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(GoldPoke.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = game.icon,
                    contentDescription = null,
                    tint = GoldPoke,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = game.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = game.description,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    maxLines = 2
                )
            }
            
            Surface(
                color = GoldPoke,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = game.reward,
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
