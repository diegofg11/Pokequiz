package com.diegofg11.pokequiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.diegofg11.pokequiz.ui.screens.GachaScreen

// GachaActivity es un stub legacy - la navegación principal usa GachaScreen vía MainActivity NavHost
class GachaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GachaScreen(onNavigateToPC = { finish() })
        }
    }
}
