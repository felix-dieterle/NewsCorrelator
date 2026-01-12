package com.newscorrelator.app.api

data class NewsApiResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<NewsApiArticle>
)

data class NewsApiArticle(
    val source: NewsApiSource,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?
)

data class NewsApiSource(
    val id: String?,
    val name: String
)
