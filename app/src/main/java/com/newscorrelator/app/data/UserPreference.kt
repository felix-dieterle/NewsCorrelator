package com.newscorrelator.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreference(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val keywords: String = "", // Comma-separated
    val categories: String = "general,technology,business", // Comma-separated
    val preferredSources: String = "", // Comma-separated source IDs
    val sourcesPerTopic: Int = 4, // Number of diverse sources to fetch per topic
    val enableAiAnalysis: Boolean = true,
    val openRouterApiKey: String = "", // User's OpenRouter API key
    val newsApiKey: String = "", // User's News API key
    val lastUpdated: Long = System.currentTimeMillis()
)
