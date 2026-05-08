package com.diegofg11.pokequiz.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.R
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.models.User
import com.diegofg11.pokequiz.ui.components.RetroButton
import com.diegofg11.pokequiz.ui.components.RetroMenuBox
import com.diegofg11.pokequiz.ui.components.RetroText
import com.diegofg11.pokequiz.ui.theme.GoldPoke
import com.diegofg11.pokequiz.ui.theme.RedPoke
import com.diegofg11.pokequiz.utils.AvatarManager
import com.diegofg11.pokequiz.utils.Logger
import com.diegofg11.pokequiz.utils.SafariUtils
import com.diegofg11.pokequiz.utils.SessionManager
import com.diegofg11.pokequiz.utils.SoundManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun UserScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAccessibilityDialog by remember { mutableStateOf(false) }

    // Cargar datos del usuario
    LaunchedEffect(Unit) {
        try {
            val response = withContext(Dispatchers.IO) {
                Network.api.getUser(SessionManager.currentUserId)
            }
            if (response.isSuccessful) {
                user = response.body()
            }
        } catch (e: Exception) {
            Logger.e(context, "UserScreen", "Error loading user profile", e)
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldPoke)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header con Foto y Nombre
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = AvatarManager.getAvatarUrl(user?.avatarId ?: 1),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(4.dp, GoldPoke, CircleShape)
                            .background(Color.White),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                RetroText(
                    text = user?.username ?: "Trainer",
                    fontSize = 22.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Estadísticas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard(label = stringResource(R.string.level), value = user?.nivelProgreso?.toString() ?: "1")
                    StatCard(label = stringResource(R.string.coins), value = user?.monedas?.toString() ?: "0")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botones de Acción
                RetroButton(
                    text = stringResource(R.string.accessibility_title),
                    onClick = { showAccessibilityDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color(0xFF2D5A27)
                )

                Spacer(modifier = Modifier.height(12.dp))

                RetroButton(
                    text = stringResource(R.string.settings),
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
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
                        RetroText(text = stringResource(R.string.settings), fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(24.dp))

                        // Control de Volumen
                        var volume by remember { mutableStateOf(SoundManager.getVolume(context)) }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.volume_label, (volume * 100).toInt()),
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

                        // Control de Idioma
                        var currentLang by remember { mutableStateOf(com.diegofg11.pokequiz.utils.SessionManager.getLanguage(context)) }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.language_label),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Black
                            )
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RetroButton(
                                text = "ES",
                                onClick = { 
                                    com.diegofg11.pokequiz.utils.SessionManager.setLanguage(context, "es")
                                    currentLang = "es"
                                    var actContext = context
                                    while (actContext is android.content.ContextWrapper) {
                                        if (actContext is android.app.Activity) break
                                        actContext = actContext.baseContext
                                    }
                                    (actContext as? android.app.Activity)?.recreate()
                                },
                                modifier = Modifier.weight(1f),
                                containerColor = if (currentLang == "es") GoldPoke else Color.LightGray,
                                contentColor = if (currentLang == "es") Color.White else Color.Black
                            )
                            RetroButton(
                                text = "EN",
                                onClick = { 
                                    com.diegofg11.pokequiz.utils.SessionManager.setLanguage(context, "en")
                                    currentLang = "en"
                                    var actContext = context
                                    while (actContext is android.content.ContextWrapper) {
                                        if (actContext is android.app.Activity) break
                                        actContext = actContext.baseContext
                                    }
                                    (actContext as? android.app.Activity)?.recreate()
                                },
                                modifier = Modifier.weight(1f),
                                containerColor = if (currentLang == "en") GoldPoke else Color.LightGray,
                                contentColor = if (currentLang == "en") Color.White else Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        RetroButton(
                            text = stringResource(R.string.free_coins_btn) + " (CHEAT)",
                            onClick = {
                                Logger.i(context, "UserScreen", "Usuario activó cheat de monedas")
                                SafariUtils.rewardUser(
                                    scope = scope,
                                    coins = 100,
                                    gameType = "cheat",
                                    difficulty = "none",
                                    onSuccess = {
                                        scope.launch {
                                            val userId = SessionManager.currentUserId
                                            val userResp = withContext(Dispatchers.IO) { Network.api.getUser(userId) }
                                            if (userResp.isSuccessful) {
                                                user = userResp.body()
                                                Toast.makeText(context, context.getString(R.string.coins_added_success), Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    onError = { errorMsg -> 
                                        Logger.e(context, "UserScreen", "Error en cheat de monedas: $errorMsg")
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show() 
                                    }
                                )
                            },
                            containerColor = GoldPoke,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        RetroButton(
                            text = stringResource(R.string.logout),
                            onClick = {
                                Logger.i(context, "UserScreen", "Cierre de sesión")
                                SessionManager.logout(context)
                                showSettingsDialog = false
                                onLogout()
                            },
                            containerColor = RedPoke,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        RetroButton(
                            text = stringResource(R.string.go_back),
                            onClick = { showSettingsDialog = false },
                            containerColor = Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // --- DIÁLOGO DE ACCESIBILIDAD ---
        if (showAccessibilityDialog) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                RetroMenuBox(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    backgroundColor = Color.White,
                    borderColor = GoldPoke
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        RetroText(text = stringResource(R.string.accessibility_title), fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(20.dp))

                        val accessibilityManager = com.diegofg11.pokequiz.utils.AccessibilityManager

                        AccessibilityToggle(
                            label = stringResource(R.string.accessibility_high_contrast),
                            checked = accessibilityManager.isHighContrastEnabled,
                            onCheckedChange = { accessibilityManager.setHighContrast(context, it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AccessibilityToggle(
                            label = stringResource(R.string.accessibility_vibration),
                            checked = accessibilityManager.isHapticFeedbackEnabled,
                            onCheckedChange = { accessibilityManager.setHapticFeedback(context, it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AccessibilityToggle(
                            label = stringResource(R.string.accessibility_screen_reader),
                            checked = accessibilityManager.isScreenReaderOptimized,
                            onCheckedChange = { accessibilityManager.setScreenReaderOptimization(context, it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.accessibility_font_size, String.format("%.1fx", accessibilityManager.fontScale)),
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
                                thumbColor = GoldPoke,
                                activeTrackColor = Color(0xFF2D5A27),
                                inactiveTrackColor = Color.LightGray
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        RetroButton(
                            text = stringResource(R.string.go_back),
                            onClick = { showAccessibilityDialog = false },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        border = BorderStroke(2.dp, GoldPoke)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(text = value, fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Black)
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
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = GoldPoke)
        )
    }
}
