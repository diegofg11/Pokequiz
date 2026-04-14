package com.diegofg11.pokequiz.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.models.PokemonBattle
import com.diegofg11.pokequiz.models.Question
import com.diegofg11.pokequiz.ui.theme.*

@Composable
fun BattleScreen(
    levelId: Int,
    onBattleWin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // Mock Battle Data (could use levelId to customize opponent)
    val battleData = remember(levelId) {
        PokemonBattle(
            id = levelId,
            name = if (levelId == 1) "Pikachu" else "Charmander",
            imageUrl = if (levelId == 1) 
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png" 
                else "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/4.png",
            maxHp = 100,
            questions = listOf(
                Question(1, "¿Qué movimiento es característico de este Pokémon?", listOf("Impactrueno", "Lanzallamas", "Burbuja", "Hoja Afilada"), if (levelId == 1) 0 else 1),
                Question(2, "¿Cuál es su tipo?", listOf("Eléctrico", "Fuego", "Agua", "Planta"), if (levelId == 1) 0 else 1)
            )
        )
    }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var opponentHp by remember { mutableIntStateOf(battleData.maxHp) }
    var userHp by remember { mutableIntStateOf(100) }
    var showGameOver by remember { mutableStateOf(false) }
    var gameOverMessage by remember { mutableStateOf("") }

    val currentQuestion = battleData.questions[currentQuestionIndex]

    if (showGameOver) {
        GameOverDialog(message = gameOverMessage, onDismiss = onNavigateBack)
    }

    Column(
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Opponent Section
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(150.dp)
            ) {
                Text(
                    text = battleData.name.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                HpBar(currentHp = opponentHp, maxHp = battleData.maxHp)
            }

            AsyncImage(
                model = battleData.imageUrl,
                contentDescription = battleData.name,
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.CenterStart)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Question Area
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            color = CardBackground.copy(alpha = 0.8f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = currentQuestion.text,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                currentQuestion.options.forEachIndexed { index, option ->
                    AnswerButton(
                        text = option,
                        onClick = {
                            if (index == currentQuestion.correctAnswerIndex) {
                                opponentHp = (opponentHp - 25).coerceAtLeast(0)
                                if (opponentHp == 0) {
                                    gameOverMessage = "¡HAS GANADO!"
                                    showGameOver = true
                                    // Trigger progress update
                                    onBattleWin()
                                }
                            } else {
                                userHp = (userHp - 25).coerceAtLeast(0)
                                if (userHp == 0) {
                                    gameOverMessage = "HAS PERDIDO..."
                                    showGameOver = true
                                }
                            }
                            
                            if (!showGameOver) {
                                if (currentQuestionIndex < battleData.questions.size - 1) {
                                    currentQuestionIndex++
                                } else {
                                    currentQuestionIndex = 0 // Restart or end
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // User Section
        Column(
            modifier = Modifier
                .align(Alignment.Start)
                .width(150.dp)
        ) {
            Text(
                text = "TÚ",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            HpBar(currentHp = userHp, maxHp = 100)
        }
    }
}

@Composable
fun HpBar(currentHp: Int, maxHp: Int) {
    val progress by animateFloatAsState(targetValue = currentHp.toFloat() / maxHp)
    
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Gray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(
                        when {
                            progress > 0.5f -> Color.Green
                            progress > 0.2f -> Color.Yellow
                            else -> Color.Red
                        }
                    )
            )
        }
        Text(
            text = "HP: $currentHp / $maxHp",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun AnswerButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )
    }
}

@Composable
fun GameOverDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = message, fontWeight = FontWeight.Bold, color = Color.White) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("VOLVER AL MAPA", color = RedPoke)
            }
        },
        containerColor = DarkPoke,
        shape = RoundedCornerShape(24.dp)
    )
}
