/**
 * @authors: Gaizka, Diego y Xiker
 * Componentes comunes reutilizables (iconos decorativos como la PokéBall).
 */
package com.diegofg11.pokequiz.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.diegofg11.pokequiz.ui.theme.RedPoke

@Composable
fun PokeBallIcon(modifier: Modifier = Modifier, outerColor: Color = Color.White.copy(alpha = 0.1f)) {
    Box(
        modifier = modifier
            .background(outerColor, CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)
            drawArc(color = RedPoke, startAngle = 180f, sweepAngle = 180f, useCenter = true)
            drawArc(color = Color.White, startAngle = 0f, sweepAngle = 180f, useCenter = true)
            drawRect(color = Color.Black, topLeft = Offset(0f, center.y - 2.dp.toPx()), size = androidx.compose.ui.geometry.Size(size.width, 4.dp.toPx()))
            drawCircle(color = Color.Black, radius = radius * 0.3f, center = center)
            drawCircle(color = Color.White, radius = radius * 0.15f, center = center)
            drawCircle(color = Color.Black, radius = radius, style = Stroke(width = 2.dp.toPx()))
        }
    }
}
