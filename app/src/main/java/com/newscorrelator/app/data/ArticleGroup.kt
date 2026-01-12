package com.newscorrelator.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "article_groups")
data class ArticleGroup(
    @PrimaryKey
    val topicHash: String,
    val topicTitle: String,
    val articleCount: Int = 0,
    val avgIntegrityScore: Float? = null,
    val createdAt: Long = System.currentTimeMillis()
)
