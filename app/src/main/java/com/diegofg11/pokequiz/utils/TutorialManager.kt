/**
 * @authors: Gaizka, Diego y Xiker
 * Gestor del tutorial de primera partida. Controla los pasos y la persistencia del estado.
 */
package com.diegofg11.pokequiz.utils

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object TutorialManager {
    private const val PREFS_NAME = "tutorial_prefs"
    private const val KEY_TUTORIAL_ACTIVE = "tutorial_active"
    private const val KEY_CURRENT_STEP = "tutorial_step"

    var isTutorialActive by mutableStateOf(false)
    var currentStep by mutableStateOf(0)

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isTutorialActive = SessionManager.isFirstTime(context)
        currentStep = prefs.getInt(KEY_CURRENT_STEP, 0)
    }

    fun startTutorial(context: Context) {
        isTutorialActive = true
        currentStep = 0
        saveState(context)
    }

    fun nextStep(context: Context) {
        currentStep++
        saveState(context)
    }

    fun finishTutorial(context: Context) {
        isTutorialActive = false
        currentStep = 0
        SessionManager.setFirstTime(context, false)
        saveState(context)
    }

    private fun saveState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_TUTORIAL_ACTIVE, isTutorialActive)
            .putInt(KEY_CURRENT_STEP, currentStep)
            .apply()
    }
}
