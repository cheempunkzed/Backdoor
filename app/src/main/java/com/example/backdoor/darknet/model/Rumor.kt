package com.example.backdoor.darknet.model

import java.util.UUID

data class Rumor(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val sourceHandle: String,
    val credibility: Float, // 0.0f (complete fake) to 1.0f (verified fact)
    val timestamp: Long = System.currentTimeMillis(),
    val propagatedCount: Int = (5..150).random(),
    val targetHostOrCorp: String? = null,
    val isTrue: Boolean = true
)

enum class UndergroundEventType(val displayName: String) {
    EXIT_NODE_COMPROMISED("Exit Node Compromised"),
    FORUM_RAID("Law Enforcement Forum Raid"),
    MARKETPLACE_SHUTDOWN("Marketplace Exit Scam"),
    ADMIN_DISAPPEARED("Key Operator Missing"),
    MASSIVE_DATA_LEAK("Massive Corporate Data Leak"),
    CRYPTO_CRASH("Cryptocurrency Flash Crash"),
    HIDDEN_SERVICE_MIGRATION("Hidden Service Emergency Migration")
}

data class UndergroundEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: UndergroundEventType,
    val headline: String,
    val description: String,
    val affectedAddress: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val heatImpact: Int = 10
)
