package com.newscorrelator.app

import android.app.Application
import android.os.Build
import com.newscorrelator.app.utils.LogManager

class NewsCorrelatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize logging first
        try {
            LogManager.init(this)
            LogManager.i("Application onCreate() started")
            LogManager.i("Android SDK version: ${Build.VERSION.SDK_INT}")
            
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
                LogManager.e("UNCAUGHT EXCEPTION in thread ${thread.name}", throwable)
                LogManager.e("App is about to crash!")
            } catch (e: Exception) {
                android.util.Log.e("NewsCorrelatorApp", "Failed to log uncaught exception", e)
            } finally {
                defaultExceptionHandler?.uncaughtException(thread, throwable)
            }
        }
        
        try {
            LogManager.i("Application onCreate() completed successfully")
        } catch (e: Exception) {
            LogManager.e("Error during Application onCreate()", e)
        }
    }
}
