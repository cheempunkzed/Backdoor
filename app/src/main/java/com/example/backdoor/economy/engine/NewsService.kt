package com.example.backdoor.economy.engine

import com.example.backdoor.economy.models.NewsArticle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NewsService {
    private val _feed = MutableStateFlow<List<NewsArticle>>(emptyList())
    val feed: StateFlow<List<NewsArticle>> = _feed.asStateFlow()

    fun publishArticle(article: NewsArticle) {
        _feed.update { listOf(article) + it }
    }

    fun restore(savedNews: List<NewsArticle>) {
        _feed.value = savedNews
    }
}
