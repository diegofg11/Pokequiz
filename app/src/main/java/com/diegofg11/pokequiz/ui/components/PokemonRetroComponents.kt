package com.diegofg11.pokequiz.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import com.diegofg11.pokequiz.ui.theme.*

@Composable
fun RetroMenuBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFECEFF1), // Gris azulado pálido muy retro
    borderColor: Color = Color(0xFF37474F), // Gris oscuro azulado para contraste
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .border(4.dp, borderColor, RoundedCornerShape(2.dp))
            .padding(2.dp)
            .border(2.dp, Color.White, RoundedCornerShape(1.dp))
            .background(backgroundColor)
    ) {
        // Subtle grid pattern
        Canvas(modifier = Modifier.matchParentSize()) {
            val step = 8.dp.toPx()
            for (x in 0..size.width.toInt() step step.toInt()) {
                drawLine(Color.Black.copy(alpha = 0.03f), Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height), strokeWidth = 1f)
            }
            for (y in 0..size.height.toInt() step step.toInt()) {
                drawLine(Color.Black.copy(alpha = 0.03f), Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), strokeWidth = 1f)
            }
        }

        // Decorative corner dots - matchParentSize prevents stretching the parent
        Box(modifier = Modifier.matchParentSize()) {
            Box(modifier = Modifier.size(6.dp).background(borderColor).align(Alignment.TopStart))
            Box(modifier = Modifier.size(6.dp).background(borderColor).align(Alignment.TopEnd))
            Box(modifier = Modifier.size(6.dp).background(borderColor).align(Alignment.BottomStart))
            Box(modifier = Modifier.size(6.dp).background(borderColor).align(Alignment.BottomEnd))
        }
        
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun RetroBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1B3022)) // Verde oscuro bosque profundo
    ) {
        // Pixel Pattern Overlay (Capa base)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 20.dp.toPx()
            for (x in 0..size.width.toInt() step step.toInt()) {
                for (y in 0..size.height.toInt() step step.toInt()) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.1f),
                        topLeft = Offset(x.toFloat(), y.toFloat()),
                        size = Size(8.dp.toPx(), 8.dp.toPx())
                    )
                }
            }
        }

        // Screen Area with subtle themed background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp) // Less padding for more space
                .background(Color(0xFF94A684), RoundedCornerShape(4.dp)) // Proper Olive Green
                .border(2.dp, Color(0xFF2D5A27), RoundedCornerShape(4.dp))
                .border(4.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
        ) {
            // Grid de fondo muy sutil para la "pantalla"
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 10.dp.toPx()
                for (x in 0..size.width.toInt() step step.toInt()) {
                    drawLine(Color.Black.copy(alpha = 0.02f), Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height))
                }
                for (y in 0..size.height.toInt() step step.toInt()) {
                    drawLine(Color.Black.copy(alpha = 0.02f), Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()))
                }
            }

            Box(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                content = content
            )
        }
    }
}

@Composable
fun RetroText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF1B3022), // Contraste alto por defecto
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Bold,
    shadowColor: Color = Color.Black,
    showShadow: Boolean = true
) {
    val style = TextStyle(
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        fontFamily = FontFamily.Monospace,
        lineHeight = fontSize * 1.2f // Prevent overlapping
    )

    Box(modifier = modifier) {
        if (showShadow) {
            // Shadow
            Text(
                text = text,
                style = style.copy(color = shadowColor),
                modifier = Modifier.offset(x = 2.dp, y = 2.dp)
            )
        }
        // Main Text
        Text(
            text = text,
            style = style
        )
    }
}

@Composable
fun PixelDivider(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(4.dp)) {
        val width = size.width
        val step = 8.dp.toPx()
        for (i in 0..(width / step).toInt()) {
            drawRect(
                color = if (i % 2 == 0) Color.Black.copy(alpha = 0.2f) else Color.Transparent,
                topLeft = Offset(i * step, 0f),
                size = Size(step, 4.dp.toPx())
            )
        }
    }
}

@Composable
fun PokeballIndicator(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.2f
        
        // Círculo base
        drawCircle(
            color = Color.Black,
            radius = radius + 2f,
            center = center,
            style = Fill
        )
        
        // Mitad Superior (Roja si activo, Gris si inactivo)
        val topPath = Path().apply {
            addArc(
                oval = androidx.compose.ui.geometry.Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f
            )
        }
        drawPath(
            path = topPath,
            color = if (isActive) Color(0xFFE53935) else Color(0xFF9E9E9E),
            style = Fill
        )
        
        // Mitad Inferior (Blanca)
        val bottomPath = Path().apply {
            addArc(
                oval = androidx.compose.ui.geometry.Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f
            )
        }
        drawPath(
            path = bottomPath,
            color = Color.White,
            style = Fill
        )
        
        // Línea central negra
        drawLine(
            color = Color.Black,
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = 4f
        )
        
        // Círculo central (Botón)
        drawCircle(
            color = Color.Black,
            radius = radius * 0.35f,
            center = center,
            style = Fill
        )
        drawCircle(
            color = Color.White,
            radius = radius * 0.2f,
            center = center,
            style = Fill
        )
    }
}

@Composable
fun PokeballPageIndicator(
    pagerState: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { iteration ->
            PokeballIndicator(
                isActive = pagerState == iteration,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun SafariRetroHeader(
    title: String,
    onBackClick: () -> Unit,
    onHelpClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    extraContent: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Título en Caja Retro (Centro)
        RetroMenuBox(
            modifier = Modifier.fillMaxWidth(0.55f),
            backgroundColor = Color(0xFF2D5A27),
            borderColor = Color(0xFF1B3022)
        ) {
            Text(
                text = title.uppercase(),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
        }
        
        extraContent()

        // Botón Atrás (Izquierda) - Ahora a juego con el de ayuda
        Surface(
            onClick = onBackClick,
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.CenterStart),
            shape = CircleShape,
            color = Color(0xFF2D5A27),
            contentColor = Color.White,
            border = BorderStroke(2.dp, Color(0xFF1B3022))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Botón Ayuda (Derecha)
        if (onHelpClick != null) {
            Surface(
                onClick = onHelpClick,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.CenterEnd),
                shape = CircleShape,
                color = Color(0xFF2D5A27),
                contentColor = Color.White,
                border = BorderStroke(2.dp, Color(0xFF1B3022))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("?", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun NavigationArrow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.3f), // Transparente y elegante
        contentColor = Color.White,
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun RetroDifficultyCard(
    title: String,
    subtitle: String,
    cost: String,
    reward: String,
    color: Color,
    modifier: Modifier = Modifier,
    costLabel: String = "ENTRA",
    rewardLabel: String = "GANA",
    onClick: () -> Unit
) {
    val borderColor = color.copy(alpha = 0.8f)
    
    Box(
        modifier = modifier
            .height(150.dp)
            .fillMaxWidth()
            .clickable { onClick() }
            .border(3.dp, Color(0xFF1B3022), RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título de dificultad (Contraste alto garantizado)
            Text(
                text = title.uppercase(),
                color = Color(0xFF1B3022), // Verde casi negro para máxima lectura
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
            
            // Subtítulo descriptivo
            Text(
                text = subtitle,
                color = Color.Black.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                lineHeight = 12.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Info Bar para Coste y Premio (Solo si hay valores)
            if (cost.isNotEmpty() || reward.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (cost.isNotEmpty()) {
                        InfoItem(label = costLabel, value = cost, color = Color(0xFFB71C1C))
                        if (reward.isNotEmpty()) {
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.Black.copy(alpha = 0.1f)))
                        }
                    }
                    if (reward.isNotEmpty()) {
                        InfoItem(label = rewardLabel, value = reward, color = Color(0xFF1B5E20))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label, 
            fontSize = 9.sp, 
            fontWeight = FontWeight.Black, 
            color = Color.Black.copy(alpha = 0.5f),
            fontFamily = FontFamily.Monospace
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "💰",
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 2.dp)
            )
            Text(
                text = value, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Black, 
                color = color,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun SafariResultScreen(
    title: String,
    subtitle: String,
    description: String,
    isVictory: Boolean,
    coinsEarned: Int,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    RetroBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono de Resultado
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(if (isVictory) Color(0xFF4CAF50) else Color(0xFFE53935), CircleShape)
                    .border(4.dp, Color.Black.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isVictory) "🏆" else "❌",
                    fontSize = 50.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Título Principal
            RetroText(
                text = title.uppercase(),
                fontSize = 38.sp,
                textAlign = TextAlign.Center,
                color = if (isVictory) Color.White else Color(0xFF1B3022)
            )

            // Subtítulo (Modo)
            Text(
                text = subtitle,
                color = if (isVictory) Color(0xFF2D5A27) else Color(0xFF1B3022).copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Descripción
            Text(
                text = description,
                color = Color(0xFF1B3022).copy(alpha = 0.8f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Monedas Ganadas
            RetroMenuBox(
                backgroundColor = Color.Black.copy(alpha = 0.05f),
                borderColor = if (isVictory) GoldPoke else Color.Gray,
                modifier = Modifier.wrapContentSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = if (coinsEarned >= 0) "+$coinsEarned" else "$coinsEarned",
                        color = if (coinsEarned >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("💰", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Botones de Acción
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2D5A27).copy(alpha = 0.2f),
                    contentColor = Color(0xFF1B3022)
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(2.dp, Color(0xFF1B3022).copy(alpha = 0.5f))
            ) {
                Text(
                    "INTENTAR DE NUEVO",
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onExit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "VOLVER AL MENÚ",
                    color = Color(0xFF1B3022).copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
    }
}
@Composable
fun SafariSelectionScreen(
    title: String,
    subtitle: String,
    cards: List<DifficultyCardData>,
    columns: Int = 2
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            RetroText(
                text = title.uppercase(),
                fontSize = 38.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = Color(0xFF333333),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
            )

            // Grid de Selección de Dificultad
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                cards.forEach { card ->
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(card.span) }) {
                        RetroDifficultyCard(
                            title = card.title,
                            subtitle = card.subtitle,
                            cost = card.cost,
                            reward = card.reward,
                            rewardLabel = card.rewardLabel,
                            color = card.color,
                            onClick = card.onClick,
                            modifier = card.modifier
                        )
                    }
                }
            }
        }
    }
}

data class DifficultyCardData(
    val title: String,
    val subtitle: String,
    val cost: String = "",
    val reward: String = "",
    val color: Color,
    val onClick: () -> Unit,
    val rewardLabel: String = "GANA",
    val span: Int = 1,
    val modifier: Modifier = Modifier
)
