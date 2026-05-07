package com.diegofg11.pokequiz.utils

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

object SoundManager {
    private const val TAG = "SoundManager"
    private var mediaPlayer: MediaPlayer? = null
    private var currentResId: Int = -1
    private var volume: Float = 1.0f

    fun playMusic(context: Context, resId: Int, volumeFactor: Float = 1.0f) {
        val appContext = context.applicationContext
        
        volume = appContext.getSharedPreferences("sound_prefs", Context.MODE_PRIVATE)
            .getFloat("volume", 1.0f) * volumeFactor

        if (currentResId == resId) {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.setVolume(volume, volume)
                    return
                }
            }
        }

        stopMusic()

        try {
            mediaPlayer = MediaPlayer().apply {
                val afd = appContext.resources.openRawResourceFd(resId)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                setVolume(volume, volume)
                isLooping = true
                prepare()
                start()
            }
            currentResId = resId
        } catch (e: Exception) {
            currentResId = -1
        }
    }

    fun setVolume(context: Context, newVolume: Float) {
        volume = newVolume.coerceIn(0f, 1f)
        context.applicationContext.getSharedPreferences("sound_prefs", Context.MODE_PRIVATE)
            .edit().putFloat("volume", volume).apply()
        
        try {
            mediaPlayer?.setVolume(volume, volume)
        } catch (e: Exception) {
            Log.e(TAG, "Error al cambiar volumen: ${e.message}")
        }
    }

    fun getVolume(context: Context): Float {
        return context.applicationContext.getSharedPreferences("sound_prefs", Context.MODE_PRIVATE)
            .getFloat("volume", 1.0f)
    }

    fun pauseMusic() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.pause()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al pausar: ${e.message}")
        }
    }

    fun resumeMusic() {
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.setVolume(volume, volume)
                    it.start()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al reanudar: ${e.message}")
        }
    }

    fun stopMusic() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al detener msica: ${e.message}")
        } finally {
            mediaPlayer = null
            currentResId = -1
        }
    }
}
