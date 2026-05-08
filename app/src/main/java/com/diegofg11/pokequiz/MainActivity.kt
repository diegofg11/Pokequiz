package com.diegofg11.pokequiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import com.diegofg11.pokequiz.ui.screens.BattleScreen
import com.diegofg11.pokequiz.ui.screens.GachaScreen
import com.diegofg11.pokequiz.ui.screens.MapScreen
import com.diegofg11.pokequiz.ui.screens.MinigamesScreen
import com.diegofg11.pokequiz.ui.screens.PCScreen
import com.diegofg11.pokequiz.ui.screens.UserScreen
import com.diegofg11.pokequiz.ui.screens.WelcomeScreen
import com.diegofg11.pokequiz.ui.theme.*
import com.diegofg11.pokequiz.ui.components.PokeBallIcon
import com.diegofg11.pokequiz.ui.components.PokemonAlertDialog
import kotlinx.coroutines.launch
import androidx.navigation.compose.currentBackStackEntryAsState
import com.diegofg11.pokequiz.ui.components.TutorialBox
import com.diegofg11.pokequiz.utils.SessionManager
import com.diegofg11.pokequiz.utils.TutorialManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.diegofg11.pokequiz.utils.SessionManager.init(this)
        com.diegofg11.pokequiz.utils.AccessibilityManager.init(this)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent {
            PokequizTheme {
                val navController = rememberNavController()
                var completedLevel by remember { mutableIntStateOf(0) }
                var selectedItem by remember { mutableIntStateOf(0) }
                var globalErrorMessage by remember { mutableStateOf<String?>(null) }

                val scope = rememberCoroutineScope()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // --- GESTIÓN GLOBAL DE MÚSICA ---
                val context = androidx.compose.ui.platform.LocalContext.current
                val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

                // Observador de ciclo de vida para pausar/reanudar al salir de la app
                DisposableEffect(lifecycleOwner) {
                    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                        when (event) {
                            androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> com.diegofg11.pokequiz.utils.SoundManager.pauseMusic()
                            androidx.lifecycle.Lifecycle.Event.ON_RESUME -> com.diegofg11.pokequiz.utils.SoundManager.resumeMusic()
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                LaunchedEffect(currentRoute) {
                    kotlinx.coroutines.delay(300) // Pequeo delay para evitar cortes
                    when {
                        currentRoute == "welcome" -> {
                            com.diegofg11.pokequiz.utils.SoundManager.playMusic(context, R.raw.menu_music, volumeFactor = 0.5f)
                        }
                        currentRoute?.startsWith("battle") == true -> {
                            com.diegofg11.pokequiz.utils.SoundManager.playMusic(context, R.raw.battle_song, volumeFactor = 1.0f)
                        }
                        currentRoute?.startsWith("safari_zone") == true -> {
                            com.diegofg11.pokequiz.utils.SoundManager.playMusic(context, R.raw.battle_song, volumeFactor = 1.0f)
                        }
                        else -> {
                            com.diegofg11.pokequiz.utils.SoundManager.playMusic(context, R.raw.menu_music, volumeFactor = 0.5f)
                        }
                    }
                }

                // Refetch user data when currentUserId changes and is valid
                LaunchedEffect(com.diegofg11.pokequiz.utils.SessionManager.currentUserId) {
                    if (com.diegofg11.pokequiz.utils.SessionManager.currentUserId != -1) {
                        scope.launch {
                            try {
                                val response =
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                        com.diegofg11.pokequiz.api.Network.api.getUser(com.diegofg11.pokequiz.utils.SessionManager.currentUserId)
                                    }
                                if (response.isSuccessful && response.body() != null) {
                                    completedLevel = response.body()!!.nivelProgreso
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("MainActivity", "Error fetching user", e)
                                globalErrorMessage =
                                    "No se pudo conectar con el servidor para cargar tu perfil."
                            }
                        }
                    }
                }

                if (globalErrorMessage != null) {
                    PokemonAlertDialog(
                        title = "¡Error de Conexión!",
                        message = globalErrorMessage!!,
                        isError = true,
                        onDismiss = { globalErrorMessage = null }
                    )
                }

                // --- GESTIÓN DEL TUTORIAL ---
                com.diegofg11.pokequiz.utils.TutorialManager.init(context)

                val tutorialSteps = listOf(
                    "map" to ("EL MAPA" to "Aquí verás los niveles disponibles. Pulsa en uno para empezar una batalla y progresar."),
                    "pc" to ("TU EQUIPO (PC)" to "En el PC podrás gestionar tus Pokémon y elegir quiénes te acompañan en batalla."),
                    "gacha" to ("EL BAZAR" to "¡Usa tus monedas aquí para conseguir nuevos Pokémon aleatorios!"),
                    "games" to ("ZONA SAFARI" to "¡Gana monedas extra jugando a divertidos minijuegos de tipos y memoria!"),
                    "user" to ("TU PERFIL" to "Personaliza tu avatar y el fondo de pantalla desde aquí.")
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            val isFullScreen = currentRoute?.startsWith("battle") == true ||
                                    currentRoute == "welcome" ||
                                    currentRoute?.startsWith("safari_zone") == true

                            if (!isFullScreen) {
                                RetroBottomNavigation(
                                    currentRoute = currentRoute,
                                    onNavigate = { route ->
                                        navController.navigate(route) {
                                            if (route == "map") {
                                                popUpTo("map") { inclusive = true }
                                            } else {
                                                popUpTo("map") { saveState = true }
                                                restoreState = true
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "welcome",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("welcome") {
                                WelcomeScreen(
                                    onEnterClick = {
                                        navController.navigate("map") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("map") {
                                MapScreen(
                                    completedLevel = completedLevel,
                                    onNavigateToBattle = { levelId ->
                                        navController.navigate("battle/$levelId")
                                    }
                                )
                            }
                            composable("battle/{levelId}") { backStackEntry ->
                                val levelId =
                                    backStackEntry.arguments?.getString("levelId")?.toIntOrNull() ?: 1
                                BattleScreen(
                                    levelId = levelId,
                                    onBattleWin = {
                                        scope.launch {
                                            try {
                                                val response =
                                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                        com.diegofg11.pokequiz.api.Network.api.getUser(
                                                            com.diegofg11.pokequiz.utils.SessionManager.currentUserId
                                                        )
                                                    }
                                                if (response.isSuccessful && response.body() != null) {
                                                    completedLevel = response.body()!!.nivelProgreso
                                                }
                                            } catch (e: Exception) {
                                                globalErrorMessage =
                                                    "Error de sincronización con el servidor."
                                            }
                                            // Volver al mapa tras la victoria
                                            navController.navigate("map") {
                                                popUpTo("map") { inclusive = true }
                                            }
                                        }
                                    },
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                            composable("pc") {
                                PCScreen(onBack = { navController.popBackStack() })
                            }
                            composable("gacha") {
                                GachaScreen(
                                    onBack = { navController.popBackStack() },
                                    onNavigateToPC = {
                                        navController.navigate("pc") {
                                            popUpTo("map") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                            composable("games") {
                                MinigamesScreen(navController = navController)
                            }
                            composable("safari_zone/{gameIndex}") { backStackEntry ->
                                val gameIndex =
                                    backStackEntry.arguments?.getString("gameIndex")?.toIntOrNull() ?: 0
                                com.diegofg11.pokequiz.ui.screens.SafariZonePager(
                                    initialPage = gameIndex,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("user") {
                                UserScreen(
                                    onLogout = {
                                        navController.navigate("welcome") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Overlay de Tutorial por pasos navegables
                    if (TutorialManager.isTutorialActive) {
                        val currentStep = TutorialManager.currentStep
                        val stepData = tutorialSteps.getOrNull(currentStep)
                        
                        if (stepData != null) {
                            val (targetRoute, content) = stepData
                            val (title, description) = content
                            
                            // Si estamos en la ruta correcta, mostramos el box
                            if (currentRoute == targetRoute) {
                                TutorialBox(
                                    title = title,
                                    description = description,
                                    buttonText = if (currentStep < tutorialSteps.size - 1) "SIGUIENTE" else "ENTENDIDO",
                                    onNext = {
                                        if (currentStep < tutorialSteps.size - 1) {
                                            val nextRoute = tutorialSteps[currentStep + 1].first
                                            TutorialManager.nextStep(context)
                                            navController.navigate(nextRoute)
                                        } else {
                                            TutorialManager.finishTutorial(context)
                                            navController.navigate("map") {
                                                popUpTo("map") { inclusive = true }
                                            }
                                        }
                                    }
                                )
                            } else if (currentRoute != "welcome") {
                                // Forzar navegación al inicio del tutorial si se pierde
                                LaunchedEffect(Unit) {
                                    navController.navigate(targetRoute)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RetroBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp) // Aumentado de 64.dp a 76.dp
            .background(Color.White)
            // Borde superior grueso estilo retro
            .drawBehind {
                drawLine(
                    color = Color(0xFF2D5A27),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = 6.dp.toPx()
                )
                drawLine(
                    color = Color.Black,
                    start = androidx.compose.ui.geometry.Offset(0f, 6.dp.toPx()),
                    end = androidx.compose.ui.geometry.Offset(size.width, 6.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }
    ) {
        Row(
            modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RetroNavItem(label = "MAPA", selected = currentRoute == "map", onClick = { onNavigate("map") })
            RetroNavItem(label = "PC", selected = currentRoute == "pc", onClick = { onNavigate("pc") })
            RetroNavItem(label = "BAZAR", selected = currentRoute == "gacha", onClick = { onNavigate("gacha") })
            RetroNavItem(label = "SAFARI", selected = currentRoute == "games" || currentRoute?.startsWith("safari_zone") == true, onClick = { onNavigate("games") })
            RetroNavItem(label = "PERFIL", selected = currentRoute == "user", onClick = { onNavigate("user") })
        }
    }
}

@Composable
fun RowScope.RetroNavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (selected) {
                // Cursor retro auténtico
                Text(
                    text = "▶",
                    fontSize = 12.sp,
                    color = Color.Black,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
            } else {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
