package com.newscorrelator.app

import android.app.Application
import android.os.Build
import android.widget.Toast
import com.newscorrelator.app.utils.LogManager

class NewsCorrelatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize logging first
        try {
            LogManager.init(this)
            
            // Check if logging was successful
            if (LogManager.isFullyInitialized()) {
                LogManager.i("Application onCreate() started")
                LogManager.i("Log file successfully created at: ${LogManager.getLogFilePath()}")
            } else {
                android.util.Log.e("NewsCorrelatorApp", "LogManager initialization failed: ${LogManager.getInitError()}")
                LogManager.i("Application onCreate() started (file logging may not work)")
            }
            
            LogManager.i("Android SDK version: ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})")
            LogManager.i("Build manufacturer: ${Build.MANUFACTURER}")
            LogManager.i("Build model: ${Build.MODEL}")
            LogManager.i("Build brand: ${Build.BRAND}")
            
            // For Android 10+, scoped storage allows writing to Downloads without special permissions
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                LogManager.w("Running on Android ${Build.VERSION.SDK_INT}. Storage permissions may be required.")
            }
        } catch (e: Exception) {
            android.util.Log.e("NewsCorrelatorApp", "Failed to initialize LogManager", e)
        }
        
        // Set up uncaught exception handler
        val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                LogManager.e("=================================================")
                LogManager.e("UNCAUGHT EXCEPTION in thread ${thread.name}", throwable)
                LogManager.e("Exception class: ${throwable.javaClass.name}")
                LogManager.e("Exception message: ${throwable.message}")
                LogManager.e("Stack trace follows:")
                LogManager.e("App is about to crash!")
                LogManager.e("Log file location: ${LogManager.getLogFilePath()}")
                LogManager.e("=================================================")
            } catch (e: Exception) {
                android.util.Log.e("NewsCorrelatorApp", "Failed to log uncaught exception", e)
            } finally {
                // Always call the default handler to maintain normal crash behavior
                defaultExceptionHandler?.uncaughtException(thread, throwable)
            }
        }
        
        try {
            LogManager.i("Uncaught exception handler registered")
            LogManager.i("Application onCreate() completed successfully")
        } catch (e: Exception) {
            LogManager.e("Error during Application onCreate()", e)
        }
    }
}
