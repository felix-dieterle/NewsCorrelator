package com.newscorrelator.app.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ArticleGroupDao {
    @Query("SELECT * FROM article_groups ORDER BY createdAt DESC")
    fun getAllGroups(): LiveData<List<ArticleGroup>>
    
    @Query("SELECT * FROM article_groups WHERE topicHash = :topicHash")
    suspend fun getGroupByHash(topicHash: String): ArticleGroup?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: ArticleGroup)
    
    @Update
    suspend fun updateGroup(group: ArticleGroup)
}
