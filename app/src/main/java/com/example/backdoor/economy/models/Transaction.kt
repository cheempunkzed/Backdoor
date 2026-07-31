package com.example.backdoor.economy.models

import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val amount: Long,
    val currency: CurrencyType,
    val description: String,
    val isIncoming: Boolean,
    val status: TransactionStatus = TransactionStatus.COMPLETED
)

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    FROZEN
}
