package com.diegofg11.pokequiz.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.style.TextAlign

@Composable
fun UserScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    var user by remember { mutableStateOf<User?>(null) }
    var pokedexCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

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
                                text = "TRAINER CARD",
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
                                    text = "IDNo.",
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
                                    text = "NAME",
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
                                    text = "MONEY",
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$${displayUser?.monedasGacha ?: 0}",
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "POKéDEX",
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
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .border(2.dp, Color.Black, androidx.compose.ui.graphics.RectangleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayUser?.nombre?.firstOrNull()?.uppercase() ?: "?",
                                    fontSize = 40.sp,
                                    color = Color(0xFF6C63FF),
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Black
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
                                        .border(2.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .clip(RoundedCornerShape(4.dp))
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
                            text = "VOLVER",
                            onClick = { showSettingsDialog = false },
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
