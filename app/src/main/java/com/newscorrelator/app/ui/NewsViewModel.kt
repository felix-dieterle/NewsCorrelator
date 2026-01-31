package com.newscorrelator.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.newscorrelator.app.data.*
import com.newscorrelator.app.utils.LogManager
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NewsDatabase.getDatabase(application)
    
    private val repository = NewsRepository(
        database.articleDao(),
        database.sourceDao(),
        database.userPreferenceDao(),
        database.articleGroupDao()
    )

    val articles: LiveData<List<Article>> = database.articleDao().getAllArticles()
    val savedArticles: LiveData<List<Article>> = database.articleDao().getSavedArticles()
    val sources: LiveData<List<Source>> = database.sourceDao().getAllSources()
    val preferences: LiveData<UserPreference?> = database.userPreferenceDao().getPreferences()
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        LogManager.i("NewsViewModel initializing")
        LogManager.i("NewsDatabase obtained")
        LogManager.i("NewsRepository created")
        LogManager.i("NewsViewModel initialized successfully")
    }

    fun refreshNews() {
        LogManager.i("refreshNews() called")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                LogManager.i("Getting preferences")
                val prefs = repository.getPreferences()
                if (prefs == null || prefs.newsApiKey.isEmpty()) {
                    val errorMsg = "Please configure API keys in Settings"
                    LogManager.w(errorMsg)
                    _error.value = errorMsg
                    return@launch
                }

                LogManager.i("Preferences found - API key configured")
                val categories = prefs.categories.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                LogManager.i("Categories: ${categories.joinToString(", ")}")
                LogManager.i("Sources per topic: ${prefs.sourcesPerTopic}")
                
                LogManager.i("Fetching news from API")
                repository.fetchAndStoreNews(
                    apiKey = prefs.newsApiKey,
                    categories = categories,
                    sourcesPerTopic = prefs.sourcesPerTopic
                )
                LogManager.i("News fetched and stored successfully")
            } catch (e: Exception) {
                val errorMsg = "Error fetching news: ${e.message}"
                LogManager.e(errorMsg, e)
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
                LogManager.i("refreshNews() completed")
            }
        }
    }

    fun analyzeArticle(article: Article) {
        LogManager.i("analyzeArticle() called for: ${article.title}")
        viewModelScope.launch {
            try {
                val prefs = repository.getPreferences()
                if (prefs == null || !prefs.enableAiAnalysis || prefs.openRouterApiKey.isEmpty()) {
                    LogManager.w("AI analysis not enabled or API key not configured")
                    return@launch
                }

                LogManager.i("Analyzing article integrity")
                val analyzed = repository.analyzeArticleIntegrity(article, prefs.openRouterApiKey)
                database.articleDao().updateArticle(analyzed)
                LogManager.i("Article analyzed successfully")
            } catch (e: Exception) {
                val errorMsg = "Error analyzing article: ${e.message}"
                LogManager.e(errorMsg, e)
                _error.value = errorMsg
            }
        }
    }

    fun toggleSaveArticle(article: Article) {
        LogManager.i("toggleSaveArticle() called for: ${article.title}")
        viewModelScope.launch {
            database.articleDao().updateArticle(article.copy(saved = !article.saved))
            LogManager.i("Article save status toggled")
        }
    }

    fun savePreferences(preferences: UserPreference) {
        LogManager.i("savePreferences() called")
        viewModelScope.launch {
            repository.savePreferences(preferences)
            LogManager.i("Preferences saved")
        }
    }

    fun clearError() {
        _error.value = null
    }
}
