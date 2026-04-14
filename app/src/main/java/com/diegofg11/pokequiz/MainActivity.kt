package com.diegofg11.pokequiz

import android.content.Intent
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
import com.diegofg11.pokequiz.ui.screens.MapScreen
import com.diegofg11.pokequiz.ui.theme.*
import com.diegofg11.pokequiz.ui.components.PokeBallIcon

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokequizTheme {
                val navController = rememberNavController()
                var completedLevel by remember { mutableIntStateOf(0) }
                var selectedItem by remember { mutableIntStateOf(0) }
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = BackgroundStart,
                            contentColor = Color.White,
                            tonalElevation = 8.dp
                        ) {
                            // Aventura
                            NavigationBarItem(
                                selected = selectedItem == 0,
                                onClick = { 
                                    selectedItem = 0
                                    navController.navigate("map") { popUpTo("map") { inclusive = true } }
                                },
                                icon = { Icon(Icons.Default.Place, contentDescription = "Aventura") },
                                label = { Text("Aventura") },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = GoldPoke, selectedTextColor = GoldPoke, unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary, indicatorColor = Color.White.copy(alpha = 0.1f))
                            )
                            // PC/Usuario
                            NavigationBarItem(
                                selected = selectedItem == 1,
                                onClick = { 
                                    selectedItem = 1
                                    startActivity(Intent(this@MainActivity, PokemonPCActivity::class.java))
                                },
                                icon = { PokeBallIcon(modifier = Modifier.size(24.dp), outerColor = Color.Transparent) },
                                label = { Text("PC") },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = GoldPoke, selectedTextColor = GoldPoke, unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary, indicatorColor = Color.White.copy(alpha = 0.1f))
                            )
                            // Gacha
                            NavigationBarItem(
                                selected = selectedItem == 2,
                                onClick = { 
                                    selectedItem = 2
                                    startActivity(Intent(this@MainActivity, GachaActivity::class.java))
                                },
                                icon = { Icon(Icons.Default.Star, contentDescription = "Gacha") },
                                label = { Text("Gacha") },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = GoldPoke, selectedTextColor = GoldPoke, unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary, indicatorColor = Color.White.copy(alpha = 0.1f))
                            )
                            // Minijuegos
                            NavigationBarItem(
                                selected = selectedItem == 3,
                                onClick = { selectedItem = 3 },
                                icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Minijuegos") },
                                label = { Text("Juegos") },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = GoldPoke, selectedTextColor = GoldPoke, unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary, indicatorColor = Color.White.copy(alpha = 0.1f))
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "map",
                        modifier = Modifier.padding(innerPadding)
                    ) {
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
                                    if (levelId > completedLevel) {
                                        completedLevel = levelId
                                    }
                                    navController.popBackStack()
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Helper to start old activities if needed (could be called from Compose)
    fun openPokemonPC() {
        startActivity(Intent(this, PokemonPCActivity::class.java))
    }

    fun openGacha() {
        startActivity(Intent(this, GachaActivity::class.java))
    }
}
