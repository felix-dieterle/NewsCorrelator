package com.newscorrelator.app.api

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("apiKey") apiKey: String,
        @Query("category") category: String? = null,
        @Query("country") country: String? = null,
        @Query("q") query: String? = null,
        @Query("pageSize") pageSize: Int = 20
    ): NewsApiResponse
    
    @GET("everything")
    suspend fun searchEverything(
        @Query("apiKey") apiKey: String,
        @Query("q") query: String,
        @Query("language") language: String? = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 20
    ): NewsApiResponse
}
