package com.newscorrelator.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sources")
data class Source(
    @PrimaryKey
    val id: String,
    val name: String,
    val country: String?,
    val category: String?,
    val trustScore: Float = 5.0f, // Learned over time, 1-10 scale
    val articlesAnalyzed: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
