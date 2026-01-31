package com.newscorrelator.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.newscorrelator.app.utils.LogManager

@Database(
    entities = [Article::class, Source::class, UserPreference::class, ArticleGroup::class],
    version = 1,
    exportSchema = false
)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun sourceDao(): SourceDao
    abstract fun userPreferenceDao(): UserPreferenceDao
    abstract fun articleGroupDao(): ArticleGroupDao

    companion object {
        @Volatile
        private var INSTANCE: NewsDatabase? = null

        fun getDatabase(context: Context): NewsDatabase {
            LogManager.i("NewsDatabase.getDatabase() called")
            return INSTANCE ?: synchronized(this) {
                val existing = INSTANCE
                if (existing != null) {
                    LogManager.i("Returning existing database instance")
                    existing
                } else {
                    LogManager.i("Creating new database instance")
                    try {
                        val instance = Room.databaseBuilder(
                            context.applicationContext,
                            NewsDatabase::class.java,
                            "news_database"
                        ).build()
                        INSTANCE = instance
                        LogManager.i("Database instance created successfully")
                        instance
                    } catch (e: Exception) {
                        LogManager.e("Failed to create database instance", e)
                        throw e
                    }
                }
            }
        }
    }
}
