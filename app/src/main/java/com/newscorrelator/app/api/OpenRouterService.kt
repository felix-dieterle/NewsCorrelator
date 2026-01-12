package com.newscorrelator.app.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterService {
    @POST("chat/completions")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String = "https://github.com/felix-dieterle/NewsCorrelator",
        @Header("X-Title") title: String = "NewsCorrelator",
        @Body request: OpenRouterRequest
    ): OpenRouterResponse
}
