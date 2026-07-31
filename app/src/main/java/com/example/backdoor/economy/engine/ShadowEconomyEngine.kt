package com.example.backdoor.economy.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.backdoor.economy.models.*
import com.example.backdoor.core.SystemEventBus
import com.example.backdoor.core.SystemEvent

class ShadowEconomyEngine(
    private val scope: CoroutineScope,
    private val eventBus: SystemEventBus
) {
    val walletManager = WalletManager()
    val contractManager = ContractManager()
    val inventoryManager = InventoryManager()
    val marketManager = MarketManager()
    val mailService = MailService()
    val newsService = NewsService()

    init {
        startAmbientSimulation()
    }

    private fun startAmbientSimulation() {
        scope.launch {
            // Populate initial market listings
            populateInitialMarket()
            populateInitialNews()
            populateInitialContracts()

            while (true) {
                delay(60000) // Every minute simulate some market/news changes
                simulateEconomyTick()
            }
        }
    }

    private fun simulateEconomyTick() {
        // Randomly adjust prices, add/remove listings, publish news
        if (Math.random() > 0.8) {
            val news = NewsArticle(
                title = "Market Fluctuation Detected",
                content = "Global markets are experiencing minor shifts due to recent corporate audits.",
                category = NewsCategory.MARKET
            )
            newsService.publishArticle(news)
            eventBus.emit(SystemEvent.NotificationTriggered("News Update", news.title))
        }

        if (Math.random() > 0.7) {
            val newContract = Contract(
                title = "Routine Audit",
                description = "Perform a routine security audit on public server.",
                type = ContractType.FREELANCE,
                difficulty = 2,
                requiredReputation = 10,
                rewardAmount = 250,
                rewardCurrency = CurrencyType.CREDITS,
                issuer = "Freelance Board",
                completionCriteria = "Submit audit log."
            )
            contractManager.addContract(newContract)
        }
    }

    private fun populateInitialMarket() {
        marketManager.addListing(MarketListing(
            item = Item(name = "1TB Encrypted SSD", description = "High-speed encrypted storage.", type = ItemType.HARDWARE, rarity = ItemRarity.UNCOMMON),
            price = 500,
            currency = CurrencyType.CREDITS,
            sellerId = "Hardware_Hub",
            sellerReputation = 80,
            stock = 10
        ))
        marketManager.addListing(MarketListing(
            item = Item(name = "Zero-Day Exploit: Apache", description = "Highly classified zero-day.", type = ItemType.SOFTWARE, rarity = ItemRarity.LEGENDARY),
            price = 5000,
            currency = CurrencyType.ABYSS_COIN,
            sellerId = "Anon_0x99",
            sellerReputation = 95,
            stock = 1,
            isDarkMarket = true
        ))
    }

    private fun populateInitialNews() {
        newsService.publishArticle(NewsArticle(
            title = "AbyssOS 0.9.0 Released",
            content = "The new Shadow Economy system is now fully integrated into the corporate grid.",
            category = NewsCategory.TECHNOLOGY
        ))
    }

    private fun populateInitialContracts() {
        contractManager.addContract(Contract(
            title = "Welcome to the Grid",
            description = "Setup your wallet and access the marketplace.",
            type = ContractType.PUBLIC,
            difficulty = 1,
            requiredReputation = 0,
            rewardAmount = 100,
            rewardCurrency = CurrencyType.CREDITS,
            issuer = "System",
            completionCriteria = "Auto-complete"
        ))
    }
}
