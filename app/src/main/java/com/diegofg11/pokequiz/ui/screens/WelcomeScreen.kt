package com.diegofg11.pokequiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.diegofg11.pokequiz.ui.theme.*

@Composable
fun WelcomeScreen(onEnterClick: () -> Unit = {}) {
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // Título Principal
            Text(
                text = "POKÉQUIZ",
                color = GoldPoke,
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Logo Dibujado con Compose (Forma de Pokeball)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Parte Superior Roja
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(RedPoke))
                    // Línea Central Negra
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(DarkPoke))
                    // Parte Inferior Blanca
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.White))
                }
                // Botón Central de la Pokeball
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DarkPoke),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Botón Entrar
            Button(
                onClick = onEnterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPoke,
                    contentColor = DarkPoke
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(
                    text = "ENTRAR",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    PokequizTheme {
        WelcomeScreen()
    }
}
