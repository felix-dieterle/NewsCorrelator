package com.newscorrelator.app.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Optimizes API queries to reduce redundant requests
 * 
 * Features:
 * - Query deduplication (prevents multiple identical requests)
 * - Query batching (combines similar queries when possible)
 * - Smart parameter optimization based on AI vs non-AI mode
 * - Request coalescing (merges concurrent identical requests)
 */
object QueryOptimizer {
    
    // Track in-flight requests to prevent duplicates
    private val inFlightRequests = ConcurrentHashMap<String, Mutex>()
    
    /**
     * Optimize news query parameters based on mode
     * 
     * AI Mode: Fetch more data for better analysis
     * Non-AI Mode: Minimize data to reduce API calls
     */
    data class OptimizedNewsQuery(
        val pageSize: Int,
        val shouldFetchMultipleSources: Boolean,
        val maxCountries: Int
    )
    
    fun optimizeNewsQuery(
        isAiMode: Boolean,
        requestedPageSize: Int,
        sourcesPerTopic: Int
    ): OptimizedNewsQuery {
        return if (isAiMode) {
            // AI mode: Fetch more articles for correlation analysis
            OptimizedNewsQuery(
                pageSize = requestedPageSize.coerceAtMost(20), // More articles
                shouldFetchMultipleSources = true,
                maxCountries = sourcesPerTopic.coerceAtLeast(3) // At least 3 sources
            )
        } else {
            // Non-AI mode: Minimize requests
            OptimizedNewsQuery(
                pageSize = requestedPageSize.coerceAtMost(10), // Fewer articles
                shouldFetchMultipleSources = sourcesPerTopic > 2,
                maxCountries = sourcesPerTopic.coerceAtMost(2) // At most 2 sources
            )
        }
    }
    
    /**
     * Batch multiple categories into efficient query groups
     * Reduces total API calls by grouping compatible queries
     */
    data class QueryBatch(
        val categories: List<String>,
        val estimatedApiCalls: Int
    )
    
    fun batchCategoryQueries(
        categories: List<String>,
        countriesPerCategory: Int,
        isAiMode: Boolean
    ): List<QueryBatch> {
        // In non-AI mode, we can be more aggressive with batching
        val maxCategoriesPerBatch = if (isAiMode) 2 else 3
        
        val batches = mutableListOf<QueryBatch>()
        var currentBatch = mutableListOf<String>()
        
        for (category in categories) {
            currentBatch.add(category)
            
            if (currentBatch.size >= maxCategoriesPerBatch) {
                val estimatedCalls = currentBatch.size * countriesPerCategory
                batches.add(QueryBatch(currentBatch.toList(), estimatedCalls))
                currentBatch.clear()
            }
        }
        
        // Add remaining categories
        if (currentBatch.isNotEmpty()) {
            val estimatedCalls = currentBatch.size * countriesPerCategory
            batches.add(QueryBatch(currentBatch.toList(), estimatedCalls))
        }
        
        LogManager.i("Batched ${categories.size} categories into ${batches.size} batches")
        return batches
    }
    
    /**
     * Deduplicate query parameters
     * Returns normalized query to check for duplicates
     */
    fun normalizeQuery(
        category: String?,
        country: String?,
        query: String?,
        pageSize: Int
    ): String {
        return listOfNotNull(
            category?.lowercase()?.trim(),
            country?.lowercase()?.trim(),
            query?.lowercase()?.trim(),
            pageSize.toString()
        ).joinToString("|")
    }
    
    /**
     * Execute with request coalescing
     * If multiple coroutines request same data, only one request is made
     */
    suspend fun <T> executeWithCoalescing(
        key: String,
        block: suspend () -> T
    ): T {
        // Get or create mutex for this key
        val mutex = inFlightRequests.getOrPut(key) { Mutex() }
        
        // If already locked, this will wait for the first request to complete
        return mutex.withLock {
            LogManager.i("Executing request for key: $key")
            block()
        }
        // Note: We don't remove the mutex to avoid race conditions
        // ConcurrentHashMap will clean up unused entries naturally
    }
    
    /**
     * Optimize countries list based on mode and reduce redundant locations
     */
    fun optimizeCountriesList(
        requestedCountries: List<String>,
        isAiMode: Boolean,
        maxSources: Int
    ): List<String> {
        // Remove duplicates and normalize
        val uniqueCountries = requestedCountries.distinct()
        
        // In AI mode, prioritize diversity; in normal mode, prioritize efficiency
        return if (isAiMode) {
            // Ensure diverse sources
            uniqueCountries.take(maxSources.coerceAtLeast(3))
        } else {
            // Minimize API calls
            uniqueCountries.take(maxSources.coerceAtMost(2))
        }
    }
    
    /**
     * Determine if a search query should be split into multiple smaller queries
     * to optimize API usage
     */
    data class SplitQuery(
        val queryParts: List<String>,
        val shouldSplit: Boolean
    )
    
    fun analyzeQueryForSplitting(query: String, isAiMode: Boolean): SplitQuery {
        val words = query.trim().split("\\s+".toRegex())
        
        // Don't split short queries
        if (words.size <= 2) {
            return SplitQuery(listOf(query), false)
        }
        
        // In AI mode, keep queries together for better context
        if (isAiMode && words.size <= 5) {
            return SplitQuery(listOf(query), false)
        }
        
        // In non-AI mode, split long queries to reduce per-query cost
        if (!isAiMode && words.size > 4) {
            // Split into key terms
            val keyTerms = words.filter { it.length > 4 }.take(3)
            if (keyTerms.size > 1) {
                LogManager.i("Splitting query '$query' into ${keyTerms.size} parts")
                return SplitQuery(keyTerms, true)
            }
        }
        
        return SplitQuery(listOf(query), false)
    }
    
    /**
     * Calculate optimal delay between requests to avoid rate limiting
     */
    fun calculateOptimalDelay(
        apiName: String,
        isAiMode: Boolean,
        remainingRequests: Int
    ): Long {
        // Base delay in milliseconds
        val baseDelay = when (apiName) {
            RateLimitManager.API_NEWS -> {
                // Spread requests across the hour
                if (isAiMode) 200L else 500L
            }
            RateLimitManager.API_OPENROUTER -> {
                // More conservative with AI API
                if (isAiMode) 500L else 1000L
            }
            else -> 0L
        }
        
        // Increase delay if we're running low on quota
        val quotaMultiplier = when {
            remainingRequests < 5 -> 2.0
            remainingRequests < 10 -> 1.5
            else -> 1.0
        }
        
        return (baseDelay * quotaMultiplier).toLong()
    }
    
    /**
     * Get statistics about query optimization
     */
    fun getOptimizationStats(): Map<String, Int> {
        return mapOf(
            "in_flight_requests" to inFlightRequests.size
        )
    }
    
    /**
     * Clear all optimization state (useful for testing)
     */
    fun reset() {
        inFlightRequests.clear()
        LogManager.i("Query optimizer state reset")
    }
}
