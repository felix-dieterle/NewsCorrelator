package com.newscorrelator.app.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun getAllArticles(): LiveData<List<Article>>
    
    @Query("SELECT * FROM articles WHERE topicHash = :topicHash ORDER BY publishedAt DESC")
    fun getArticlesByTopic(topicHash: String): LiveData<List<Article>>
    
    @Query("SELECT * FROM articles WHERE category IN (:categories) ORDER BY publishedAt DESC LIMIT :limit")
    fun getArticlesByCategories(categories: List<String>, limit: Int = 100): LiveData<List<Article>>
    
    @Query("SELECT * FROM articles WHERE saved = 1 ORDER BY publishedAt DESC")
    fun getSavedArticles(): LiveData<List<Article>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<Article>)
    
    @Update
    suspend fun updateArticle(article: Article)
    
    @Query("DELETE FROM articles WHERE publishedAt < :timestamp")
    suspend fun deleteOldArticles(timestamp: Long)
}
