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
    private var initError: String? = null
    
    fun init(context: Context) {
        try {
            // Use the Downloads directory
            // For Android 10+, use the scoped storage approach which doesn't require permissions
            // For older versions, use the deprecated API which still works
            val downloadsDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: Use app-specific directory in Downloads
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "").also {
                    if (!it.exists()) {
                        val created = it.mkdirs()
                        Log.i(TAG, "Downloads directory created: $created at ${it.absolutePath}")
                    }
                }
            } else {
                // Android 9 and below: Use public Downloads directory
                @Suppress("DEPRECATION")
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).also {
                    if (!it.exists()) {
                        val created = it.mkdirs()
                        Log.i(TAG, "Downloads directory created: $created at ${it.absolutePath}")
                    }
                }
            }
            
            logFile = File(downloadsDir, LOG_FILE_NAME)
            
            // Test if we can write to the file
            try {
                FileWriter(logFile, true).use { writer ->
                    // Test write
                }
                isInitialized = true
                Log.i(TAG, "LogManager initialized successfully. Log file: ${logFile?.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Cannot write to log file at ${logFile?.absolutePath}", e)
                initError = "Cannot write to log file: ${e.message}"
                isInitialized = false
            }
            
            // Write initialization message
            if (isInitialized) {
                log("INFO", "=================================================")
                log("INFO", "LogManager initialized. Log file: ${logFile?.absolutePath}")
                log("INFO", "App started at ${getCurrentTimestamp()}")
                log("INFO", "Android version: ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})")
                log("INFO", "Device: ${Build.MANUFACTURER} ${Build.MODEL}")
                log("INFO", "App version: ${getAppVersion(context)}")
                log("INFO", "=================================================")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize LogManager", e)
            initError = "Failed to initialize: ${e.message}"
            isInitialized = false
        }
    }
    
    private fun getAppVersion(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    fun log(level: String, message: String, throwable: Throwable? = null) {
        val timestamp = getCurrentTimestamp()
        val logMessage = "[$timestamp] [$level] $message"
        
        // Always log to Android logcat
        when (level) {
            "ERROR" -> Log.e(TAG, message, throwable)
            "WARN" -> Log.w(TAG, message, throwable)
            "INFO" -> Log.i(TAG, message)
            "DEBUG" -> Log.d(TAG, message)
            else -> Log.v(TAG, message)
        }
        
        // Write to file if initialized
        if (isInitialized && logFile != null) {
            try {
                FileWriter(logFile, true).use { writer ->
                    PrintWriter(writer).use { printer ->
                        printer.println(logMessage)
                        throwable?.let {
                            printer.println("Exception: ${it.javaClass.name}: ${it.message}")
                            it.printStackTrace(printer)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write to log file", e)
                // Don't try to log this error to file to avoid infinite loop
            }
        } else {
            // Only log initialization errors once
            if (initError != null && level == "INFO" && message.contains("LogManager initialized")) {
                Log.w(TAG, "LogManager not fully initialized: $initError. Logs will only appear in Logcat.")
            }
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
            log("INFO", "Log file cleared and restarted")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear log file", e)
        }
    }
    
    fun getLogFilePath(): String? = logFile?.absolutePath
    
    fun getInitError(): String? = initError
    
    fun isFullyInitialized(): Boolean = isInitialized && initError == null
}
