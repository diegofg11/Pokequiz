package com.diegofg11.pokequiz.utils

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class ColorBlindMode(val displayName: String) {
    NONE("NINGUNO"), 
    PROTANOPIA("PROTANOPIA"), 
    DEUTERANOPIA("DEUTERANOPIA"), 
    TRITANOPIA("TRITANOPIA"), 
    MONOCHROMACY("ACROMATOPSIA")
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

    fun getColorMatrix(): androidx.compose.ui.graphics.ColorMatrix {
        val matrix = androidx.compose.ui.graphics.ColorMatrix()
        if (colorBlindMode == ColorBlindMode.NONE) return matrix

        val values = when (colorBlindMode) {
            ColorBlindMode.PROTANOPIA -> floatArrayOf(
                0.56667f, 0.43333f, 0f, 0f, 0f,
                0.55833f, 0.44167f, 0f, 0f, 0f,
                0f, 0.24167f, 0.75833f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            ColorBlindMode.DEUTERANOPIA -> floatArrayOf(
                0.625f, 0.375f, 0f, 0f, 0f,
                0.7f, 0.3f, 0f, 0f, 0f,
                0f, 0.3f, 0.7f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            ColorBlindMode.TRITANOPIA -> floatArrayOf(
                0.95f, 0.05f, 0f, 0f, 0f,
                0f, 0.43333f, 0.56667f, 0f, 0f,
                0f, 0.475f, 0.525f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            ColorBlindMode.MONOCHROMACY -> floatArrayOf(
                0.299f, 0.587f, 0.114f, 0f, 0f,
                0.299f, 0.587f, 0.114f, 0f, 0f,
                0.299f, 0.587f, 0.114f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            else -> return matrix
        }
        
        // Asignar manualmente los valores ya que setValues o similar no est disponible directamente igual
        // En Compose ColorMatrix se usa con un floatArray de 20 elementos
        return androidx.compose.ui.graphics.ColorMatrix(values)
    }

    fun applyColorBlindFilter(color: Color): Color {
        // Mantenemos esto por compatibilidad si algn componente lo usa fuera de RetroBackground
        // Pero lo ideal es usar el ColorMatrix global
        if (colorBlindMode == ColorBlindMode.NONE) return color
        
        val r = color.red
        val g = color.green
        val b = color.blue
        
        return when (colorBlindMode) {
            ColorBlindMode.PROTANOPIA -> {
                val newR = 0.567f * r + 0.433f * g
                val newG = 0.558f * r + 0.442f * g
                val newB = 0.242f * g + 0.758f * b
                Color(newR.coerceIn(0f, 1f), newG.coerceIn(0f, 1f), newB.coerceIn(0f, 1f), color.alpha)
            }
            ColorBlindMode.DEUTERANOPIA -> {
                val newR = 0.625f * r + 0.375f * g
                val newG = 0.7f * r + 0.3f * g
                val newB = 0.3f * g + 0.7f * b
                Color(newR.coerceIn(0f, 1f), newG.coerceIn(0f, 1f), newB.coerceIn(0f, 1f), color.alpha)
            }
            ColorBlindMode.TRITANOPIA -> {
                val newR = 0.95f * r + 0.05f * g
                val newG = 0.433f * g + 0.567f * b
                val newB = 0.475f * g + 0.525f * b
                Color(newR.coerceIn(0f, 1f), newG.coerceIn(0f, 1f), newB.coerceIn(0f, 1f), color.alpha)
            }
            ColorBlindMode.MONOCHROMACY -> {
                val gray = 0.299f * r + 0.587f * g + 0.114f * b
                Color(gray, gray, gray, color.alpha)
            }
            else -> color
        }
    }
}
