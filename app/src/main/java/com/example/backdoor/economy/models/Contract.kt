package com.example.backdoor.economy.models

import java.util.UUID

data class Contract(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val type: ContractType,
    val difficulty: Int, // 1 to 10
    val requiredReputation: Int,
    val rewardAmount: Long,
    val rewardCurrency: CurrencyType,
    val timeLimitSeconds: Long? = null,
    val issuer: String, // NPC or Corp Name
    val status: ContractStatus = ContractStatus.AVAILABLE,
    val risks: List<String> = emptyList(),
    val requiredHardware: List<String> = emptyList(),
    val completionCriteria: String
)

enum class ContractType {
    PUBLIC, CORPORATE, GOVERNMENT, RESEARCH, DARKNET, FREELANCE, NPC
}

enum class ContractStatus {
    AVAILABLE,
    ACCEPTED,
    COMPLETED,
    FAILED,
    EXPIRED
}
