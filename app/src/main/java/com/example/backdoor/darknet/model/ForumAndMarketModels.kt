package com.example.backdoor.darknet.model

data class ForumPost(
    val id: String,
    val authorHandle: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val upvotes: Int = (0..45).random(),
    val isPinned: Boolean = false
)

data class ForumThread(
    val id: String,
    val title: String,
    val authorHandle: String,
    val category: String,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val requiredAccess: AccessLevel = AccessLevel.PUBLIC,
    val posts: MutableList<ForumPost> = mutableListOf(),
    val views: Int = (120..4200).random(),
    val isLocked: Boolean = false
)

data class DarkMarketListing(
    val id: String,
    val title: String,
    val sellerHandle: String,
    val category: String,
    val description: String,
    val priceCredits: Double,
    val sellerRating: Float = 4.8f,
    val salesCount: Int = (5..350).random(),
    val available: Boolean = true
)
