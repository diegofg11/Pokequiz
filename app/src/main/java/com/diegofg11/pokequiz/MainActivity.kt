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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import com.diegofg11.pokequiz.ui.screens.BattleScreen
import com.diegofg11.pokequiz.ui.screens.GachaScreen
import com.diegofg11.pokequiz.ui.screens.MapScreen
import com.diegofg11.pokequiz.ui.screens.MinigamesScreen
import com.diegofg11.pokequiz.ui.screens.PCScreen
import com.diegofg11.pokequiz.ui.screens.WelcomeScreen
import com.diegofg11.pokequiz.ui.theme.*
import com.diegofg11.pokequiz.ui.components.PokeBallIcon
import com.diegofg11.pokequiz.ui.components.PokemonAlertDialog
import kotlinx.coroutines.launch
import androidx.navigation.compose.currentBackStackEntryAsState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.diegofg11.pokequiz.utils.SessionManager.init(this)
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

                // Refetch user data when currentUserId changes and is valid
                LaunchedEffect(com.diegofg11.pokequiz.utils.SessionManager.currentUserId) {
                    if (com.diegofg11.pokequiz.utils.SessionManager.currentUserId != -1) {
                        scope.launch {
                            try {
                                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    com.diegofg11.pokequiz.api.Network.api.getUser(com.diegofg11.pokequiz.utils.SessionManager.currentUserId)
                                }
                                if (response.isSuccessful && response.body() != null) {
                                    completedLevel = response.body()!!.nivelProgreso
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("MainActivity", "Error fetching user", e)
                                globalErrorMessage = "No se pudo conectar con el servidor para cargar tu perfil."
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

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val isFullScreen = currentRoute?.startsWith("battle") == true || currentRoute == "welcome"
                        if (!isFullScreen) {
                        NavigationBar(
                            containerColor = BackgroundStart,
                            contentColor = Color.White,
                            tonalElevation = 8.dp
                        ) {
                            // Aventura
                            NavigationBarItem(
                                selected = currentRoute == "map" || currentRoute?.startsWith("battle") == true,
                                onClick = { 
                                    navController.navigate("map") { 
                                        popUpTo("map") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                icon = { Icon(Icons.Default.Place, contentDescription = "Aventura") },
                                label = { Text("Aventura") },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = GoldPoke, selectedTextColor = GoldPoke, unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary, indicatorColor = Color.White.copy(alpha = 0.1f))
                            )
                            // PC/Usuario
                            NavigationBarItem(
                                selected = currentRoute == "pc",
                                onClick = { 
                                    navController.navigate("pc") {
                                        popUpTo("map") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { PokeBallIcon(modifier = Modifier.size(24.dp), outerColor = Color.Transparent) },
                                label = { Text("PC") },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = GoldPoke, selectedTextColor = GoldPoke, unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary, indicatorColor = Color.White.copy(alpha = 0.1f))
                            )
                            // Gacha
                            NavigationBarItem(
                                selected = currentRoute == "gacha",
                                onClick = { 
                                    navController.navigate("gacha") {
                                        popUpTo("map") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.Star, contentDescription = "Gacha") },
                                label = { Text("Gacha") },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = GoldPoke, selectedTextColor = GoldPoke, unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary, indicatorColor = Color.White.copy(alpha = 0.1f))
                            )
                            // Minijuegos
                            NavigationBarItem(
                                selected = currentRoute == "games",
                                onClick = { 
                                    navController.navigate("games") {
                                        popUpTo("map") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Minijuegos") },
                                label = { Text("Zona Safari") },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = GoldPoke, selectedTextColor = GoldPoke, unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary, indicatorColor = Color.White.copy(alpha = 0.1f))
                            )
                        }
                    }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (com.diegofg11.pokequiz.utils.SessionManager.currentUserId != -1) "map" else "welcome",
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
                            val levelId = backStackEntry.arguments?.getString("levelId")?.toIntOrNull() ?: 1
                            BattleScreen(
                                levelId = levelId,
                                onBattleWin = {
                                    scope.launch {
                                        try {
                                            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                com.diegofg11.pokequiz.api.Network.api.getUser(com.diegofg11.pokequiz.utils.SessionManager.currentUserId)
                                            }
                                            if (response.isSuccessful && response.body() != null) {
                                                completedLevel = response.body()!!.nivelProgreso
                                            }
                                        } catch(e: Exception) {
                                            globalErrorMessage = "Error de sincronización con el servidor."
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
                            PCScreen()
                        }
                        composable("gacha") {
                            GachaScreen(onNavigateToPC = {
                                navController.navigate("pc") {
                                    popUpTo("map") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            })
                        }
                        composable("games") {
                            MinigamesScreen(navController = navController)
                        }
                        composable("guess_pokemon") {
                            com.diegofg11.pokequiz.ui.screens.GuessPokemonScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("memory_game") {
                            com.diegofg11.pokequiz.ui.screens.MemoryGameScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("word_search") {
                            com.diegofg11.pokequiz.ui.screens.WordSearchScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

}
