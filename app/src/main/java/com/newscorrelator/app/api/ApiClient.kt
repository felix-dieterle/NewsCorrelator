package com.newscorrelator.app.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val NEWS_API_BASE_URL = "https://newsapi.org/v2/"
    private const val OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val newsApiRetrofit = Retrofit.Builder()
        .baseUrl(NEWS_API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val openRouterRetrofit = Retrofit.Builder()
        .baseUrl(OPENROUTER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val newsApiService: NewsApiService = newsApiRetrofit.create(NewsApiService::class.java)
    val openRouterService: OpenRouterService = openRouterRetrofit.create(OpenRouterService::class.java)
}
