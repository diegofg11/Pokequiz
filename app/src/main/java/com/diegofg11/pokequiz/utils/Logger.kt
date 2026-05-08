package com.diegofg11.pokequiz.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gestor de Logs para Pokequiz.
 * Guarda registros en archivos locales y gestiona la limpieza automática.
 */
object Logger {
    private const val TAG = "PokeLogger"
    private const val LOG_DIR_NAME = "app_logs"
    private const val MAX_LOG_AGE_DAYS = 7 // Tiempo adecuado para conservar logs en móvil
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Registra un mensaje de información.
     */
    fun i(context: Context, tag: String, message: String) {
        log(context, "INFO", tag, message)
    }

    /**
     * Registra un mensaje de error.
     */
    fun e(context: Context, tag: String, message: String, throwable: Throwable? = null) {
        val fullMessage = if (throwable != null) {
            "$message\n${Log.getStackTraceString(throwable)}"
        } else {
            message
        }
        log(context, "ERROR", tag, fullMessage)
    }

    /**
     * Registra un mensaje de depuración.
     */
    fun d(context: Context, tag: String, message: String) {
        log(context, "DEBUG", tag, message)
    }

    private fun log(context: Context, level: String, tag: String, message: String) {
        // Log en Logcat para desarrollo
        Log.println(when(level) {
            "ERROR" -> Log.ERROR
            "DEBUG" -> Log.DEBUG
            else -> Log.INFO
        }, tag, message)

        // Guardar en archivo
        try {
            val logDir = File(context.filesDir, LOG_DIR_NAME)
            if (!logDir.exists()) {
                logDir.mkdirs()
            }

            val fileName = "log_${fileDateFormat.format(Date())}.txt"
            val logFile = File(logDir, fileName)

            val timestamp = dateFormat.format(Date())
            val logEntry = "[$timestamp] [$level] [$tag]: $message\n"

            FileWriter(logFile, true).use { it.write(logEntry) }
            
            // Limpieza ocasional (por ejemplo, 1 de cada 10 logs para no impactar rendimiento)
            if (Random().nextInt(10) == 0) {
                cleanOldLogs(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al escribir log en archivo", e)
        }
    }

    /**
     * Elimina los archivos de log más antiguos que MAX_LOG_AGE_DAYS.
     */
    fun cleanOldLogs(context: Context) {
        try {
            val logDir = File(context.filesDir, LOG_DIR_NAME)
            if (!logDir.exists()) return

            val files = logDir.listFiles() ?: return
            val expirationTime = System.currentTimeMillis() - (MAX_LOG_AGE_DAYS * 24 * 60 * 60 * 1000L)

            files.forEach { file ->
                if (file.lastModified() < expirationTime) {
                    file.delete()
                    Log.d(TAG, "Log antiguo eliminado: ${file.name}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la limpieza de logs", e)
        }
    }
}
