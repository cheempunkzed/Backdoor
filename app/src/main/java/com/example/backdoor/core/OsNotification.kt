package com.example.backdoor.core

import java.util.UUID

enum class NotificationLevel {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

data class OsNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val level: NotificationLevel = NotificationLevel.INFO,
    val timestamp: Long = System.currentTimeMillis()
)
