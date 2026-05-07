package com.diegofg11.pokequiz.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegofg11.pokequiz.ui.theme.*

@Composable
fun TutorialBox(
    title: String,
    description: String,
    buttonText: String = "SIGUIENTE",
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)) // Menos opacidad para dejar ver
            .clickable(enabled = true) { /* Bloquear clics al fondo */ },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 80.dp) // Espacio para el BottomNav
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        RetroText(
                            text = title,
                            fontSize = 16.sp,
                            color = Color(0xFF2D5A27)
                        )
                        // Pequeño cursor indicando que hay que leer
                        Text("▼", color = GoldPoke, fontSize = 12.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    RetroText(
                        text = description,
                        fontSize = 14.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    RetroButton(
                        text = buttonText,
                        onClick = onNext,
                        modifier = Modifier.align(Alignment.End).height(40.dp).width(140.dp),
                        containerColor = GoldPoke,
                        contentColor = Color.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
