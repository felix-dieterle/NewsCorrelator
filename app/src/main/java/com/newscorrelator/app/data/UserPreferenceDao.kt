package com.newscorrelator.app.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserPreferenceDao {
    @Query("SELECT * FROM user_preferences LIMIT 1")
    fun getPreferences(): LiveData<UserPreference?>
    
    @Query("SELECT * FROM user_preferences LIMIT 1")
    suspend fun getPreferencesSync(): UserPreference?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: UserPreference): Long
    
    @Update
    suspend fun updatePreference(preference: UserPreference)
}
