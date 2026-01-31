package com.newscorrelator.app.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

object LogManager {
    private const val TAG = "NewsCorrelator"
    private const val LOG_FILE_NAME = "newscorrelator_debug.txt"
    
    private var logFile: File? = null
    private var isInitialized = false
    
    fun init(context: Context) {
        try {
            // Use the Downloads directory
            // For Android 10+, use the scoped storage approach which doesn't require permissions
            // For older versions, use the deprecated API which still works
            val downloadsDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: Use app-specific directory in Downloads
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "").also {
                    if (!it.exists()) {
                        it.mkdirs()
                    }
                }
            } else {
                // Android 9 and below: Use public Downloads directory
                @Suppress("DEPRECATION")
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).also {
                    if (!it.exists()) {
                        it.mkdirs()
                    }
                }
            }
            
            logFile = File(downloadsDir, LOG_FILE_NAME)
            isInitialized = true
            
            // Write initialization message
            log("INFO", "LogManager initialized. Log file: ${logFile?.absolutePath}")
            log("INFO", "App started at ${getCurrentTimestamp()}")
            log("INFO", "Android version: ${Build.VERSION.SDK_INT}")
            log("INFO", "Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize LogManager", e)
        }
    }
    
    fun log(level: String, message: String, throwable: Throwable? = null) {
        val timestamp = getCurrentTimestamp()
        val logMessage = "[$timestamp] [$level] $message"
        
        // Also log to Android logcat
        when (level) {
            "ERROR" -> Log.e(TAG, message, throwable)
            "WARN" -> Log.w(TAG, message, throwable)
            "INFO" -> Log.i(TAG, message)
            "DEBUG" -> Log.d(TAG, message)
            else -> Log.v(TAG, message)
        }
        
        // Write to file
        try {
            if (isInitialized && logFile != null) {
                FileWriter(logFile, true).use { writer ->
                    PrintWriter(writer).use { printer ->
                        printer.println(logMessage)
                        throwable?.let {
                            printer.println("Exception: ${it.javaClass.name}: ${it.message}")
                            it.printStackTrace(printer)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write to log file", e)
        }
    }
    
    fun i(message: String) = log("INFO", message)
    fun e(message: String, throwable: Throwable? = null) = log("ERROR", message, throwable)
    fun w(message: String, throwable: Throwable? = null) = log("WARN", message, throwable)
    fun d(message: String) = log("DEBUG", message)
    
    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    fun clearLog() {
        try {
            logFile?.delete()
            log("INFO", "Log file cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear log file", e)
        }
    }
    
    fun getLogFilePath(): String? = logFile?.absolutePath
}
