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
            .border(4.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(2.dp)
            .border(2.dp, Color.White, RoundedCornerShape(6.dp))
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .padding(16.dp)
    ) {
        Column {
            content()
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
            backgroundColor = Color(0xFFF8F8F8)
        ) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                letterSpacing = 1.sp
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
    Box(
        modifier = modifier
            .clickable { onClick() }
            .border(4.dp, Color.Black, RoundedCornerShape(12.dp))
            .padding(2.dp)
            .border(2.dp, Color.White, RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = subtitle,
                color = Color.DarkGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(cost, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(reward, color = Color(0xFFBF8F00), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
