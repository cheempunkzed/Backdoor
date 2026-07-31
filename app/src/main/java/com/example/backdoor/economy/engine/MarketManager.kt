package com.example.backdoor.economy.engine

import com.example.backdoor.economy.models.MarketListing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MarketManager {
    private val _listings = MutableStateFlow<List<MarketListing>>(emptyList())
    val listings: StateFlow<List<MarketListing>> = _listings.asStateFlow()

    fun addListing(listing: MarketListing) {
        _listings.update { it + listing }
    }

    fun removeListing(listingId: String) {
        _listings.update { it.filterNot { l -> l.id == listingId } }
    }

    fun decreaseStock(listingId: String, amount: Int = 1) {
        _listings.update { current ->
            current.map {
                if (it.id == listingId) {
                    val newStock = (it.stock - amount).coerceAtLeast(0)
                    it.copy(stock = newStock)
                } else it
            }.filter { it.stock > 0 }
        }
    }

    fun restore(savedListings: List<MarketListing>) {
        _listings.value = savedListings
    }
}
