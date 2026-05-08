package com.diegofg11.pokequiz.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontFamily
import com.diegofg11.pokequiz.utils.SoundManager
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush as GraphicsBrush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.models.User
import com.diegofg11.pokequiz.ui.components.*
import com.diegofg11.pokequiz.ui.theme.*
import com.diegofg11.pokequiz.utils.SessionManager
import com.diegofg11.pokequiz.utils.WallpaperManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.style.TextAlign

@Composable
fun UserScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<User?>(null) }
    var pokedexCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAccessibilityDialog by remember { mutableStateOf(false) }
    var selectedAvatar by remember { mutableStateOf(com.diegofg11.pokequiz.utils.AvatarManager.getSelectedAvatar(context)) }

    LaunchedEffect(Unit) {
        try {
            val userId = SessionManager.currentUserId
            val userResp = withContext(Dispatchers.IO) { Network.api.getUser(userId) }
            val pcResp = withContext(Dispatchers.IO) { Network.api.getPc(userId) }
            
            if (userResp.isSuccessful) {
                user = userResp.body()
            }
            if (pcResp.isSuccessful) {
                pokedexCount = pcResp.body()?.size ?: 0
            }
        } catch (e: Exception) {
            errorMessage = "Error al cargar datos del usuario"
        } finally {
            isLoading = false
        }
    }

    RetroBackground {
        if (errorMessage != null) {
            PokemonAlertDialog(
                title = "¡Error!",
                message = errorMessage!!,
                isError = true,
                onDismiss = { errorMessage = null }
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldPoke)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // TARJETA DE ENTRENADOR GBA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White) // Blanco estándar de las cajas retro de la app
                        .border(3.dp, Color.Black, androidx.compose.ui.graphics.RectangleShape)
                ) {
                    val displayUser = user
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Banner superior
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2D5A27)) // Verde oscuro principal de la Zona Safari
                                .padding(vertical = 8.dp)
                        ) {
                            RetroText(
                                text = "TARJETA ENTRENADOR",
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                        HorizontalDivider(thickness = 3.dp, color = Color.Black)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Contenido de la tarjeta
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Nº ID",
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "000${SessionManager.currentUserId}",
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "NOMBRE",
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = (displayUser?.nombre ?: "INVITADO").uppercase(),
                                    fontSize = 14.sp,
                                    color = Color(0xFF2D5A27), // Resaltado con el verde Safari
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "MONEDAS",
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "🪙 ${displayUser?.monedasGacha ?: 0}",
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "POKÉDEX",
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = pokedexCount.toString(),
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                            
                            // Sprite de Entrenador (Placeholder pixelado)
                            // Sprite de Entrenador (Personalizado)
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .border(2.dp, Color.Black, androidx.compose.ui.graphics.RectangleShape)
                                    .background(Color.White)
                                    .clickable { showAvatarDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = selectedAvatar.resId),
                                    contentDescription = "Avatar de ${selectedAvatar.name}",
                                    modifier = Modifier.fillMaxSize().padding(4.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Menú de Acciones
                // Menú de Acciones
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(3.dp, Color.Black, androidx.compose.ui.graphics.RectangleShape)
                        .padding(8.dp)
                ) {
                    Column {
                        ActionMenuItem(
                            icon = Icons.Default.Edit,
                            title = "PERSONALIZAR MAPA",
                            subtitle = "Cambiar fondo de pantalla",
                            onClick = { showWallpaperDialog = true }
                        )
                        HorizontalDivider(thickness = 2.dp, color = Color.Black)
                        ActionMenuItem(
                            icon = Icons.Default.Edit,
                            title = "PERSONALIZAR ENTRENADOR",
                            subtitle = "Cambiar imagen de perfil",
                            onClick = { showAvatarDialog = true }
                        )
                        HorizontalDivider(thickness = 2.dp, color = Color.Black)
                        ActionMenuItem(
                            icon = Icons.Default.Settings,
                            title = "AJUSTES DE CUENTA",
                            subtitle = "Cerrar sesión y otros",
                            onClick = { showSettingsDialog = true }
                        )
                    }
                }
            }
        }

        // --- WALLPAPER DIALOG ---
        if (showWallpaperDialog) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                RetroMenuBox(
                    modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 40.dp),
                    backgroundColor = Color.White,
                    borderColor = GoldPoke
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        RetroText(text = "SELECCIONAR FONDO", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val wallpapers = WallpaperManager.getAllWallpapers()
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.height(300.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(wallpapers) { index, resId ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .border(2.dp, Color.Black.copy(alpha = 0.1f), androidx.compose.ui.graphics.RectangleShape)
                                        .clip(androidx.compose.ui.graphics.RectangleShape)
                                        .clickable {
                                            WallpaperManager.setSelectedWallpaper(context, index)
                                            showWallpaperDialog = false
                                            Toast.makeText(context, "Fondo actualizado", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        RetroButton(
                            text = "CANCELAR",
                            onClick = { showWallpaperDialog = false },
                            containerColor = Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // --- AVATAR DIALOG ---
        if (showAvatarDialog) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                RetroMenuBox(
                    modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 40.dp),
                    backgroundColor = Color.White,
                    borderColor = GoldPoke
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        RetroText(text = "SELECCIONAR ENTRENADOR", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val avatars = com.diegofg11.pokequiz.utils.AvatarManager.availableAvatars
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.height(300.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(avatars) { _, avatar ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (selectedAvatar.id == avatar.id) 2.dp else 1.dp,
                                            color = if (selectedAvatar.id == avatar.id) GoldPoke else Color.Black.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            com.diegofg11.pokequiz.utils.AvatarManager.setSelectedAvatar(context, avatar.id)
                                            selectedAvatar = avatar
                                            showAvatarDialog = false
                                            Toast.makeText(context, "Entrenador actualizado", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(8.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = avatar.resId),
                                        contentDescription = avatar.name,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.size(60.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = avatar.name,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        RetroButton(
                            text = "CANCELAR",
                            onClick = { showAvatarDialog = false },
                            containerColor = Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // --- SETTINGS DIALOG ---
        if (showSettingsDialog) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                RetroMenuBox(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    backgroundColor = Color.White,
                    borderColor = GoldPoke
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        RetroText(text = "AJUSTES", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(24.dp))

                        // Control de Volumen
                        var volume by remember { mutableStateOf(SoundManager.getVolume(context)) }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "VOLUMEN: ${(volume * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Black
                            )
                        }
                        
                        Slider(
                            value = volume,
                            onValueChange = { 
                                volume = it
                                SoundManager.setVolume(context, it)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = GoldPoke,
                                activeTrackColor = GoldPoke,
                                inactiveTrackColor = Color.LightGray
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        RetroButton(
                            text = "DAR MONEDAS (CHEATS)",
                            onClick = {
                                scope.launch {
                                    try {
                                        val resp = withContext(Dispatchers.IO) {
                                            Network.api.safariReward(com.diegofg11.pokequiz.models.SafariRewardRequest(
                                                userId = SessionManager.currentUserId,
                                                coinsEarned = 100,
                                                gameType = "MEMORIA",
                                                difficulty = "EASY"
                                            ))
                                        }
                                        if (resp.isSuccessful) {
                                            user = resp.body()
                                            Toast.makeText(context, "¡100 Monedas añadidas!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val errorBody = resp.errorBody()?.string()
                                            Toast.makeText(context, "Error: ${errorBody ?: resp.message()}", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            containerColor = Color(0xFFC62828),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        RetroButton(
                            text = "CERRAR SESIÓN",
                            onClick = {
                                SessionManager.logout(context)
                                showSettingsDialog = false
                                onLogout()
                            },
                            containerColor = RedPoke,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        RetroButton(
                            text = "ACCESIBILIDAD",
                            onClick = { showAccessibilityDialog = true },
                            containerColor = Color(0xFF1976D2),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        RetroButton(
                            text = "VOLVER",
                            onClick = { showSettingsDialog = false },
                            containerColor = Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // --- ACCESSIBILITY DIALOG ---
        if (showAccessibilityDialog) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                RetroMenuBox(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    backgroundColor = Color.White,
                    borderColor = Color(0xFF1976D2)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        RetroText(text = "ACCESIBILIDAD", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(20.dp))

                        val accessibilityManager = com.diegofg11.pokequiz.utils.AccessibilityManager

                        // High Contrast Toggle
                        AccessibilityToggle(
                            label = "ALTO CONTRASTE",
                            checked = accessibilityManager.isHighContrastEnabled,
                            onCheckedChange = { accessibilityManager.setHighContrast(context, it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Haptic Feedback Toggle
                        AccessibilityToggle(
                            label = "VIBRACIÓN",
                            checked = accessibilityManager.isHapticFeedbackEnabled,
                            onCheckedChange = { accessibilityManager.setHapticFeedback(context, it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Screen Reader Toggle
                        AccessibilityToggle(
                            label = "OPTIMIZAR LECTOR",
                            checked = accessibilityManager.isScreenReaderOptimized,
                            onCheckedChange = { accessibilityManager.setScreenReaderOptimization(context, it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Font Scale Slider
                        Text(
                            text = "TAMAÑO FUENTE: ${String.format("%.1fx", accessibilityManager.fontScale)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )
                        Slider(
                            value = accessibilityManager.fontScale,
                            onValueChange = { accessibilityManager.setFontScale(context, it) },
                            valueRange = 0.8f..1.5f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF1976D2),
                                activeTrackColor = Color(0xFF1976D2),
                                inactiveTrackColor = Color.LightGray
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Color Blind Mode Selector
                        Text(
                            text = "MODO DALTÓNICOS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val modes = com.diegofg11.pokequiz.utils.ColorBlindMode.values()
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            modes.forEach { mode ->
                                val isSelected = accessibilityManager.colorBlindMode == mode
                                Surface(
                                    modifier = Modifier.clickable { accessibilityManager.setColorBlindMode(context, mode) },
                                    color = if (isSelected) Color(0xFF1976D2) else Color.LightGray,
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, Color.Black)
                                ) {
                                    Text(
                                        text = mode.name,
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color.White else Color.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        RetroButton(
                            text = "ACEPTAR",
                            onClick = { showAccessibilityDialog = false },
                            containerColor = Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccessibilityToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color.Black
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF1976D2),
                checkedTrackColor = Color(0xFF1976D2).copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label, 
            fontSize = 10.sp, 
            color = Color.Gray, 
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
        Text(
            text = value, 
            fontSize = 18.sp, 
            fontWeight = FontWeight.Bold, 
            color = Color(0xFF1B3022),
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

@Composable
fun ActionMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "▶",
            fontSize = 14.sp,
            color = Color.Black,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Black,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subtitle, 
                color = Color.DarkGray, 
                fontSize = 10.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}
