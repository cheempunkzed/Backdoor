package com.example.backdoor.economy.models

import java.util.UUID

data class Item(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val type: ItemType,
    val rarity: ItemRarity = ItemRarity.COMMON,
    val attributes: Map<String, String> = emptyMap()
)

enum class ItemType {
    HARDWARE, // Network devices, SSD, RAM
    LICENSE, // Access keys, certs
    DOCUMENT, // Classified docs, manuals
    COSMETIC, // Themes, wallpapers
    SOFTWARE // Exploits, tools
}

enum class ItemRarity {
    COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, CLASSIFIED
}
