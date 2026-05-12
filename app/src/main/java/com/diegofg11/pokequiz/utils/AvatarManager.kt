package com.diegofg11.pokequiz.utils

import android.content.Context
import com.diegofg11.pokequiz.R

data class Avatar(
    val id: String,
    val name: String,
    val resId: Int
)

object AvatarManager {
    private const val PREFS_NAME = "avatar_prefs"
    private const val KEY_SELECTED_AVATAR = "selected_avatar_id"

    val availableAvatars = listOf(
        Avatar("personaje_1", "PERSONAJE 1", R.drawable.personaje_1),
        Avatar("personaje_2", "PERSONAJE 2", R.drawable.personaje_2),
        Avatar("personaje_3", "PERSONAJE 3", R.drawable.personaje_3),
        Avatar("personaje_4", "PERSONAJE 4", R.drawable.personaje_4),
        Avatar("personaje_5", "PERSONAJE 5", R.drawable.personaje_5),
        Avatar("personaje_6", "PERSONAJE 6", R.drawable.personaje_6),
        Avatar("personaje_7", "PERSONAJE 7", R.drawable.personaje_7),
        Avatar("personaje_8", "PERSONAJE 8", R.drawable.personaje_8),
        Avatar("personaje_9", "PERSONAJE 9", R.drawable.personaje_9),
        Avatar("personaje_10", "PERSONAJE 10", R.drawable.personaje_10),
        Avatar("personaje_11", "PERSONAJE 11", R.drawable.personaje_11),
        Avatar("personaje_12", "PERSONAJE 12", R.drawable.personaje_12),
        Avatar("personaje_13", "PERSONAJE 13", R.drawable.personaje_13),
        Avatar("personaje_14", "PERSONAJE 14", R.drawable.personaje_14),
        Avatar("personaje_15", "PERSONAJE 15", R.drawable.personaje_15),
        Avatar("personaje_16", "PERSONAJE 16", R.drawable.personaje_16),
        Avatar("personaje_17", "PERSONAJE 17", R.drawable.personaje_17),
        Avatar("personaje_18", "PERSONAJE 18", R.drawable.personaje_18),
        Avatar("personaje_19", "PERSONAJE 19", R.drawable.personaje_19),
        Avatar("personaje_20", "PERSONAJE 20", R.drawable.personaje_20),
        Avatar("personaje_21", "PERSONAJE 21", R.drawable.personaje_21)
    )

    fun getSelectedAvatar(context: Context): Avatar {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_SELECTED_AVATAR, "personaje_1") ?: "personaje_1"
        return availableAvatars.find { it.id == id } ?: availableAvatars[0]
    }

    fun setSelectedAvatar(context: Context, avatarId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_AVATAR, avatarId).apply()
    }

    fun getAvatarModel(avatarUrl: String?): Any {
        if (avatarUrl.isNullOrEmpty()) return availableAvatars[0].resId
        if (avatarUrl.startsWith("http")) return avatarUrl
        return availableAvatars.find { it.id == avatarUrl }?.resId ?: availableAvatars[0].resId
    }
}
