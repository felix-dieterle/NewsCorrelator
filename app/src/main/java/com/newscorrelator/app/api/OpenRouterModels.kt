package com.newscorrelator.app.api

data class OpenRouterRequest(
    val model: String = "meta-llama/llama-3.2-3b-instruct:free", // Free model
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

data class OpenRouterResponse(
    val id: String,
    val choices: List<Choice>
)

data class Choice(
    val message: Message,
    val finish_reason: String?
)

data class IntegrityAnalysis(
    val score: Float, // 1-10
    val status: String, // RED, YELLOW, GREEN
    val reasoning: String,
    val manipulationIndicators: List<String>,
    val factCheckResults: String
)
