package com.diegofg11.pokequiz.utils

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class ColorBlindMode {
    NONE, PROTANOPIA, DEUTERANOPIA, TRITANOPIA, MONOCHROMACY
}

object AccessibilityManager {
    private const val PREFS_NAME = "accessibility_prefs"
    private const val KEY_HIGH_CONTRAST = "high_contrast"
    private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
    private const val KEY_FONT_SCALE = "font_scale"
    private const val KEY_SCREEN_READER_VERBOSITY = "screen_reader_verbosity"
    private const val KEY_COLOR_BLIND_MODE = "color_blind_mode"

    var isHighContrastEnabled by mutableStateOf(false)
        private set
    var isHapticFeedbackEnabled by mutableStateOf(true)
        private set
    var fontScale by mutableStateOf(1.0f)
        private set
    var isScreenReaderOptimized by mutableStateOf(false)
        private set
    var colorBlindMode by mutableStateOf(ColorBlindMode.NONE)
        private set

    fun init(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isHighContrastEnabled = prefs.getBoolean(KEY_HIGH_CONTRAST, false)
        isHapticFeedbackEnabled = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)
        fontScale = prefs.getFloat(KEY_FONT_SCALE, 1.0f)
        isScreenReaderOptimized = prefs.getBoolean(KEY_SCREEN_READER_VERBOSITY, false)
        val modeStr = prefs.getString(KEY_COLOR_BLIND_MODE, ColorBlindMode.NONE.name) ?: ColorBlindMode.NONE.name
        colorBlindMode = try { ColorBlindMode.valueOf(modeStr) } catch (e: Exception) { ColorBlindMode.NONE }
    }

    fun setHighContrast(context: Context, enabled: Boolean) {
        isHighContrastEnabled = enabled
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
    }

    fun setHapticFeedback(context: Context, enabled: Boolean) {
        isHapticFeedbackEnabled = enabled
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
    }

    fun setFontScale(context: Context, scale: Float) {
        fontScale = scale
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putFloat(KEY_FONT_SCALE, scale).apply()
    }

    fun setScreenReaderOptimization(context: Context, enabled: Boolean) {
        isScreenReaderOptimized = enabled
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_SCREEN_READER_VERBOSITY, enabled).apply()
    }

    fun setColorBlindMode(context: Context, mode: ColorBlindMode) {
        colorBlindMode = mode
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_COLOR_BLIND_MODE, mode.name).apply()
    }

    fun applyColorBlindFilter(color: Color): Color {
        if (colorBlindMode == ColorBlindMode.NONE) return color
        
        val r = color.red
        val g = color.green
        val b = color.blue
        
        return when (colorBlindMode) {
            ColorBlindMode.PROTANOPIA -> {
                // Simulación/Ajuste para Protanopia (Falta de Rojo)
                val newR = 0.567f * r + 0.433f * g
                val newG = 0.558f * r + 0.442f * g
                val newB = 0f * r + 0.242f * g + 0.758f * b
                Color(newR.coerceIn(0f, 1f), newG.coerceIn(0f, 1f), newB.coerceIn(0f, 1f), color.alpha)
            }
            ColorBlindMode.DEUTERANOPIA -> {
                // Simulación/Ajuste para Deuteranopia (Falta de Verde)
                val newR = 0.625f * r + 0.375f * g
                val newG = 0.7f * r + 0.3f * g
                val newB = 0f * r + 0.3f * g + 0.7f * b
                Color(newR.coerceIn(0f, 1f), newG.coerceIn(0f, 1f), newB.coerceIn(0f, 1f), color.alpha)
            }
            ColorBlindMode.TRITANOPIA -> {
                // Simulación/Ajuste para Tritanopia (Falta de Azul)
                val newR = 0.95f * r + 0.05f * g
                val newG = 0f * r + 0.433f * g + 0.567f * b
                val newB = 0f * r + 0.475f * g + 0.525f * b
                Color(newR.coerceIn(0f, 1f), newG.coerceIn(0f, 1f), newB.coerceIn(0f, 1f), color.alpha)
            }
            ColorBlindMode.MONOCHROMACY -> {
                // Acromatopsia (Escala de grises)
                val gray = 0.299f * r + 0.587f * g + 0.114f * b
                Color(gray, gray, gray, color.alpha)
            }
            else -> color
        }
    }
}
