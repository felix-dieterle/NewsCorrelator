package com.newscorrelator.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.newscorrelator.app.utils.CacheManager
import com.newscorrelator.app.utils.LogManager

/**
 * Background worker to clean up expired cache entries
 * Runs periodically to free up memory and storage
 */
class CacheCleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            LogManager.i("CacheCleanupWorker: Starting cache cleanup")
            
            // Clean up expired entries
            CacheManager.cleanupExpiredEntries()
            
            // Get cache stats for logging
            val stats = CacheManager.getCacheStats()
            LogManager.i("CacheCleanupWorker: Cleanup complete. Cache stats: $stats")
            
            Result.success()
        } catch (e: Exception) {
            LogManager.e("CacheCleanupWorker: Error during cleanup: ${e.message}", e)
            Result.retry()
        }
    }
    
    companion object {
        const val WORK_NAME = "cache_cleanup_work"
    }
}
