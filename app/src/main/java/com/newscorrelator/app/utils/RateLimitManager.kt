package com.newscorrelator.app.utils

import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Manages API rate limits to prevent exceeding API quotas
 * 
 * Implements intelligent rate limiting with:
 * - Per-API request tracking
 * - Time-window based limiting
 * - Automatic delay/backoff
 * - Different limits for AI vs non-AI mode
 */
object RateLimitManager {
    
    // API identifiers
    const val API_NEWS = "news_api"
    const val API_OPENROUTER = "openrouter_api"
    
    // Rate limits (requests per time window)
    // NewsAPI: 100 requests/day free tier
    private const val NEWS_API_LIMIT_PER_HOUR = 10 // Conservative: ~240/day max
    private const val NEWS_API_LIMIT_PER_DAY = 90 // Leave buffer for errors
    
    // OpenRouter: Varies by model, but free tier has limits
    private const val OPENROUTER_LIMIT_PER_HOUR = 20 // Conservative for free tier
    private const val OPENROUTER_LIMIT_PER_MINUTE = 5 // Prevent burst
    
    // Time windows in milliseconds
    private const val ONE_MINUTE = 60_000L
    private const val ONE_HOUR = 3_600_000L
    private const val ONE_DAY = 86_400_000L
    
    // Request tracking
    private data class RequestWindow(
        val count: AtomicInteger = AtomicInteger(0),
        var windowStart: Long = System.currentTimeMillis()
    )
    
    private val requestCounters = ConcurrentHashMap<String, MutableMap<Long, RequestWindow>>()
    
    /**
     * Check if request can proceed and track it
     * Returns delay in milliseconds (0 if can proceed immediately)
     */
    suspend fun checkAndTrackRequest(apiName: String, isAiMode: Boolean = false): Long {
        val now = System.currentTimeMillis()
        
        return when (apiName) {
            API_NEWS -> checkNewsApiLimit(now)
            API_OPENROUTER -> checkOpenRouterLimit(now, isAiMode)
            else -> 0L
        }
    }
    
    private suspend fun checkNewsApiLimit(now: Long): Long {
        val hourlyKey = "$API_NEWS:hour"
        val dailyKey = "$API_NEWS:day"
        
        // Check hourly limit
        val hourlyWindow = getOrCreateWindow(hourlyKey, ONE_HOUR, now)
        if (hourlyWindow.count.get() >= NEWS_API_LIMIT_PER_HOUR) {
            val waitTime = calculateWaitTime(hourlyWindow.windowStart, ONE_HOUR, now)
            if (waitTime > 0) {
                LogManager.w("NewsAPI hourly limit reached. Waiting ${waitTime}ms")
                delay(waitTime)
                resetWindow(hourlyKey, ONE_HOUR, now)
            }
        }
        
        // Check daily limit
        val dailyWindow = getOrCreateWindow(dailyKey, ONE_DAY, now)
        if (dailyWindow.count.get() >= NEWS_API_LIMIT_PER_DAY) {
            val waitTime = calculateWaitTime(dailyWindow.windowStart, ONE_DAY, now)
            if (waitTime > 0) {
                LogManager.w("NewsAPI daily limit reached. Waiting ${waitTime}ms")
                delay(waitTime)
                resetWindow(dailyKey, ONE_DAY, now)
            }
        }
        
        // Increment counters
        hourlyWindow.count.incrementAndGet()
        dailyWindow.count.incrementAndGet()
        
        LogManager.i("NewsAPI request tracked: ${hourlyWindow.count.get()}/$NEWS_API_LIMIT_PER_HOUR hourly, ${dailyWindow.count.get()}/$NEWS_API_LIMIT_PER_DAY daily")
        
        return 0L
    }
    
    private suspend fun checkOpenRouterLimit(now: Long, isAiMode: Boolean): Long {
        val minuteKey = "$API_OPENROUTER:minute"
        val hourlyKey = "$API_OPENROUTER:hour"
        
        // In non-AI mode, we might want to be more conservative
        val minuteLimit = if (isAiMode) OPENROUTER_LIMIT_PER_MINUTE else 3
        
        // Check minute limit (prevent bursts)
        val minuteWindow = getOrCreateWindow(minuteKey, ONE_MINUTE, now)
        if (minuteWindow.count.get() >= minuteLimit) {
            val waitTime = calculateWaitTime(minuteWindow.windowStart, ONE_MINUTE, now)
            if (waitTime > 0) {
                LogManager.w("OpenRouter minute limit reached. Waiting ${waitTime}ms")
                delay(waitTime)
                resetWindow(minuteKey, ONE_MINUTE, now)
            }
        }
        
        // Check hourly limit
        val hourlyWindow = getOrCreateWindow(hourlyKey, ONE_HOUR, now)
        if (hourlyWindow.count.get() >= OPENROUTER_LIMIT_PER_HOUR) {
            val waitTime = calculateWaitTime(hourlyWindow.windowStart, ONE_HOUR, now)
            if (waitTime > 0) {
                LogManager.w("OpenRouter hourly limit reached. Waiting ${waitTime}ms")
                delay(waitTime)
                resetWindow(hourlyKey, ONE_HOUR, now)
            }
        }
        
        // Increment counters
        minuteWindow.count.incrementAndGet()
        hourlyWindow.count.incrementAndGet()
        
        LogManager.i("OpenRouter request tracked: ${minuteWindow.count.get()}/$minuteLimit per minute, ${hourlyWindow.count.get()}/$OPENROUTER_LIMIT_PER_HOUR hourly")
        
        return 0L
    }
    
    private fun getOrCreateWindow(key: String, windowSize: Long, now: Long): RequestWindow {
        val windows = requestCounters.getOrPut(key) { mutableMapOf() }
        val window = windows.getOrPut(windowSize) { RequestWindow() }
        
        // Reset window if expired
        if (now - window.windowStart > windowSize) {
            window.count.set(0)
            window.windowStart = now
        }
        
        return window
    }
    
    private fun resetWindow(key: String, windowSize: Long, now: Long) {
        val windows = requestCounters[key] ?: return
        val window = windows[windowSize] ?: return
        window.count.set(0)
        window.windowStart = now
    }
    
    private fun calculateWaitTime(windowStart: Long, windowSize: Long, now: Long): Long {
        val elapsed = now - windowStart
        return if (elapsed < windowSize) {
            (windowSize - elapsed).coerceAtLeast(0)
        } else {
            0L
        }
    }
    
    /**
     * Get current usage statistics for an API
     */
    fun getUsageStats(apiName: String): Map<String, Int> {
        val stats = mutableMapOf<String, Int>()
        
        when (apiName) {
            API_NEWS -> {
                requestCounters["$API_NEWS:hour"]?.get(ONE_HOUR)?.let {
                    stats["hourly"] = it.count.get()
                }
                requestCounters["$API_NEWS:day"]?.get(ONE_DAY)?.let {
                    stats["daily"] = it.count.get()
                }
            }
            API_OPENROUTER -> {
                requestCounters["$API_OPENROUTER:minute"]?.get(ONE_MINUTE)?.let {
                    stats["minute"] = it.count.get()
                }
                requestCounters["$API_OPENROUTER:hour"]?.get(ONE_HOUR)?.let {
                    stats["hourly"] = it.count.get()
                }
            }
        }
        
        return stats
    }
    
    /**
     * Reset all counters (useful for testing or manual reset)
     */
    fun resetAll() {
        requestCounters.clear()
        LogManager.i("All rate limit counters reset")
    }
}
