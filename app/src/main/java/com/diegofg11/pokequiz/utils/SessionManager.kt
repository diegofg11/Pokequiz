package com.diegofg11.pokequiz.utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREFS_NAME = "pokequiz_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_TOKEN = "auth_token"

    private const val KEY_FIRST_TIME = "is_first_time"
    var currentUserId: Int = -1
    var currentToken: String? = null

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentUserId = prefs.getInt(KEY_USER_ID, -1)
        currentToken = prefs.getString(KEY_TOKEN, null)
    }

    fun isFirstTime(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FIRST_TIME, false)
    }

    fun setFirstTime(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_FIRST_TIME, value).apply()
    }

    fun saveUserId(context: Context, id: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_USER_ID, id).apply()
        currentUserId = id
    }

    fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TOKEN, token).apply()
        currentToken = token
    }

    fun logout(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_USER_ID).remove(KEY_TOKEN).apply()
        currentUserId = -1
        currentToken = null
    }
}
