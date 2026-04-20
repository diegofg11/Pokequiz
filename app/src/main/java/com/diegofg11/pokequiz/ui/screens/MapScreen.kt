package com.diegofg11.pokequiz.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.VerticalDivider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.sqrt
import androidx.compose.ui.graphics.Brush
import com.diegofg11.pokequiz.ui.theme.*

@Composable
fun MapScreen(
    completedLevel: Int,
    onNavigateToBattle: (Int) -> Unit
) {
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
        // Title
        Text(
            text = "AVENTURA",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
        )

        // The Map
        LevelMap(
            completedLevel = completedLevel,
            onLevelClick = onNavigateToBattle,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 120.dp, bottom = 120.dp)
        )

        // Right side floating box
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 48.dp) // Adjusted padding
                .size(64.dp),
            shape = RoundedCornerShape(16.dp),
            color = CardBackground.copy(alpha = 0.8f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 Icon(Icons.Default.Menu, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
             }
        }
    }
}

@Composable
fun LevelMap(
    completedLevel: Int,
    onLevelClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val nodeRadius = 14.dp
    
    Canvas(modifier = modifier.pointerInput(completedLevel) {
        detectTapGestures { offset ->
            val width = size.width
            val height = size.height
            
            val nodes = listOf(
                Offset(width * 0.5f, height * 0.95f),
                Offset(width * 0.45f, height * 0.8f),
                Offset(width * 0.58f, height * 0.65f),
                Offset(width * 0.42f, height * 0.5f),
                Offset(width * 0.55f, height * 0.35f),
                Offset(width * 0.45f, height * 0.2f),
                Offset(width * 0.5f, height * 0.05f)
            )
            
            nodes.forEachIndexed { index, nodeOffset ->
                val levelId = index + 1
                val distance = sqrt((offset.x - nodeOffset.x).let { it * it } + (offset.y - nodeOffset.y).let { it * it })
                if (distance <= nodeRadius.toPx() * 2) {
                    // Only clickable if it's already completed or it's the next level to play
                    if (levelId <= completedLevel + 1) {
                        onLevelClick(levelId)
                    }
                }
            }
        }
    }) {
        val width = size.width
        val height = size.height

        // Path (Curved) - Going upwards
        val path = Path().apply {
            moveTo(width * 0.5f, height)
            cubicTo(width * 0.8f, height * 0.75f, width * 0.2f, height * 0.75f, width * 0.5f, height * 0.6f)
            cubicTo(width * 0.8f, height * 0.45f, width * 0.2f, height * 0.45f, width * 0.5f, height * 0.3f)
            cubicTo(width * 0.7f, height * 0.15f, width * 0.3f, height * 0.15f, width * 0.5f, 0f)
        }

        drawPath(path = path, color = RedPoke.copy(alpha = 0.2f), style = Stroke(width = 16.dp.toPx()))
        drawPath(path = path, color = RedPoke, style = Stroke(width = 6.dp.toPx()))

        val nodes = listOf(
            Offset(width * 0.5f, height * 0.95f),
            Offset(width * 0.45f, height * 0.8f),
            Offset(width * 0.58f, height * 0.65f),
            Offset(width * 0.42f, height * 0.5f),
            Offset(width * 0.55f, height * 0.35f),
            Offset(width * 0.45f, height * 0.2f),
            Offset(width * 0.5f, height * 0.05f)
        )

        nodes.forEachIndexed { index, offset ->
            val levelId = index + 1
            val isUnlocked = levelId <= completedLevel + 1
            val isCompleted = levelId <= completedLevel
            
            val baseColor = if (isUnlocked) RedPoke else Color.Gray

            // Outer glow
            drawCircle(
                color = baseColor.copy(alpha = 0.3f),
                radius = 20.dp.toPx(),
                center = offset
            )
            // Main circle
            drawCircle(
                color = if (isCompleted) RedPoke else DarkPoke,
                radius = nodeRadius.toPx(),
                center = offset
            )
            // Border
            drawCircle(
                color = if (isUnlocked) Color.White else Color.Gray,
                radius = nodeRadius.toPx(),
                center = offset,
                style = Stroke(width = 3.dp.toPx())
            )
            
            if (isCompleted) {
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = offset
                )
            }
        }
    }
}
