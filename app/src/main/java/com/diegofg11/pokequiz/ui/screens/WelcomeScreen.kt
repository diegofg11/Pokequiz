package com.diegofg11.pokequiz.ui.screens
 
import androidx.compose.animation.core.*

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.R
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.ui.components.*
import com.diegofg11.pokequiz.ui.theme.*
import com.diegofg11.pokequiz.utils.SessionManager
import com.diegofg11.pokequiz.utils.TutorialManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.font.FontFamily
import com.diegofg11.pokequiz.utils.SoundManager
import androidx.compose.ui.platform.LocalContext

/**
 * @authors: Gaizka, Diego y Xiker
 * Pantalla de Bienvenida y Gestión de Sesión.
 * Actúa como orquestador para mostrar el menú inicial, login o registro.
 * 
 * @param onEnterClick Callback que se dispara cuando el usuario accede al juego principal.
 */
@Composable
fun WelcomeScreen(onEnterClick: () -> Unit = {}) {
    val context = LocalContext.current
    var screenState by remember { mutableStateOf("welcome") }

    when (screenState) {
        "welcome" -> WelcomeMenuScreen(
            onLoginClick = { screenState = "login" },
            onRegisterClick = { screenState = "register" },
            onContinueClick = onEnterClick
        )
        "login" -> LoginScreen(
            onBack = { screenState = "welcome" },
            onSuccess = onEnterClick
        )
        "register" -> RegisterScreen(
            onBack = { screenState = "welcome" },
            onSuccess = onEnterClick
        )
    }
}

@Composable
fun WelcomeMenuScreen(onLoginClick: () -> Unit, onRegisterClick: () -> Unit, onContinueClick: () -> Unit) {
    val isLoggedIn = SessionManager.currentUserId != -1
    
    RetroBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = com.diegofg11.pokequiz.R.drawable.logo_welcome),
                contentDescription = stringResource(R.string.desc_logo),
                modifier = Modifier
                    .size(300.dp)
                    .padding(bottom = 16.dp)
            )
            
            val infiniteTransition = rememberInfiniteTransition(label = "buttons")
            val offsetY by infiniteTransition.animateFloat(
                initialValue = -4f,
                targetValue = 4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offset"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .offset(y = offsetY.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoggedIn) {
                    RetroButton(
                        text = stringResource(R.string.continue_btn),
                        onClick = onContinueClick,
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        containerColor = Color(0xFF4CAF50),
                        fontSize = 20.sp
                    )
                }
                
                RetroButton(
                    text = stringResource(R.string.login_btn),
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    fontSize = 20.sp
                )

                RetroButton(
                    text = stringResource(R.string.new_user_btn),
                    onClick = onRegisterClick,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    containerColor = Color(0xFFE53935),
                    fontSize = 20.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onBack: () -> Unit, onSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    RetroBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            RetroText(
                text = stringResource(R.string.login_title),
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            RetroMenuBox(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.1f),
                borderColor = Color(0xFF1B3022)
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.your_name_label), color = Color(0xFF1B3022), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1B3022),
                        unfocusedBorderColor = Color(0xFF1B3022).copy(alpha = 0.5f),
                        focusedTextColor = Color(0xFF1B3022),
                        unfocusedTextColor = Color(0xFF1B3022),
                        focusedLabelColor = Color(0xFF1B3022),
                        unfocusedLabelColor = Color(0xFF1B3022).copy(alpha = 0.7f)
                    ),
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (errorMessage != null) {
                    Text(
                        errorMessage!!,
                        color = Color(0xFFB71C1C),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                RetroButton(
                    text = if (isLoading) stringResource(R.string.loading_dots) else stringResource(R.string.enter_btn),
                    onClick = {
                        if (username.isNotBlank()) {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    val response = withContext(Dispatchers.IO) {
                                        Network.api.login(com.diegofg11.pokequiz.models.LoginRequest(username.trim(), 1))
                                    }
                                    if (response.isSuccessful && response.body() != null) {
                                        val user = response.body()!!
                                        SessionManager.saveUserId(context, user.id)
                                        user.token?.let { SessionManager.saveToken(context, it) }
                                        withContext(Dispatchers.Main) { onSuccess() }
                                    } else {
                                        val errorJson = response.errorBody()?.string()
                                        val message = try {
                                            val obj = org.json.JSONObject(errorJson)
                                            obj.getString("error")
                                        } catch (e: Exception) {
                                            errorJson ?: context.getString(R.string.login_failed)
                                        }
                                        errorMessage = message
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "${context.getString(R.string.connection_error_prefix)} ${e.localizedMessage}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            errorMessage = context.getString(R.string.name_empty_error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            RetroButton(
                text = stringResource(R.string.back_to_menu),
                onClick = onBack,
                containerColor = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(0.6f).height(44.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onBack: () -> Unit, onSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var selectedStarter by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    RetroBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            RetroText(
                text = stringResource(R.string.register_title),
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            RetroMenuBox(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.1f),
                borderColor = Color(0xFF1B3022)
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.your_name_label), color = Color(0xFF1B3022), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1B3022),
                        unfocusedBorderColor = Color(0xFF1B3022).copy(alpha = 0.5f),
                        focusedTextColor = Color(0xFF1B3022),
                        unfocusedTextColor = Color(0xFF1B3022),
                        focusedLabelColor = Color(0xFF1B3022),
                        unfocusedLabelColor = Color(0xFF1B3022).copy(alpha = 0.7f)
                    ),
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    stringResource(R.string.choose_starter), 
                    color = Color(0xFF1B3022), 
                    fontSize = 14.sp, 
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StarterPokemon(1, selectedStarter) { selectedStarter = 1 }
                    StarterPokemon(4, selectedStarter) { selectedStarter = 4 }
                    StarterPokemon(7, selectedStarter) { selectedStarter = 7 }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (errorMessage != null) {
                    Text(
                        errorMessage!!, 
                        color = Color(0xFFB71C1C), 
                        fontWeight = FontWeight.Bold, 
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                RetroButton(
                    text = if (isLoading) stringResource(R.string.loading_dots) else stringResource(R.string.register_btn),
                    onClick = {
                        if (username.isNotBlank()) {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    val response = withContext(Dispatchers.IO) {
                                        Network.api.register(com.diegofg11.pokequiz.models.LoginRequest(username.trim(), selectedStarter))
                                    }
                                    if (response.isSuccessful && response.body() != null) {
                                        val user = response.body()!!
                                        SessionManager.saveUserId(context, user.id)
                                        user.token?.let { SessionManager.saveToken(context, it) }
                                        SessionManager.setFirstTime(context, true)
                                        TutorialManager.startTutorial(context)
                                        withContext(Dispatchers.Main) { onSuccess() }
                                    } else {
                                        val errorJson = response.errorBody()?.string()
                                        val message = try {
                                            val obj = org.json.JSONObject(errorJson)
                                            obj.getString("error")
                                        } catch (e: Exception) {
                                            errorJson ?: context.getString(R.string.unknown_error)
                                        }
                                        errorMessage = message
                                    }
                                } catch (e: Exception) {
                                    errorMessage = context.getString(R.string.network_error)
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            errorMessage = context.getString(R.string.name_empty_error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    containerColor = Color(0xFFE53935).copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            RetroButton(
                text = stringResource(R.string.back_to_menu),
                onClick = onBack,
                containerColor = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(0.6f).height(44.dp)
            )
        }
    }
}

@Composable
fun StarterPokemon(id: Int, selectedId: Int, onClick: () -> Unit) {
    val isSelected = id == selectedId
    val imageUrl = "${Network.BASE_URL}public/sprites/${id}_front.png"
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(if (isSelected) Color.White.copy(alpha = 0.3f) else Color.Transparent)
            .border(
                width = if (isSelected) 4.dp else 2.dp,
                color = if (isSelected) Color(0xFF1B3022) else Color(0xFF1B3022).copy(alpha = 0.3f),
                shape = androidx.compose.ui.graphics.RectangleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = stringResource(R.string.desc_starter),
            modifier = Modifier.size(60.dp)
        )
    }
}
