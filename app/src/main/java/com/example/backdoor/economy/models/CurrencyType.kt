package com.example.backdoor.economy.models

enum class CurrencyType(val code: String, val displayName: String, val symbol: String) {
    CREDITS("CR", "Credits", "CR"),
    ABYSS_COIN("ABC", "AbyssCoin", "ABC"),
    CORPORATE_TOKENS("CT", "Corporate Tokens", "CT"),
    RESEARCH_TOKENS("RT", "Research Tokens", "RT")
}
