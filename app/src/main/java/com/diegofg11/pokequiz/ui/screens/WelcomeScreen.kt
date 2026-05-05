package com.diegofg11.pokequiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.ui.components.*
import com.diegofg11.pokequiz.ui.theme.*
import com.diegofg11.pokequiz.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.font.FontFamily

@Composable
fun WelcomeScreen(onEnterClick: () -> Unit = {}) {
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
            RetroText(
                text = "POKÉQUIZ",
                fontSize = 48.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Logo de Pokebola con estilo retro
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .border(4.dp, Color(0xFF1B3022), CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFFE53935)))
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Color(0xFF1B3022)))
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.White))
                }
                Box(
                    modifier = Modifier.size(40.dp).border(4.dp, Color(0xFF1B3022), CircleShape).clip(CircleShape).background(Color(0xFF1B3022)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.White))
                }
            }
            
            Spacer(modifier = Modifier.height(56.dp))
            
            RetroMenuBox(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.Black.copy(alpha = 0.05f),
                borderColor = Color(0xFF2D5A27)
            ) {
                if (isLoggedIn) {
                    RetroButton(
                        text = "CONTINUAR",
                        onClick = onContinueClick,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                RetroButton(
                    text = "INICIAR SESIÓN",
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                RetroButton(
                    text = "NUEVO USUARIO",
                    onClick = onRegisterClick,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color(0xFFE53935).copy(alpha = 0.8f)
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
                text = "INICIAR SESIÓN",
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
                    label = { Text("TU NOMBRE", color = Color(0xFF1B3022), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
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
                    text = if (isLoading) "..." else "ENTRAR",
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
                                        withContext(Dispatchers.Main) { onSuccess() }
                                    } else {
                                        val errorBody = response.errorBody()?.string()
                                        errorMessage = if (errorBody?.contains("no existe") == true) "EL USUARIO NO EXISTE.\nREGÍSTRATE." else "ERROR EN EL INICIO DE SESIÓN."
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "ERROR DE RED."
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            errorMessage = "EL NOMBRE NO PUEDE ESTAR VACÍO"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onBack) {
                Text(
                    "VOLVER AL MENÚ",
                    color = Color(0xFF1B3022).copy(alpha = 0.8f),
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
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
                text = "NUEVO USUARIO",
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
                    label = { Text("TU NOMBRE", color = Color(0xFF1B3022), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
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
                    "ELIGE TU INICIAL:", 
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
                    text = if (isLoading) "..." else "REGISTRARSE",
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
                                        withContext(Dispatchers.Main) { onSuccess() }
                                    } else {
                                        errorMessage = "ESE NOMBRE YA EXISTE\nO OCURRIÓ UN ERROR."
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "ERROR DE RED."
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            errorMessage = "EL NOMBRE NO PUEDE ESTAR VACÍO"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    containerColor = Color(0xFFE53935).copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onBack) {
                Text(
                    "VOLVER AL MENÚ", 
                    color = Color(0xFF1B3022).copy(alpha = 0.8f), 
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
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
            .clip(CircleShape)
            .background(if (isSelected) Color.White.copy(alpha = 0.3f) else Color.Transparent)
            .border(
                width = if (isSelected) 4.dp else 2.dp,
                color = if (isSelected) Color(0xFF1B3022) else Color(0xFF1B3022).copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Starter",
            modifier = Modifier.size(60.dp)
        )
    }
}
