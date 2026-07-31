package com.example.backdoor.economy.models

import java.util.UUID

data class MarketListing(
    val id: String = UUID.randomUUID().toString(),
    val item: Item,
    val price: Long,
    val currency: CurrencyType,
    val sellerId: String,
    val sellerReputation: Int,
    val stock: Int = 1,
    val isDarkMarket: Boolean = false
)
