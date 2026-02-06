package com.newscorrelator.app.utils

import com.google.gson.Gson
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages caching of API responses to reduce redundant requests
 * 
 * Features:
 * - TTL-based cache expiration
 * - Separate caches for different data types
 * - Thread-safe operations
 * - Different TTLs for AI vs non-AI mode
 */
object CacheManager {
    
    // Internal visibility required for inline functions
    internal val gson = Gson()
    
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long,
        val ttl: Long
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > ttl
    }
    
    // Cache stores (internal visibility required for inline functions)
    internal val newsCache = ConcurrentHashMap<String, CacheEntry<String>>()
    internal val aiAnalysisCache = ConcurrentHashMap<String, CacheEntry<String>>()
    
    // Default TTL values (in milliseconds)
    private const val NEWS_CACHE_TTL_AI_MODE = 1_800_000L // 30 minutes in AI mode
    private const val NEWS_CACHE_TTL_NORMAL = 3_600_000L // 1 hour in normal mode
    private const val AI_ANALYSIS_CACHE_TTL = 86_400_000L // 24 hours - analyses don't change
    
    /**
     * Generate cache key from request parameters
     */
    private fun generateCacheKey(vararg params: Any?): String {
        return params.joinToString("|") { it?.toString() ?: "null" }
    }
    
    /**
     * Cache news response
     */
    fun <T> cacheNewsResponse(
        key: String,
        data: T,
        isAiMode: Boolean = false,
        customTtl: Long? = null
    ) {
        val ttl = customTtl ?: if (isAiMode) NEWS_CACHE_TTL_AI_MODE else NEWS_CACHE_TTL_NORMAL
        val json = gson.toJson(data)
        newsCache[key] = CacheEntry(json, System.currentTimeMillis(), ttl)
        LogManager.i("Cached news response for key: $key (TTL: ${ttl / 1000}s)")
    }
    
    /**
     * Get cached news response
     */
    inline fun <reified T> getCachedNewsResponse(key: String): T? {
        val entry = newsCache[key]
        
        if (entry == null) {
            LogManager.i("Cache miss for key: $key")
            return null
        }
        
        if (entry.isExpired()) {
            newsCache.remove(key)
            LogManager.i("Cache expired for key: $key")
            return null
        }
        
        LogManager.i("Cache hit for key: $key")
        return try {
            gson.fromJson(entry.data, T::class.java)
        } catch (e: Exception) {
            LogManager.e("Error deserializing cached data: ${e.message}", e)
            newsCache.remove(key)
            null
        }
    }
    
    /**
     * Cache AI analysis result
     */
    fun <T> cacheAiAnalysis(articleUrl: String, data: T, customTtl: Long? = null) {
        val ttl = customTtl ?: AI_ANALYSIS_CACHE_TTL
        val json = gson.toJson(data)
        aiAnalysisCache[articleUrl] = CacheEntry(json, System.currentTimeMillis(), ttl)
        LogManager.i("Cached AI analysis for article: $articleUrl (TTL: ${ttl / 1000}s)")
    }
    
    /**
     * Get cached AI analysis
     */
    inline fun <reified T> getCachedAiAnalysis(articleUrl: String): T? {
        val entry = aiAnalysisCache[articleUrl]
        
        if (entry == null) {
            LogManager.i("No cached analysis for article: $articleUrl")
            return null
        }
        
        if (entry.isExpired()) {
            aiAnalysisCache.remove(articleUrl)
            LogManager.i("Cached analysis expired for article: $articleUrl")
            return null
        }
        
        LogManager.i("Using cached analysis for article: $articleUrl")
        return try {
            gson.fromJson(entry.data, T::class.java)
        } catch (e: Exception) {
            LogManager.e("Error deserializing cached analysis: ${e.message}", e)
            aiAnalysisCache.remove(articleUrl)
            null
        }
    }
    
    /**
     * Generate cache key for news query
     */
    fun generateNewsQueryKey(
        category: String?,
        country: String?,
        query: String?,
        pageSize: Int
    ): String {
        return generateCacheKey("news", category, country, query, pageSize)
    }
    
    /**
     * Generate cache key for search query
     */
    fun generateSearchQueryKey(
        query: String,
        language: String?,
        sortBy: String,
        pageSize: Int
    ): String {
        return generateCacheKey("search", query, language, sortBy, pageSize)
    }
    
    /**
     * Clear expired entries from all caches
     */
    fun cleanupExpiredEntries() {
        var cleaned = 0
        
        // Clean news cache
        val expiredNewsKeys = newsCache.filter { it.value.isExpired() }.keys
        expiredNewsKeys.forEach { 
            newsCache.remove(it)
            cleaned++
        }
        
        // Clean AI analysis cache
        val expiredAnalysisKeys = aiAnalysisCache.filter { it.value.isExpired() }.keys
        expiredAnalysisKeys.forEach { 
            aiAnalysisCache.remove(it)
            cleaned++
        }
        
        if (cleaned > 0) {
            LogManager.i("Cleaned up $cleaned expired cache entries")
        }
    }
    
    /**
     * Clear all cached data
     */
    fun clearAll() {
        val totalSize = newsCache.size + aiAnalysisCache.size
        newsCache.clear()
        aiAnalysisCache.clear()
        LogManager.i("Cleared all caches ($totalSize entries)")
    }
    
    /**
     * Clear only news cache
     */
    fun clearNewsCache() {
        val size = newsCache.size
        newsCache.clear()
        LogManager.i("Cleared news cache ($size entries)")
    }
    
    /**
     * Clear only AI analysis cache
     */
    fun clearAiAnalysisCache() {
        val size = aiAnalysisCache.size
        aiAnalysisCache.clear()
        LogManager.i("Cleared AI analysis cache ($size entries)")
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): Map<String, Int> {
        cleanupExpiredEntries()
        return mapOf(
            "news_cache_size" to newsCache.size,
            "ai_analysis_cache_size" to aiAnalysisCache.size,
            "total_cache_size" to (newsCache.size + aiAnalysisCache.size)
        )
    }
}
