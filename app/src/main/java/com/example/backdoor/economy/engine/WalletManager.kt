package com.example.backdoor.economy.engine

import com.example.backdoor.economy.models.CurrencyType
import com.example.backdoor.economy.models.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WalletManager {
    private val _balances = MutableStateFlow(
        mapOf(
            CurrencyType.CREDITS to 1500L,
            CurrencyType.ABYSS_COIN to 0L,
            CurrencyType.CORPORATE_TOKENS to 0L,
            CurrencyType.RESEARCH_TOKENS to 0L
        )
    )
    val balances: StateFlow<Map<CurrencyType, Long>> = _balances.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    fun getBalance(type: CurrencyType): Long {
        return _balances.value[type] ?: 0L
    }

    fun addFunds(amount: Long, currency: CurrencyType, description: String) {
        if (amount <= 0) return
        _balances.update { current ->
            val updated = current.toMutableMap()
            updated[currency] = (updated[currency] ?: 0L) + amount
            updated
        }
        addTransaction(Transaction(amount = amount, currency = currency, description = description, isIncoming = true))
    }

    fun deductFunds(amount: Long, currency: CurrencyType, description: String): Boolean {
        if (amount <= 0) return true
        val currentBalance = _balances.value[currency] ?: 0L
        if (currentBalance < amount) return false

        _balances.update { current ->
            val updated = current.toMutableMap()
            updated[currency] = currentBalance - amount
            updated
        }
        addTransaction(Transaction(amount = amount, currency = currency, description = description, isIncoming = false))
        return true
    }

    private fun addTransaction(tx: Transaction) {
        _transactions.update { it + tx }
    }

    fun restore(balances: Map<CurrencyType, Long>, txs: List<Transaction>) {
        _balances.value = balances
        _transactions.value = txs
    }
}
