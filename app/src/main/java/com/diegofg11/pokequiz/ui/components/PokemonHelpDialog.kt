/**
 * @authors: Gaizka, Diego y Xiker
 * Diálogo de ayuda con secciones explicativas para cada pantalla del juego.
 */
package com.diegofg11.pokequiz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import com.diegofg11.pokequiz.R
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import com.diegofg11.pokequiz.utils.AccessibilityManager

@Composable
fun PokemonHelpDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val isHighContrast = AccessibilityManager.isHighContrastEnabled
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        // Nuevo contenedor simétrico blanco
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 550.dp)
                .drawBehind {
                    // Simetría: Franja verde arriba y abajo
                    val greenColor = if (isHighContrast) Color.Black else Color(0xFF2D5A27)
                    // Top green
                    drawRect(greenColor, Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(size.width, 6.dp.toPx()))
                    // Bottom green
                    drawRect(greenColor, Offset(0f, size.height - 6.dp.toPx()), size = androidx.compose.ui.geometry.Size(size.width, 6.dp.toPx()))
                },
            color = Color.White,
            shape = androidx.compose.ui.graphics.RectangleShape,
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp, bottom = 10.dp) // Espacio para las franjas verdes
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header del Diálogo (Estilo Safari Header)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title.uppercase(),
                        color = Color.Black,
                        fontSize = 18.sp * AccessibilityManager.fontScale,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
                
                PixelDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Contenido Scrolleable
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.05f), androidx.compose.ui.graphics.RectangleShape)
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column {
                        content()
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón Cerrar Retro (Estilo limpio)
                Surface(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .heightIn(min = 48.dp),
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    color = if (isHighContrast) Color.Black else Color(0xFFF5F5F5),
                    contentColor = if (isHighContrast) Color.White else Color.Black,
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.understood),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp * AccessibilityManager.fontScale,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HelpSection(title: String, description: String) {
    val scaledFontScale = AccessibilityManager.fontScale
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title.uppercase(),
            fontWeight = FontWeight.Black,
            fontSize = 13.sp * scaledFontScale,
            color = Color(0xFF1B3022),
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            fontSize = 12.sp * scaledFontScale,
            color = Color.Black.copy(alpha = 0.7f),
            lineHeight = 16.sp * scaledFontScale,
            fontFamily = FontFamily.Monospace
        )
    }
}
