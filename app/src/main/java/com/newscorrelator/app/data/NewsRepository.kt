package com.newscorrelator.app.data

import android.util.Log
import com.google.gson.Gson
import com.newscorrelator.app.api.*
import com.newscorrelator.app.utils.hashString
import kotlinx.coroutines.Dispatchers
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

    suspend fun fetchAndStoreNews(apiKey: String, categories: List<String>, sourcesPerTopic: Int) {
        withContext(Dispatchers.IO) {
            try {
                val countries = listOf("us", "gb", "de", "fr", "ca") // Diverse sources
                val allArticles = mutableListOf<Article>()

                for (category in categories) {
                    val articlesForCategory = mutableListOf<NewsApiArticle>()
                    
                    // Fetch from multiple countries for diversity
                    for (country in countries.take(sourcesPerTopic)) {
                        try {
                            val response = newsApiService.getTopHeadlines(
                                apiKey = apiKey,
                                category = category,
                                country = country,
                                pageSize = 5
                            )
                            articlesForCategory.addAll(response.articles)
                        } catch (e: Exception) {
                            Log.e("NewsRepository", "Error fetching from $country: ${e.message}")
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
                articleDao.insertArticles(allArticles)

                // Group articles by topic
                groupArticlesByTopic(allArticles)
                
            } catch (e: Exception) {
                Log.e("NewsRepository", "Error fetching news: ${e.message}", e)
                throw e
            }
        }
    }

    private fun generateTopicHash(title: String): String {
        // Simple topic extraction - use main keywords
        val words = title.toLowerCase(Locale.getDefault())
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

    suspend fun analyzeArticleIntegrity(article: Article, apiKey: String): Article {
        return withContext(Dispatchers.IO) {
            try {
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

                // Update source trust score based on article integrity
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

                article.copy(
                    integrityScore = analysis.score,
                    integrityStatus = analysis.status,
                    analyzed = true
                )
            } catch (e: Exception) {
                Log.e("NewsRepository", "Error analyzing article: ${e.message}", e)
                article.copy(
                    integrityScore = 5.0f,
                    integrityStatus = "YELLOW",
                    analyzed = true
                )
            }
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
