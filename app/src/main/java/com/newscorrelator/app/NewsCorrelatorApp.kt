package com.newscorrelator.app

import android.app.Application
import com.newscorrelator.app.utils.LogManager
import kotlin.system.exitProcess

class NewsCorrelatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize logging first
        try {
            LogManager.init(this)
            LogManager.i("Application onCreate() started")
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
