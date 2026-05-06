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
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = if (isError) Color(0xFFFBE6E6) else Color(0xFFE6F2FF),
                border = BorderStroke(4.dp, if (isError) Color(0xFFD32F2F) else Color(0xFF1976D2)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 20.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = if (isError) Color(0xFFB71C1C) else Color(0xFF0D47A1),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (onConfirm != null && confirmText != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RetroButton(
                                text = "CANCELAR",
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f).height(48.dp),
                                containerColor = Color.Gray,
                                fontSize = 12.sp
                            )
                            RetroButton(
                                text = confirmText,
                                onClick = onConfirm,
                                modifier = Modifier.weight(1f).height(48.dp),
                                containerColor = if (isError) Color(0xFFD32F2F) else Color(0xFF1976D2),
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        RetroButton(
                            text = "ENTENDIDO",
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                            containerColor = if (isError) Color(0xFFD32F2F) else Color(0xFF1976D2),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Icono en la esquina superior izquierda
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.TopStart)
                    .border(
                        3.dp,
                        if (isError) Color(0xFFD32F2F) else Color(0xFF1976D2),
                        CircleShape
                    ),
                shape = CircleShape,
                color = if (isError) Color(0xFFD32F2F) else Color(0xFF1976D2),
                shadowElevation = 10.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
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
