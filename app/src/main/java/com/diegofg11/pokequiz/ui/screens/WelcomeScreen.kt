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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.models.LoginRequest
import com.diegofg11.pokequiz.ui.theme.*
import com.diegofg11.pokequiz.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundStart, BackgroundMid, BackgroundEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "POKÉQUIZ",
                color = GoldPoke,
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(RedPoke))
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(DarkPoke))
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.White))
                }
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(DarkPoke),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White))
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            if (isLoggedIn) {
                Button(
                    onClick = onContinueClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text("CONTINUAR", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPoke, contentColor = DarkPoke),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text("INICIAR SESIÓN", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedPoke, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text("NUEVO USUARIO", fontSize = 20.sp, fontWeight = FontWeight.Bold)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(BackgroundStart, BackgroundMid, BackgroundEnd)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "INICIAR SESIÓN",
                color = GoldPoke,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Tu Nombre", color = Color.White) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPoke, unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (errorMessage != null) {
                Text(errorMessage!!, color = RedPoke, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (username.isNotBlank()) {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                val response = withContext(Dispatchers.IO) {
                                    Network.api.login(LoginRequest(username.trim(), 1))
                                }
                                if (response.isSuccessful && response.body() != null) {
                                    val user = response.body()!!
                                    SessionManager.saveUserId(context, user.id)
                                    withContext(Dispatchers.Main) { onSuccess() }
                                } else {
                                    val errorBody = response.errorBody()?.string()
                                    errorMessage = if (errorBody?.contains("no existe") == true) "El usuario no existe. Regístrate." else "Error en el inicio de sesión."
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error de red."
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        errorMessage = "El nombre no puede estar vacío"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPoke, contentColor = DarkPoke),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) CircularProgressIndicator(color = DarkPoke, modifier = Modifier.size(24.dp))
                else Text("ENTRAR", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBack) {
                Text("VOLVER", color = Color.White, fontWeight = FontWeight.Bold)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(BackgroundStart, BackgroundMid, BackgroundEnd)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "NUEVO USUARIO",
                color = GoldPoke,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Tu Nombre", color = Color.White) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPoke, unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Elige tu inicial:", color = Color.White, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                StarterPokemon(1, selectedStarter) { selectedStarter = 1 }
                StarterPokemon(4, selectedStarter) { selectedStarter = 4 }
                StarterPokemon(7, selectedStarter) { selectedStarter = 7 }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (errorMessage != null) {
                Text(errorMessage!!, color = RedPoke, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (username.isNotBlank()) {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                val response = withContext(Dispatchers.IO) {
                                    Network.api.register(LoginRequest(username.trim(), selectedStarter))
                                }
                                if (response.isSuccessful && response.body() != null) {
                                    val user = response.body()!!
                                    SessionManager.saveUserId(context, user.id)
                                    withContext(Dispatchers.Main) { onSuccess() }
                                } else {
                                    errorMessage = "Ese nombre de usuario ya existe u ocurrió un error."
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error de red."
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        errorMessage = "El nombre no puede estar vacío"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = RedPoke, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("REGISTRARSE", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBack) {
                Text("VOLVER", color = Color.White, fontWeight = FontWeight.Bold)
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
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) GoldPoke else Color.Gray,
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
