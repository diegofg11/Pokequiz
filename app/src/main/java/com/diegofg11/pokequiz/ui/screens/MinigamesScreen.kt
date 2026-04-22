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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
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
fun MinigamesScreen() {
    val dummyGames = listOf(
        MinigameItem("1", "¿Quién es ese Pokémon?", "Adivina la silueta del Pokémon", Icons.Default.PlayArrow, "50 Monedas"),
        MinigameItem("2", "Memorama", "Encuentra las parejas de cartas", Icons.Default.Star, "100 Monedas"),
        MinigameItem("3", "Sopa de Letras", "Busca nombres de Pokémon ocultos", Icons.Default.PlayArrow, "75 Monedas"),
        MinigameItem("4", "Batalla Rápida", "Vence a 3 rivales seguidos", Icons.Default.Star, "150 Monedas")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundStart,
                        BackgroundMid,
                        BackgroundEnd
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ZONA SAFARI",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 8.dp, top = 24.dp)
            )
        
        Text(
            text = "Juega para conseguir monedas extra.",
            color = Color.LightGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(dummyGames) { game ->
                    MinigameCard(game)
                }
            }
        }
    }
}

@Composable
fun MinigameCard(game: MinigameItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .clickable { /* TODO: Navigate to game */ },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = game.icon,
                contentDescription = null,
                tint = GoldPoke,
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 8.dp)
            )
            
            Text(
                text = game.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = game.description,
                color = Color.LightGray,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
            
            Surface(
                color = GoldPoke.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = game.reward,
                    color = GoldPoke,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
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
