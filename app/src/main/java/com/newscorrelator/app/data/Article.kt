package com.newscorrelator.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String?,
    val content: String?,
    val url: String,
    val imageUrl: String?,
    val publishedAt: Long,
    val source: String,
    val sourceId: String,
    val country: String?,
    val category: String?,
    val topicHash: String? = null, // For grouping similar articles
    val integrityScore: Float? = null, // 1-10 rating
    val integrityStatus: String? = null, // RED, YELLOW, GREEN
    val analyzed: Boolean = false,
    val saved: Boolean = false
)
