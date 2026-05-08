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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.BlendMode
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
import com.diegofg11.pokequiz.utils.AccessibilityManager
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.diegofg11.pokequiz.utils.ColorBlindMode

/**
 * @authors: Gaizka, Diego y Xiker
 * Contenedor con estética retro estilo GameBoy Advance.
 * Añade bordes de píxel, un patrón de rejilla sutil y puntos decorativos en las esquinas.
 */
@Composable
fun RetroMenuBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFECEFF1), // Gris azulado pálido muy retro
    borderColor: Color = Color(0xFF37474F), // Gris oscuro azulado para contraste
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .border(4.dp, borderColor, androidx.compose.ui.graphics.RectangleShape)
            .padding(2.dp)
            .border(2.dp, Color.White, androidx.compose.ui.graphics.RectangleShape)
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

/**
 * Fondo base para todas las pantallas del juego.
 * Implementa el sistema de accesibilidad global:
 * - Aplica matrices de color para modos daltónicos (Protanopia, Deuteranopia, Tritanopia).
 * - Cambia a modo de alto contraste (Blanco/Negro) si está habilitado.
 * - Dibuja un patrón de píxeles decorativo de fondo.
 */
@Composable
fun RetroBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isHighContrast = AccessibilityManager.isHighContrastEnabled
    val bgColor = if (isHighContrast) Color.Black else Color(0xFF1B3022)
    val screenBg = if (isHighContrast) Color.White else Color(0xFF94A684)
    val borderColor = if (isHighContrast) Color.White else Color(0xFF2D5A27)

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                renderEffect = if (AccessibilityManager.colorBlindMode != ColorBlindMode.NONE) {
                    androidx.compose.ui.graphics.ColorFilter.colorMatrix(AccessibilityManager.getColorMatrix()).let { null } 
                    // Nota: renderEffect es para efectos ms complejos, en Compose estable usaremos colorFilter si est disponible
                    // o simplemente graphicsLayer con colorFilter no existe directamente, se usa en Canvas o Painter.
                    // Para un Box completo, lo mejor es usar drawWithContent.
                    null
                } else null
            }
            .drawWithContent {
                val matrix = AccessibilityManager.getColorMatrix()
                if (AccessibilityManager.colorBlindMode != ColorBlindMode.NONE) {
                    drawContent()
                    drawRect(
                        color = Color.Transparent,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(matrix),
                        blendMode = androidx.compose.ui.graphics.BlendMode.Color
                    )
                } else {
                    drawContent()
                }
            }
            .background(bgColor)
    ) {
        // Pixel Pattern Overlay (Capa base) - Hidden in high contrast for clarity
        if (!isHighContrast) {
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
        }

        // Screen Area with subtle themed background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .background(screenBg, androidx.compose.ui.graphics.RectangleShape)
                .border(2.dp, borderColor, androidx.compose.ui.graphics.RectangleShape)
                .border(4.dp, Color.Black.copy(alpha = 0.1f), androidx.compose.ui.graphics.RectangleShape)
        ) {
            // Grid de fondo muy sutil para la "pantalla"
            if (!isHighContrast) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 10.dp.toPx()
                    for (x in 0..size.width.toInt() step step.toInt()) {
                        drawLine(Color.Black.copy(alpha = 0.02f), Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height))
                    }
                    for (y in 0..size.height.toInt() step step.toInt()) {
                        drawLine(Color.Black.copy(alpha = 0.02f), Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()))
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                content = content
            )
        }
    }
}

/**
 * Texto con fuente Monoespaciada y sombras retro.
 * Escala automáticamente según el factor de fuente configurado en Accesibilidad.
 */
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
    val scaledFontSize = fontSize * AccessibilityManager.fontScale
    val isHighContrast = AccessibilityManager.isHighContrastEnabled
    val textColor = if (isHighContrast) Color.Black else color

    val style = TextStyle(
        color = textColor,
        fontSize = scaledFontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        fontFamily = FontFamily.Monospace,
        lineHeight = scaledFontSize * 1.2f
    )

    val boxAlignment = when (textAlign) {
        TextAlign.Center -> Alignment.Center
        TextAlign.End -> Alignment.CenterEnd
        else -> Alignment.CenterStart
    }

    Box(
        modifier = modifier,
        contentAlignment = boxAlignment
    ) {
        if (showShadow && !isHighContrast) {
            Text(
                text = text,
                style = style.copy(color = shadowColor.copy(alpha = 0.3f)),
                modifier = Modifier.offset(x = 1.dp, y = 1.dp)
            )
        }
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
fun RetroHeader(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onHelpClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isSafariStyle: Boolean = false, // Si es true, usa el estilo verde antiguo para juegos
    rightContent: (@Composable () -> Unit)? = null,
    extraContent: (@Composable () -> Unit)? = null,
    subtitle: String? = null,
    titleAlignment: Alignment = Alignment.Center
) {
    val height = if (extraContent != null) 110.dp else 70.dp
    val isHighContrast = AccessibilityManager.isHighContrastEnabled
    // Colores dinámicos según el estilo
    val headerBg = if (isSafariStyle) {
        if (isHighContrast) Color.Black else Color(0xFF1B3022)
    } else {
        Color.White
    }
    val contentColor = if (isSafariStyle) Color.White else Color.Black
    val borderColor = if (isSafariStyle) {
        if (isHighContrast) Color.White else Color.White.copy(alpha = 0.3f)
    } else {
        Color.Black
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .drawBehind {
                if (!isSafariStyle) {
                    // Estilo Blanco (Sincronizado con menú inferior)
                    drawLine(
                        color = if (isHighContrast) Color.Black else Color(0xFF2D5A27),
                        start = Offset(0f, size.height - 4.dp.toPx()),
                        end = Offset(size.width, size.height - 4.dp.toPx()),
                        strokeWidth = 8.dp.toPx()
                    )
                    drawLine(
                        color = Color.Black,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                } else {
                    // Estilo Safari (Verde clásico para juegos)
                    drawLine(
                        color = if (isHighContrast) Color.White else Color.Black.copy(alpha = 0.4f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 6.dp.toPx()
                    )
                }
            },
        color = headerBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = height),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Back Button
                if (onBackClick != null) {
                    Box(modifier = Modifier.size(width = 80.dp, height = 40.dp), contentAlignment = Alignment.CenterStart) {
                        Surface(
                            onClick = onBackClick,
                            modifier = Modifier.size(40.dp),
                            shape = androidx.compose.ui.graphics.RectangleShape,
                            color = if (isSafariStyle) {
                                if (isHighContrast) Color.Black else Color(0xFF2D5A27)
                            } else {
                                if (isHighContrast) Color.White else Color(0xFFF5F5F5)
                            },
                            contentColor = contentColor,
                            border = BorderStroke(2.dp, if (isSafariStyle) borderColor else Color.Black)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                } else if (titleAlignment != Alignment.Center) {
                    // Si no hay botón atrás y no está centrado, reducimos el espacio para que el texto esté más a la izquierda
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    // Mantenemos el espacio para asegurar que el título centrado siga centrado si hay algo a la derecha
                    Box(modifier = Modifier.size(width = 80.dp, height = 40.dp))
                }

                // Center: Title
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    contentAlignment = titleAlignment
                ) {
                    val scaledTitleSize = 20.sp * AccessibilityManager.fontScale
                    val scaledSubtitleSize = 12.sp * AccessibilityManager.fontScale
                    
                    Column(
                        horizontalAlignment = if (titleAlignment == Alignment.Center) Alignment.CenterHorizontally else Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title.uppercase(),
                            color = contentColor,
                            fontSize = scaledTitleSize,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            textAlign = if (titleAlignment == Alignment.Center) TextAlign.Center else TextAlign.Start,
                            maxLines = 1
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle.uppercase(),
                                color = if (isSafariStyle) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                fontSize = scaledSubtitleSize,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = if (titleAlignment == Alignment.Center) TextAlign.Center else TextAlign.Start,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Right: Custom Content or Help
                Box(modifier = Modifier.size(width = 80.dp, height = 40.dp), contentAlignment = Alignment.CenterEnd) {
                    if (rightContent != null) {
                        rightContent()
                    } else if (onHelpClick != null) {
                        Surface(
                            onClick = onHelpClick,
                            modifier = Modifier.size(40.dp),
                            shape = androidx.compose.ui.graphics.RectangleShape,
                            color = if (isSafariStyle) {
                                if (isHighContrast) Color.Black else Color(0xFF2D5A27)
                            } else {
                                if (isHighContrast) Color.White else Color(0xFFF5F5F5)
                            },
                            contentColor = contentColor,
                            border = BorderStroke(2.dp, if (isSafariStyle) borderColor else Color.Black)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("?", fontWeight = FontWeight.Black, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }

            if (extraContent != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    extraContent()
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
    val isHighContrast = AccessibilityManager.isHighContrastEnabled
    
    Surface(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        shape = androidx.compose.ui.graphics.RectangleShape,
        color = if (isHighContrast) Color.Black else Color.White,
        contentColor = if (isHighContrast) Color.White else Color.Black,
        border = BorderStroke(2.dp, if (isHighContrast) Color.White else Color.Black)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    if (!isHighContrast) {
                        // Franja decorativa verde arriba para mantener consistencia con el header y bottom nav
                        drawRect(
                            color = Color(0xFF2D5A27),
                            topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                            size = androidx.compose.ui.geometry.Size(size.width, 4.dp.toPx())
                        )
                    }
                }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
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
    val isHighContrast = AccessibilityManager.isHighContrastEnabled
    val borderColor = if (isHighContrast) Color.Black else Color(0xFF1B3022)
    val mainColor = if (isHighContrast) Color.White else color
    
    Box(
        modifier = modifier
            .heightIn(min = 150.dp)
            .fillMaxWidth()
            .clickable { onClick() }
            .border(3.dp, borderColor, androidx.compose.ui.graphics.RectangleShape)
    ) {
        // Fondo base que llena todo
        Box(modifier = Modifier.matchParentSize().background(if (isHighContrast) Color.White else mainColor.copy(alpha = 0.15f)))
        
        Column(modifier = Modifier.matchParentSize()) {
            // Cabecera GBA Style
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isHighContrast) Color.Black else mainColor)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp * AccessibilityManager.fontScale,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            // Cuerpo
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = subtitle,
                    color = Color.Black.copy(alpha = 0.8f),
                    fontSize = 10.sp * AccessibilityManager.fontScale,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp * AccessibilityManager.fontScale
                )
            }
            
            // Footer de Recompensa
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.08f))
                    .drawBehind {
                        drawLine(
                            color = Color.Black.copy(alpha = 0.1f),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoItem(
                    label = rewardLabel, 
                    value = reward, 
                    color = if (isHighContrast) Color.Black else Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
fun RetroStatCard(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier,
    icon: String? = null
) {
    Box(
        modifier = modifier
            .border(2.dp, Color.Black, androidx.compose.ui.graphics.RectangleShape)
            .padding(1.dp)
            .border(1.dp, Color.White.copy(alpha = 0.3f), androidx.compose.ui.graphics.RectangleShape)
            .background(containerColor, androidx.compose.ui.graphics.RectangleShape)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 9.sp * AccessibilityManager.fontScale,
                fontWeight = FontWeight.Black,
                color = contentColor.copy(alpha = 0.8f),
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Text(icon, fontSize = 16.sp, modifier = Modifier.padding(end = 4.dp))
                }
                Text(
                    text = value,
                    fontSize = 20.sp * AccessibilityManager.fontScale,
                    fontWeight = FontWeight.Black,
                    color = contentColor,
                    fontFamily = FontFamily.Monospace,
                    style = TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.3f),
                            offset = Offset(2f, 2f),
                            blurRadius = 2f
                        )
                    )
                )
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
        val isHighContrast = AccessibilityManager.isHighContrastEnabled
        Text(
            text = label, 
            fontSize = 9.sp * AccessibilityManager.fontScale, 
            fontWeight = FontWeight.Black, 
            color = if (isHighContrast) Color.Black else Color.Black.copy(alpha = 0.5f),
            fontFamily = FontFamily.Monospace
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🪙",
                fontSize = 12.sp * AccessibilityManager.fontScale,
                modifier = Modifier.padding(end = 2.dp)
            )
            Text(
                text = value, 
                fontSize = 14.sp * AccessibilityManager.fontScale, 
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
    val infiniteTransition = rememberInfiniteTransition(label = "results")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    RetroBackground(
        modifier = Modifier.pointerInput(Unit) { detectTapGestures {} }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .offset(y = offsetY.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono de Resultado
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(if (isVictory) Color(0xFF4CAF50) else Color(0xFFE53935), androidx.compose.ui.graphics.RectangleShape)
                    .border(4.dp, Color.Black.copy(alpha = 0.3f), androidx.compose.ui.graphics.RectangleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isVictory) "🏆" else "❌",
                    fontSize = 48.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título Principal
            RetroText(
                text = title.uppercase(),
                fontSize = 36.sp,
                textAlign = TextAlign.Center,
                color = if (isVictory) Color.White else Color(0xFF1B3022)
            )

            // Subtítulo (Modo)
            RetroText(
                text = subtitle,
                color = if (isVictory) Color(0xFF2D5A27) else Color(0xFF1B3022).copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Descripción
            Text(
                text = description,
                color = Color(0xFF1B3022).copy(alpha = 0.8f),
                fontSize = 13.sp * AccessibilityManager.fontScale,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp * AccessibilityManager.fontScale,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Monedas Ganadas (Ahora sin recuadro, más limpio)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.05f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (coinsEarned >= 0) "+$coinsEarned" else "$coinsEarned",
                    color = if (coinsEarned >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                    fontSize = 24.sp * AccessibilityManager.fontScale,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("🪙", fontSize = 20.sp * AccessibilityManager.fontScale)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Botones de Acción Gigantes
            RetroButton(
                text = "INTENTAR DE NUEVO",
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                containerColor = Color(0xFF2D5A27),
                contentColor = Color.White,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            RetroButton(
                text = "VOLVER AL MENÚ",
                onClick = onExit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                containerColor = Color.DarkGray,
                contentColor = Color.White,
                fontSize = 18.sp
            )
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
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp, bottom = 40.dp)
            )

            // Contenedor de cartas centrado
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                var currentBatch = mutableListOf<DifficultyCardData>()
                var currentSpanSum = 0
                
                cards.forEach { card ->
                    if (currentSpanSum + card.span > columns) {
                        DifficultyRow(currentBatch, columns)
                        currentBatch = mutableListOf()
                        currentSpanSum = 0
                    }
                    currentBatch.add(card)
                    currentSpanSum += card.span
                }
                
                if (currentBatch.isNotEmpty()) {
                    DifficultyRow(currentBatch, columns)
                }
            }
        }
    }
}

@Composable
private fun DifficultyRow(rowCards: List<DifficultyCardData>, totalColumns: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        rowCards.forEach { card ->
            Box(
                modifier = Modifier.weight(card.span.toFloat())
            ) {
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
        
        val rowSpanSum = rowCards.sumOf { it.span }
        if (rowSpanSum < totalColumns) {
            Spacer(modifier = Modifier.weight((totalColumns - rowSpanSum).toFloat()))
        }
    }
}


@Composable
fun RetroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFF2D5A27).copy(alpha = 0.8f),
    contentColor: Color = Color.White,
    borderColor: Color = Color(0xFF1B3022),
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val isHighContrast = AccessibilityManager.isHighContrastEnabled
    
    var finalContainerColor = if (isHighContrast) {
        if (containerColor == Color.Gray) Color.DarkGray else Color.Black
    } else containerColor
    
    val finalContentColor = if (isHighContrast) Color.White else contentColor
    val finalBorderColor = if (isHighContrast) Color.White else borderColor
    val scaledFontSize = fontSize * AccessibilityManager.fontScale

    Box(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clickable(
                enabled = enabled,
                onClickLabel = if (AccessibilityManager.isScreenReaderOptimized) "Seleccionar $text" else null
            ) { 
                if (AccessibilityManager.isHapticFeedbackEnabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                onClick() 
            }
            .border(2.dp, finalBorderColor, androidx.compose.ui.graphics.RectangleShape)
            .padding(1.dp)
            .drawBehind {
                if (!isHighContrast) {
                    // Sombra interior sutil
                    drawRect(
                        color = Color.Black.copy(alpha = 0.15f),
                        topLeft = Offset(0f, size.height * 0.6f),
                        size = Size(size.width, size.height * 0.4f)
                    )
                }
            }
            .background(
                if (enabled) finalContainerColor else finalContainerColor.copy(alpha = 0.5f),
                androidx.compose.ui.graphics.RectangleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = if (enabled) finalContentColor else finalContentColor.copy(alpha = 0.5f),
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            fontSize = scaledFontSize,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
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
