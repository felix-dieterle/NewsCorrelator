package com.newscorrelator.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.newscorrelator.app.data.*
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

    fun refreshNews() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val prefs = repository.getPreferences()
                if (prefs == null || prefs.newsApiKey.isEmpty()) {
                    _error.value = "Please configure API keys in Settings"
                    return@launch
                }

                val categories = prefs.categories.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                repository.fetchAndStoreNews(
                    apiKey = prefs.newsApiKey,
                    categories = categories,
                    sourcesPerTopic = prefs.sourcesPerTopic
                )
            } catch (e: Exception) {
                _error.value = "Error fetching news: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun analyzeArticle(article: Article) {
        viewModelScope.launch {
            try {
                val prefs = repository.getPreferences()
                if (prefs == null || !prefs.enableAiAnalysis || prefs.openRouterApiKey.isEmpty()) {
                    return@launch
                }

                val analyzed = repository.analyzeArticleIntegrity(article, prefs.openRouterApiKey)
                database.articleDao().updateArticle(analyzed)
            } catch (e: Exception) {
                _error.value = "Error analyzing article: ${e.message}"
            }
        }
    }

    fun toggleSaveArticle(article: Article) {
        viewModelScope.launch {
            database.articleDao().updateArticle(article.copy(saved = !article.saved))
        }
    }

    fun savePreferences(preferences: UserPreference) {
        viewModelScope.launch {
            repository.savePreferences(preferences)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
