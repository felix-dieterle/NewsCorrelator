package com.newscorrelator.app.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources ORDER BY trustScore DESC")
    fun getAllSources(): LiveData<List<Source>>
    
    @Query("SELECT * FROM sources WHERE id = :id")
    suspend fun getSourceById(id: String): Source?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: Source)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<Source>)
    
    @Update
    suspend fun updateSource(source: Source)
}
