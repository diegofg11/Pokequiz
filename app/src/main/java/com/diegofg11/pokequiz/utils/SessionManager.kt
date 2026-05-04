package com.diegofg11.pokequiz.utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREFS_NAME = "pokequiz_prefs"
    private const val KEY_USER_ID = "user_id"

    var currentUserId: Int = -1

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentUserId = prefs.getInt(KEY_USER_ID, -1)
    }

    fun saveUserId(context: Context, id: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_USER_ID, id).apply()
        currentUserId = id
    }
}
