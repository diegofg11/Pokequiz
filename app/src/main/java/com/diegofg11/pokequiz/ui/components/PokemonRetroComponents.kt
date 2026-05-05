package com.diegofg11.pokequiz.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    backgroundColor: Color = Color.White,
    borderColor: Color = Color.Black,
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

        // Decorative corner dots
        Box(modifier = Modifier.fillMaxSize()) {
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
            .background(Color(0xFF2D5A27))
    ) {
        // Pixel Pattern Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 16.dp.toPx()
            for (x in 0..size.width.toInt() step step.toInt()) {
                for (y in 0..size.height.toInt() step step.toInt()) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.05f),
                        topLeft = Offset(x.toFloat(), y.toFloat()),
                        size = Size(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }

        // Screen Border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .border(4.dp, Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}

@Composable
fun RetroText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Bold,
    shadowColor: Color = Color.Black
) {
    Box(modifier = modifier) {
        // Shadow (More defined)
        Text(
            text = text,
            color = shadowColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.offset(x = 2.dp, y = 2.dp)
        )
        // Main Text
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            fontFamily = FontFamily.Monospace
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón Retro Atrás
        Surface(
            onClick = onBackClick,
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(4.dp),
            color = Color.Black,
            contentColor = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("<", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Título en Caja Retro
        RetroMenuBox(
            modifier = Modifier.weight(1f),
            backgroundColor = Color(0xFFF8F8F8),
            borderColor = Color(0xFF5D4037)
        ) {
            Text(
                text = title.uppercase(),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = color.copy(alpha = 0.8f)
    
    Box(
        modifier = modifier
            .height(130.dp) // Fixed height to reduce empty space
            .clickable { onClick() }
            .border(4.dp, Color.Black, RoundedCornerShape(4.dp))
            .padding(2.dp)
            .border(2.dp, Color.White, RoundedCornerShape(2.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(12.dp)
    ) {
        // Mini corner ornaments
        Box(modifier = Modifier.size(4.dp).background(Color.Black).align(Alignment.TopStart))
        Box(modifier = Modifier.size(4.dp).background(Color.Black).align(Alignment.TopEnd))
        Box(modifier = Modifier.size(4.dp).background(Color.Black).align(Alignment.BottomStart))
        Box(modifier = Modifier.size(4.dp).background(Color.Black).align(Alignment.BottomEnd))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = title,
                color = color,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = subtitle,
                    color = Color.Black,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    fontFamily = FontFamily.Monospace
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    cost, 
                    color = Color.Black, 
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    reward, 
                    color = Color(0xFFD4AF37), // Gold more saturated
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
