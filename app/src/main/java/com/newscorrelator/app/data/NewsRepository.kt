package com.newscorrelator.app.data

import android.util.Log
import com.google.gson.Gson
import com.newscorrelator.app.api.*
import com.newscorrelator.app.utils.CacheManager
import com.newscorrelator.app.utils.LogManager
import com.newscorrelator.app.utils.QueryOptimizer
import com.newscorrelator.app.utils.RateLimitManager
import com.newscorrelator.app.utils.hashString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class NewsRepository(
    private val articleDao: ArticleDao,
    private val sourceDao: SourceDao,
    private val userPreferenceDao: UserPreferenceDao,
    private val articleGroupDao: ArticleGroupDao
) {
    private val newsApiService = ApiClient.newsApiService
    private val openRouterService = ApiClient.openRouterService
    private val gson = Gson()

    suspend fun fetchAndStoreNews(apiKey: String, categories: List<String>, sourcesPerTopic: Int, isAiMode: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                LogManager.i("NewsRepository.fetchAndStoreNews() started (AI mode: $isAiMode)")
                LogManager.i("Categories: ${categories.joinToString(", ")}, sourcesPerTopic: $sourcesPerTopic")
                
                // Optimize query parameters based on mode
                val queryOptimization = QueryOptimizer.optimizeNewsQuery(
                    isAiMode = isAiMode,
                    requestedPageSize = 5,
                    sourcesPerTopic = sourcesPerTopic
                )
                
                val countries = listOf("us", "gb", "de", "fr", "ca") // Diverse sources
                val optimizedCountries = QueryOptimizer.optimizeCountriesList(
                    requestedCountries = countries,
                    isAiMode = isAiMode,
                    maxSources = queryOptimization.maxCountries
                )
                
                LogManager.i("Optimized: pageSize=${queryOptimization.pageSize}, countries=${optimizedCountries.size}")
                
                val allArticles = mutableListOf<Article>()

                for (category in categories) {
                    LogManager.i("Fetching news for category: $category")
                    val articlesForCategory = mutableListOf<NewsApiArticle>()
                    
                    // Fetch from multiple countries for diversity
                    for (country in optimizedCountries) {
                        try {
                            // Generate cache key
                            val cacheKey = CacheManager.generateNewsQueryKey(
                                category = category,
                                country = country,
                                query = null,
                                pageSize = queryOptimization.pageSize
                            )
                            
                            // Check cache first
                            val cachedResponse = CacheManager.getCachedNewsResponse<NewsApiResponse>(cacheKey)
                            val response = if (cachedResponse != null) {
                                LogManager.i("Using cached response for $country/$category")
                                cachedResponse
                            } else {
                                // Apply rate limiting before API call
                                RateLimitManager.checkAndTrackRequest(
                                    RateLimitManager.API_NEWS,
                                    isAiMode
                                )
                                
                                LogManager.i("Fetching from country: $country")
                                val apiResponse = newsApiService.getTopHeadlines(
                                    apiKey = apiKey,
                                    category = category,
                                    country = country,
                                    pageSize = queryOptimization.pageSize
                                )
                                
                                // Cache the response
                                CacheManager.cacheNewsResponse(cacheKey, apiResponse, isAiMode)
                                apiResponse
                            }
                            
                            articlesForCategory.addAll(response.articles)
                            LogManager.i("Got ${response.articles.size} articles from $country")
                            
                            // Add small delay between requests to be respectful
                            val stats = RateLimitManager.getUsageStats(RateLimitManager.API_NEWS)
                            val usedRequests = stats["hourly"] ?: 0
                            val remainingRequests = RateLimitManager.NEWS_API_LIMIT_PER_HOUR - usedRequests
                            val delayTime = QueryOptimizer.calculateOptimalDelay(
                                RateLimitManager.API_NEWS,
                                isAiMode,
                                remainingRequests.coerceAtLeast(0)
                            )
                            if (delayTime > 0) {
                                delay(delayTime)
                            }
                        } catch (e: Exception) {
                            LogManager.e("Error fetching from $country: ${e.message}", e)
                        }
                    }

                    // Convert to Article entities
                    val articles = articlesForCategory.map { apiArticle ->
                        Article(
                            title = apiArticle.title,
                            description = apiArticle.description,
                            content = apiArticle.content,
                            url = apiArticle.url,
                            imageUrl = apiArticle.urlToImage,
                            publishedAt = parseDate(apiArticle.publishedAt),
                            source = apiArticle.source.name,
                            sourceId = apiArticle.source.id ?: apiArticle.source.name,
                            country = null,
                            category = category,
                            topicHash = generateTopicHash(apiArticle.title)
                        )
                    }
                    allArticles.addAll(articles)
                    LogManager.i("Converted ${articles.size} articles for category $category")

                    // Update sources
                    articlesForCategory.forEach { apiArticle ->
                        val sourceId = apiArticle.source.id ?: apiArticle.source.name
                        val existingSource = sourceDao.getSourceById(sourceId)
                        if (existingSource == null) {
                            sourceDao.insertSource(
                                Source(
                                    id = sourceId,
                                    name = apiArticle.source.name,
                                    country = null,
                                    category = category
                                )
                            )
                        }
                    }
                }

                // Store articles
                LogManager.i("Storing ${allArticles.size} articles in database")
                articleDao.insertArticles(allArticles)
                LogManager.i("Articles stored successfully")

                // Group articles by topic
                LogManager.i("Grouping articles by topic")
                groupArticlesByTopic(allArticles)
                LogManager.i("NewsRepository.fetchAndStoreNews() completed successfully")
                
            } catch (e: Exception) {
                LogManager.e("Error in fetchAndStoreNews: ${e.message}", e)
                throw e
            }
        }
    }

    private fun generateTopicHash(title: String): String {
        // Simple topic extraction - use main keywords
        val words = title.lowercase(Locale.getDefault())
            .split(" ")
            .filter { it.length > 4 }
            .take(3)
            .sorted()
            .joinToString("")
        return hashString(words)
    }

    private suspend fun groupArticlesByTopic(articles: List<Article>) {
        val groupedByHash = articles.groupBy { it.topicHash }
        for ((hash, groupArticles) in groupedByHash) {
            if (hash != null && groupArticles.size > 1) {
                val existingGroup = articleGroupDao.getGroupByHash(hash)
                if (existingGroup == null) {
                    articleGroupDao.insertGroup(
                        ArticleGroup(
                            topicHash = hash,
                            topicTitle = groupArticles.first().title,
                            articleCount = groupArticles.size
                        )
                    )
                }
            }
        }
    }

    suspend fun analyzeArticleIntegrity(article: Article, apiKey: String, isAiMode: Boolean = true): Article {
        return withContext(Dispatchers.IO) {
            try {
                // Check cache first
                val cachedAnalysis = CacheManager.getCachedAiAnalysis<IntegrityAnalysis>(article.url)
                if (cachedAnalysis != null) {
                    LogManager.i("Using cached analysis for article: ${article.title}")
                    
                    // Update source trust score
                    updateSourceTrustScore(article, cachedAnalysis)
                    
                    return@withContext article.copy(
                        integrityScore = cachedAnalysis.score,
                        integrityStatus = cachedAnalysis.status,
                        analyzed = true
                    )
                }
                
                // Apply rate limiting before AI API call
                RateLimitManager.checkAndTrackRequest(
                    RateLimitManager.API_OPENROUTER,
                    isAiMode
                )
                
                val prompt = """
                    Analyze this news article for integrity and potential manipulation:
                    
                    Title: ${article.title}
                    Description: ${article.description}
                    Source: ${article.source}
                    
                    Provide a JSON response with:
                    1. score (1-10, where 10 is highest integrity)
                    2. status (RED for score 1-3, YELLOW for 4-7, GREEN for 8-10)
                    3. reasoning (brief explanation)
                    4. manipulationIndicators (list of any red flags)
                    5. factCheckResults (brief assessment)
                    
                    Format: {"score": X, "status": "COLOR", "reasoning": "...", "manipulationIndicators": [...], "factCheckResults": "..."}
                """.trimIndent()

                val request = OpenRouterRequest(
                    messages = listOf(
                        Message(role = "user", content = prompt)
                    )
                )

                val response = openRouterService.chat(
                    authorization = "Bearer $apiKey",
                    request = request
                )

                val analysisText = response.choices.firstOrNull()?.message?.content ?: ""
                
                // Parse JSON response
                val analysis = try {
                    gson.fromJson(analysisText, IntegrityAnalysis::class.java)
                } catch (e: Exception) {
                    // Fallback if AI doesn't return proper JSON
                    IntegrityAnalysis(
                        score = 5.0f,
                        status = "YELLOW",
                        reasoning = "Analysis completed but format unclear",
                        manipulationIndicators = emptyList(),
                        factCheckResults = analysisText
                    )
                }
                
                // Cache the analysis result
                CacheManager.cacheAiAnalysis(article.url, analysis)
                
                // Update source trust score based on article integrity
                updateSourceTrustScore(article, analysis)

                article.copy(
                    integrityScore = analysis.score,
                    integrityStatus = analysis.status,
                    analyzed = true
                )
            } catch (e: Exception) {
                LogManager.e("Error analyzing article: ${e.message}", e)
                article.copy(
                    integrityScore = 5.0f,
                    integrityStatus = "YELLOW",
                    analyzed = true
                )
            }
        }
    }
    
    private suspend fun updateSourceTrustScore(article: Article, analysis: IntegrityAnalysis) {
        val source = sourceDao.getSourceById(article.sourceId)
        if (source != null) {
            val newScore = ((source.trustScore * source.articlesAnalyzed) + analysis.score) / 
                           (source.articlesAnalyzed + 1)
            sourceDao.updateSource(
                source.copy(
                    trustScore = newScore,
                    articlesAnalyzed = source.articlesAnalyzed + 1,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    private fun parseDate(dateString: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    suspend fun getPreferences() = userPreferenceDao.getPreferencesSync()
    
    suspend fun savePreferences(preferences: UserPreference) {
        val existing = userPreferenceDao.getPreferencesSync()
        if (existing == null) {
            userPreferenceDao.insertPreference(preferences)
        } else {
            userPreferenceDao.updatePreference(preferences.copy(id = existing.id))
        }
    }
}
