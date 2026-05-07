package com.diegofg11.pokequiz.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun PokemonAlertDialog(
    title: String,
    message: String,
    isError: Boolean = true,
    confirmText: String? = null,
    onConfirm: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            RetroMenuBox(
                backgroundColor = if (isError) Color(0xFFFBE6E6) else Color(0xFFE3F2FD),
                borderColor = if (isError) Color(0xFFD32F2F) else Color(0xFF1976D2),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RetroText(
                        text = title,
                        fontSize = 22.sp,
                        color = if (isError) Color(0xFFB71C1C) else Color(0xFF0D47A1),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    RetroText(
                        text = message,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    if (onConfirm != null && confirmText != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RetroButton(
                                text = "SALIR",
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f).height(46.dp),
                                containerColor = Color.DarkGray,
                                fontSize = 10.sp
                            )
                            RetroButton(
                                text = confirmText.uppercase(),
                                onClick = onConfirm,
                                modifier = Modifier.weight(1.2f).height(46.dp),
                                containerColor = if (isError) Color(0xFFD32F2F) else Color(0xFF1976D2),
                                fontSize = 10.sp
                            )
                        }
                    } else {
                        RetroButton(
                            text = "ENTENDIDO",
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(0.8f).height(46.dp),
                            containerColor = if (isError) Color(0xFFD32F2F) else Color(0xFF1976D2),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            // Retro Styled Icon Box
            RetroMenuBox(
                backgroundColor = if (isError) Color(0xFFD32F2F) else Color(0xFF1976D2),
                borderColor = Color.Black,
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-4).dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = if (isError) Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = "Icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}
