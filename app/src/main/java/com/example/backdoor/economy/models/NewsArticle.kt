package com.example.backdoor.economy.models

import java.util.UUID

data class NewsArticle(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val category: NewsCategory,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "Global News Network"
)

enum class NewsCategory {
    MARKET, CORPORATE, TECHNOLOGY, DARKNET, SECURITY
}
