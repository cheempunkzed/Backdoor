package com.example.backdoor.economy.models

import java.util.UUID

data class EmailMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val subject: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val attachments: List<String> = emptyList() // could be item IDs or file paths
)
