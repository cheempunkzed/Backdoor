package com.example.backdoor.core

import java.util.UUID

enum class NotificationLevel {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

enum class NotificationPriority {
    SYSTEM,
    IMPORTANT,
    INFO,
    BACKGROUND
}

data class OsNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val level: NotificationLevel = NotificationLevel.INFO,
    val priority: NotificationPriority = NotificationPriority.INFO,
    val timestamp: Long = System.currentTimeMillis()
)
