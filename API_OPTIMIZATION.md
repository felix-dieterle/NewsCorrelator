# API Rate Limiting & Optimization Guide

## Overview

The NewsCorrelator app implements intelligent mechanisms to minimize API rate limits and optimize API usage. This is critical because:

- **NewsAPI.org** free tier: 100 requests/day
- **OpenRouter.AI** free tier: Limited requests per hour/day depending on model

The optimization system differentiates between **AI mode** (when AI analysis is enabled) and **non-AI mode** to achieve optimal performance.

## Architecture

### Core Components

#### 1. RateLimitManager (`utils/RateLimitManager.kt`)

Tracks and enforces API rate limits to prevent exceeding quotas.

**Features:**
- Per-API request tracking (NewsAPI, OpenRouter)
- Time-window based limiting (minute, hour, day)
- Automatic delay/backoff when limits are approached
- Different limits for AI vs non-AI mode

**Rate Limits:**
- **NewsAPI:**
  - 10 requests/hour (conservative)
  - 90 requests/day (leaving buffer)
  
- **OpenRouter:**
  - 5 requests/minute (prevent bursts)
  - 20 requests/hour
  - In non-AI mode: 3 requests/minute (more conservative)

**Usage:**
```kotlin
// Before making an API call
RateLimitManager.checkAndTrackRequest(
    RateLimitManager.API_NEWS,
    isAiMode = true
)

// Get usage statistics
val stats = RateLimitManager.getUsageStats(RateLimitManager.API_NEWS)
// Returns: Map("hourly" -> 5, "daily" -> 45)
```

**How it works:**
1. Tracks requests in rolling time windows
2. If limit is reached, automatically delays until window resets
3. Logs all rate limiting activity
4. Thread-safe using concurrent data structures

#### 2. CacheManager (`utils/CacheManager.kt`)

Manages caching of API responses to reduce redundant requests.

**Features:**
- TTL-based cache expiration
- Separate caches for news and AI analysis
- Thread-safe operations
- Different TTLs for AI vs non-AI mode

**Cache TTLs:**
- **News responses:**
  - AI mode: 30 minutes (fetch more frequently for better analysis)
  - Normal mode: 1 hour (reduce API calls)
  
- **AI analysis results:**
  - 24 hours (analyses don't change, safe to cache long)

**Usage:**
```kotlin
// Cache a news response
val cacheKey = CacheManager.generateNewsQueryKey(
    category = "technology",
    country = "us",
    query = null,
    pageSize = 20
)
CacheManager.cacheNewsResponse(cacheKey, response, isAiMode = true)

// Retrieve cached response
val cached = CacheManager.getCachedNewsResponse<NewsApiResponse>(cacheKey)

// Cache AI analysis
CacheManager.cacheAiAnalysis(articleUrl, analysisResult)

// Retrieve cached analysis
val cachedAnalysis = CacheManager.getCachedAiAnalysis<IntegrityAnalysis>(articleUrl)
```

**Cache Cleanup:**
- Automatic cleanup via `CacheCleanupWorker` every 6 hours
- Manual cleanup: `CacheManager.cleanupExpiredEntries()`
- Clear all: `CacheManager.clearAll()`

#### 3. QueryOptimizer (`utils/QueryOptimizer.kt`)

Optimizes API queries to reduce redundant requests and batch operations efficiently.

**Features:**
- Query parameter optimization based on mode
- Query batching and deduplication
- Request coalescing (prevents duplicate concurrent requests)
- Smart delay calculation

**AI Mode vs Non-AI Mode:**

| Aspect | AI Mode | Non-AI Mode |
|--------|---------|-------------|
| Page Size | Up to 20 articles | Up to 10 articles |
| Countries/Sources | 3-5 (diverse sources) | 1-2 (minimal) |
| Cache TTL | 30 minutes | 1 hour |
| Request Delay | 200ms | 500ms |

**Usage:**
```kotlin
// Optimize query parameters
val optimization = QueryOptimizer.optimizeNewsQuery(
    isAiMode = true,
    requestedPageSize = 20,
    sourcesPerTopic = 4
)
// Returns: OptimizedNewsQuery(pageSize=20, shouldFetchMultipleSources=true, maxCountries=4)

// Optimize countries list
val countries = QueryOptimizer.optimizeCountriesList(
    requestedCountries = listOf("us", "gb", "de", "fr", "ca"),
    isAiMode = false,
    maxSources = 3
)
// Returns: ["us", "gb"] in non-AI mode, ["us", "gb", "de"] in AI mode

// Calculate optimal delay
val delay = QueryOptimizer.calculateOptimalDelay(
    RateLimitManager.API_NEWS,
    isAiMode = true,
    remainingRequests = 50
)
// Returns: 200ms base delay, increases if quota is low
```

#### 4. CacheCleanupWorker (`workers/CacheCleanupWorker.kt`)

Background worker that periodically cleans up expired cache entries.

**Schedule:**
- Runs every 6 hours
- 1-hour flex period for battery optimization
- Automatically scheduled on app startup

## Integration in NewsRepository

The `NewsRepository` integrates all optimization components:

### News Fetching Flow

```kotlin
suspend fun fetchAndStoreNews(
    apiKey: String,
    categories: List<String>,
    sourcesPerTopic: Int,
    isAiMode: Boolean = false
) {
    // 1. Optimize query parameters based on mode
    val optimization = QueryOptimizer.optimizeNewsQuery(isAiMode, ...)
    
    // 2. Optimize countries list
    val countries = QueryOptimizer.optimizeCountriesList(...)
    
    // 3. For each category and country:
    for (category in categories) {
        for (country in countries) {
            // Generate cache key
            val cacheKey = CacheManager.generateNewsQueryKey(...)
            
            // Check cache first
            val cached = CacheManager.getCachedNewsResponse<NewsApiResponse>(cacheKey)
            
            if (cached != null) {
                // Use cached data (saves API call!)
                use(cached)
            } else {
                // Apply rate limiting
                RateLimitManager.checkAndTrackRequest(API_NEWS, isAiMode)
                
                // Make API call
                val response = newsApiService.getTopHeadlines(...)
                
                // Cache the response
                CacheManager.cacheNewsResponse(cacheKey, response, isAiMode)
                
                // Add delay between requests
                delay(QueryOptimizer.calculateOptimalDelay(...))
            }
        }
    }
}
```

### AI Analysis Flow

```kotlin
suspend fun analyzeArticleIntegrity(
    article: Article,
    apiKey: String,
    isAiMode: Boolean = true
): Article {
    // Check cache first
    val cached = CacheManager.getCachedAiAnalysis<IntegrityAnalysis>(article.url)
    if (cached != null) {
        return article.copy(
            integrityScore = cached.score,
            integrityStatus = cached.status,
            analyzed = true
        )
    }
    
    // Apply rate limiting
    RateLimitManager.checkAndTrackRequest(API_OPENROUTER, isAiMode)
    
    // Make AI API call
    val response = openRouterService.chat(...)
    
    // Parse and cache result
    val analysis = parseAnalysis(response)
    CacheManager.cacheAiAnalysis(article.url, analysis)
    
    return article.copy(...)
}
```

## Benefits

### 1. Reduced API Calls

**Before optimization:**
- 5 categories × 5 countries = 25 API calls
- No caching = repeat calls for same data
- No rate limiting = risk of exceeding quota

**After optimization:**
- AI mode: 3-4 countries × categories = 12-16 calls (35% reduction)
- Non-AI mode: 1-2 countries × categories = 5-10 calls (60-80% reduction)
- Caching: 50-80% of requests served from cache on subsequent loads
- Rate limiting: Prevents quota exhaustion

### 2. Better Resource Utilization

- **Memory:** Caches expire automatically, cleanup runs periodically
- **Network:** Fewer API calls = less data transfer
- **Battery:** Fewer network operations = better battery life

### 3. Improved User Experience

- **Faster loads:** Cached responses return instantly
- **Reliable:** Rate limiting prevents API errors
- **Smart:** AI mode gets more data for better analysis, non-AI mode stays efficient

## Monitoring & Debugging

### Usage Statistics

```kotlin
// Get rate limit stats
val newsStats = RateLimitManager.getUsageStats(RateLimitManager.API_NEWS)
val aiStats = RateLimitManager.getUsageStats(RateLimitManager.API_OPENROUTER)

// Get cache stats
val cacheStats = CacheManager.getCacheStats()
// Returns: Map("news_cache_size" -> 15, "ai_analysis_cache_size" -> 8, "total_cache_size" -> 23)

// Get query optimizer stats
val optimizerStats = QueryOptimizer.getOptimizationStats()
```

### Logs

All components log their activity using `LogManager`:

```
[RateLimitManager] NewsAPI request tracked: 5/10 hourly, 45/90 daily
[CacheManager] Cache hit for key: news|technology|us|null|20
[CacheManager] Cached news response for key: news|business|gb|null|20 (TTL: 1800s)
[QueryOptimizer] Optimized: pageSize=10, countries=2
```

### Manual Controls

```kotlin
// Reset rate limiters (for testing)
RateLimitManager.resetAll()

// Clear all caches
CacheManager.clearAll()

// Clear specific cache
CacheManager.clearNewsCache()
CacheManager.clearAiAnalysisCache()

// Reset query optimizer
QueryOptimizer.reset()
```

## Configuration

### Adjusting Rate Limits

Edit `RateLimitManager.kt`:

```kotlin
// NewsAPI limits
private const val NEWS_API_LIMIT_PER_HOUR = 10 // Adjust as needed
private const val NEWS_API_LIMIT_PER_DAY = 90

// OpenRouter limits
private const val OPENROUTER_LIMIT_PER_HOUR = 20
private const val OPENROUTER_LIMIT_PER_MINUTE = 5
```

### Adjusting Cache TTLs

Edit `CacheManager.kt`:

```kotlin
// News cache TTLs
private const val NEWS_CACHE_TTL_AI_MODE = 1_800_000L // 30 minutes
private const val NEWS_CACHE_TTL_NORMAL = 3_600_000L // 1 hour

// AI analysis cache TTL
private const val AI_ANALYSIS_CACHE_TTL = 86_400_000L // 24 hours
```

### Adjusting Request Delays

Edit `QueryOptimizer.kt`:

```kotlin
fun calculateOptimalDelay(...): Long {
    val baseDelay = when (apiName) {
        API_NEWS -> if (isAiMode) 200L else 500L
        API_OPENROUTER -> if (isAiMode) 500L else 1000L
        else -> 0L
    }
    // ...
}
```

## Best Practices

1. **Enable AI mode only when needed:** Non-AI mode is more efficient for simple browsing
2. **Let caches warm up:** First load may be slower, subsequent loads will be fast
3. **Monitor logs:** Check for rate limiting warnings
4. **Adjust limits conservatively:** Better to be under quota than over
5. **Clear caches strategically:** Only clear when necessary (e.g., after API errors)

## Future Enhancements

Potential improvements:

1. **Persistent cache:** Store cache to disk for survival across app restarts
2. **Adaptive rate limiting:** Adjust limits based on actual API responses
3. **Smart prefetching:** Predict and prefetch likely-needed data
4. **Compression:** Compress cached data to save memory
5. **Priority queuing:** Prioritize user-initiated requests over background refreshes
6. **Network-aware optimization:** Adjust behavior based on WiFi vs cellular
