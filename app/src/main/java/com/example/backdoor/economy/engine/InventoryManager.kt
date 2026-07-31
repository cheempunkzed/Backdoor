package com.example.backdoor.economy.engine

import com.example.backdoor.economy.models.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InventoryManager {
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    fun addItem(item: Item) {
        _items.update { it + item }
    }

    fun removeItem(itemId: String) {
        _items.update { it.filterNot { i -> i.id == itemId } }
    }

    fun hasItem(itemId: String): Boolean {
        return _items.value.any { it.id == itemId }
    }

    fun restore(savedItems: List<Item>) {
        _items.value = savedItems
    }
}
