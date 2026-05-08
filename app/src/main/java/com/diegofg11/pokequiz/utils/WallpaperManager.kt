/**
 * @authors: Gaizka, Diego y Xiker
 * Gestor de fondos de pantalla del mapa. Persiste la selección del usuario.
 */
package com.diegofg11.pokequiz.utils

import android.content.Context
import com.diegofg11.pokequiz.R

object WallpaperManager {
    private const val PREFS_NAME = "pokequiz_prefs"
    private const val KEY_WALLPAPER = "map_wallpaper"

    private val wallpapers = listOf(
        R.drawable.fondo_seleccion_niveles1,
        R.drawable.fondo_seleccion_niveles2,
        R.drawable.fondo_seleccion_niveles3,
        R.drawable.fondo_seleccion_niveles4,
        R.drawable.fondo_seleccion_niveles5,
        R.drawable.fondo_seleccion_niveles6,
        R.drawable.fondo_seleccion_niveles7,
        R.drawable.fondo_seleccion_niveles8,
        R.drawable.fondo_seleccion_niveles9
    )

    fun getSelectedWallpaperRes(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val index = prefs.getInt(KEY_WALLPAPER, 0) // Default to first wallpaper
        return wallpapers.getOrElse(index) { wallpapers[0] }
    }

    fun setSelectedWallpaper(context: Context, index: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_WALLPAPER, index).apply()
    }

    fun getAllWallpapers(): List<Int> = wallpapers
}
