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

@Composable
fun PokemonHelpDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        RetroBackground(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header del Diálogo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF2D5A27)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RetroText(
                        text = title.uppercase(),
                        fontSize = 20.sp,
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
                
                // Botón Cerrar Retro
                Surface(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(48.dp),
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    color = Color(0xFF2D5A27),
                    contentColor = Color.White,
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1B3022))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.understood),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HelpSection(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title.uppercase(),
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            color = Color(0xFF1B3022),
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            fontSize = 12.sp,
            color = Color.Black.copy(alpha = 0.7f),
            lineHeight = 16.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
