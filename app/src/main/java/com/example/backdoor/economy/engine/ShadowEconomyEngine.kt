package com.example.backdoor.economy.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.backdoor.economy.models.*
import com.example.backdoor.core.SystemEventBus
import com.example.backdoor.core.SystemEvent
import org.json.JSONArray
import org.json.JSONObject

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
        contractManager.setEventBus(eventBus, scope)
        startAmbientSimulation()
    }

    private fun startAmbientSimulation() {
        scope.launch {
            populateInitialMarket()
            populateInitialNews()
            populateInitialContracts()

            while (true) {
                delay(60000)
                simulateEconomyTick()
            }
        }
    }

    private fun simulateEconomyTick() {
        if (Math.random() > 0.8) {
            val news = NewsArticle(
                title = "Corporate Network Audit Sweeps",
                content = "Global networks are undergoing automated perimeter security analysis.",
                category = NewsCategory.MARKET
            )
            newsService.publishArticle(news)
            eventBus.emit(SystemEvent.NotificationTriggered("News Update", news.title))
        }

        if (Math.random() > 0.6) {
            val generatedContract = generateDynamicContract()
            contractManager.addContract(generatedContract)
        }
    }

    private fun generateDynamicContract(): Contract {
        val id = System.currentTimeMillis() % 10000
        val categories = listOf("NETWORK", "SECURITY", "FILESYSTEM", "DARKNET", "ECONOMY")
        val cat = categories.random()

        return when (cat) {
            "NETWORK" -> Contract(
                title = "Network Recon: Subnet #$id",
                description = "Perform host discovery and port scan against dynamic target host.",
                type = ContractType.FREELANCE,
                difficulty = 2,
                requiredReputation = 5,
                rewardAmount = 1500 + (id % 1000),
                rewardCurrency = CurrencyType.CREDITS,
                issuer = "NetSec Syndicate",
                completionCriteria = "Discover and scan network nodes.",
                category = "NETWORK",
                objectives = listOf(
                    Objective(
                        objectiveType = ObjectiveType.SCAN_TARGET,
                        description = "Execute network scanner against target subnet",
                        targetParam = null
                    ),
                    Objective(
                        objectiveType = ObjectiveType.EXECUTE_COMMAND,
                        description = "Run 'ping' or 'traceroute' command to verify connection",
                        targetParam = "ping"
                    )
                )
            )

            "SECURITY" -> Contract(
                title = "Vulnerability Assessment #$id",
                description = "Scan corporate server perimeter and compile vulnerability assessment report.",
                type = ContractType.CORPORATE,
                difficulty = 3,
                requiredReputation = 10,
                rewardAmount = 3000 + (id % 2000),
                rewardCurrency = CurrencyType.CREDITS,
                issuer = "Apex Risk Management",
                completionCriteria = "Identify open ports and compile report.",
                category = "SECURITY",
                objectives = listOf(
                    Objective(
                        objectiveType = ObjectiveType.ANALYZE_SECURITY,
                        description = "Run security assessment module against target",
                        targetParam = null
                    ),
                    Objective(
                        objectiveType = ObjectiveType.CREATE_REPORT,
                        description = "Save generated security report to disk",
                        targetParam = null
                    )
                )
            )

            "FILESYSTEM" -> Contract(
                title = "Data Retrieval Contract #$id",
                description = "Retrieve or manage target system documents and clean logs.",
                type = ContractType.PUBLIC,
                difficulty = 2,
                requiredReputation = 0,
                rewardAmount = 1200 + (id % 800),
                rewardCurrency = CurrencyType.CREDITS,
                issuer = "Anonymous Operator",
                completionCriteria = "Manage target filesystem node.",
                category = "FILESYSTEM",
                objectives = listOf(
                    Objective(
                        objectiveType = ObjectiveType.EXECUTE_COMMAND,
                        description = "Use 'find' command to search system files",
                        targetParam = "find"
                    ),
                    Objective(
                        objectiveType = ObjectiveType.DOWNLOAD_FILE,
                        description = "Store target payload or report file in home directory",
                        targetParam = null
                    )
                )
            )

            "DARKNET" -> Contract(
                title = "DarkNet Onion Recon #$id",
                description = "Establish secure onion circuit and participate in forum discussion.",
                type = ContractType.DARKNET,
                difficulty = 3,
                requiredReputation = 15,
                rewardAmount = 2500 + (id % 1500),
                rewardCurrency = CurrencyType.CREDITS,
                issuer = "v0id_walker",
                completionCriteria = "Connect to onion site and publish forum entry.",
                category = "DARKNET",
                objectives = listOf(
                    Objective(
                        objectiveType = ObjectiveType.VISIT_HIDDEN_SERVICE,
                        description = "Connect to an active .onion hidden service",
                        targetParam = ".onion"
                    ),
                    Objective(
                        objectiveType = ObjectiveType.CREATE_FORUM_POST,
                        description = "Post thread or reply in underground cyber forum",
                        targetParam = null
                    )
                )
            )

            else -> Contract(
                title = "Shadow Market Procurement #$id",
                description = "Acquire assets or execute transactions across the Shadow Economy.",
                type = ContractType.FREELANCE,
                difficulty = 1,
                requiredReputation = 0,
                rewardAmount = 800 + (id % 500),
                rewardCurrency = CurrencyType.CREDITS,
                issuer = "Shadow Board",
                completionCriteria = "Execute transaction or item purchase.",
                category = "ECONOMY",
                objectives = listOf(
                    Objective(
                        objectiveType = ObjectiveType.PURCHASE_ITEM,
                        description = "Purchase item from Marketplace or Dark Market",
                        targetParam = null
                    ),
                    Objective(
                        objectiveType = ObjectiveType.PAYMENT_SENT,
                        description = "Execute payment transfer over Wallet",
                        targetParam = null
                    )
                )
            )
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
            title = "AbyssOS 1.2.0 Abyss Contracts Engine Deployed",
            content = "The new validated objective-driven contract framework is active across the corporate grid.",
            category = NewsCategory.TECHNOLOGY
        ))
    }

    private fun populateInitialContracts() {
        val welcomeContract = Contract(
            title = "Grid Operator Initialization",
            description = "Welcome to AbyssOS. Execute basic system verification commands to fulfill contract.",
            type = ContractType.PUBLIC,
            difficulty = 1,
            requiredReputation = 0,
            rewardAmount = 500,
            rewardCurrency = CurrencyType.CREDITS,
            issuer = "System Security",
            completionCriteria = "Execute terminal commands.",
            category = "ONBOARDING",
            objectives = listOf(
                Objective(
                    objectiveType = ObjectiveType.EXECUTE_COMMAND,
                    description = "Execute 'ls' command to view file system",
                    targetParam = "ls"
                ),
                Objective(
                    objectiveType = ObjectiveType.EXECUTE_COMMAND,
                    description = "Execute 'whoami' to verify user identity",
                    targetParam = "whoami"
                )
            )
        )

        val corpAudit = Contract(
            title = "Corporate Security Audit: Aegis Defense",
            description = "Perform perimeter scanning and compile security assessment report.",
            type = ContractType.CORPORATE,
            difficulty = 3,
            requiredReputation = 5,
            rewardAmount = 3500,
            rewardCurrency = CurrencyType.CREDITS,
            issuer = "Apex Global Risk",
            completionCriteria = "Discover services and compile security report.",
            category = "SECURITY",
            objectives = listOf(
                Objective(
                    objectiveType = ObjectiveType.SCAN_TARGET,
                    description = "Scan target host aegis-defense.net or subnet",
                    targetParam = "aegis"
                ),
                Objective(
                    objectiveType = ObjectiveType.ANALYZE_SECURITY,
                    description = "Run security assessment module on target",
                    targetParam = "aegis"
                ),
                Objective(
                    objectiveType = ObjectiveType.CREATE_REPORT,
                    description = "Generate and save security report",
                    targetParam = "aegis"
                )
            )
        )

        val darknetRecon = Contract(
            title = "DarkNet Intelligence Gathering",
            description = "Connect to underground onion forum and publish cyber security post.",
            type = ContractType.DARKNET,
            difficulty = 2,
            requiredReputation = 5,
            rewardAmount = 2200,
            rewardCurrency = CurrencyType.CREDITS,
            issuer = "v0id_walker",
            completionCriteria = "Visit darknet forum and post entry.",
            category = "DARKNET",
            objectives = listOf(
                Objective(
                    objectiveType = ObjectiveType.VISIT_HIDDEN_SERVICE,
                    description = "Establish connection to abyss-forum.onion",
                    targetParam = "abyss-forum.onion"
                ),
                Objective(
                    objectiveType = ObjectiveType.CREATE_FORUM_POST,
                    description = "Publish thread or reply in underground forum",
                    targetParam = null
                )
            )
        )

        contractManager.addContract(welcomeContract)
        contractManager.addContract(corpAudit)
        contractManager.addContract(darknetRecon)
    }

    fun serializeToJson(): String {
        val root = JSONObject()
        
        // Wallet balances
        val balancesObj = JSONObject()
        val currentBalances = walletManager.balances.value
        for ((currency, bal) in currentBalances) {
            balancesObj.put(currency.name, bal)
        }
        root.put("balances", balancesObj)
        
        // Wallet transactions
        val txsArr = JSONArray()
        val currentTxs = walletManager.transactions.value
        for (tx in currentTxs) {
            val txObj = JSONObject()
            txObj.put("id", tx.id)
            txObj.put("timestamp", tx.timestamp)
            txObj.put("amount", tx.amount)
            txObj.put("currency", tx.currency.name)
            txObj.put("description", tx.description)
            txObj.put("isIncoming", tx.isIncoming)
            txObj.put("status", tx.status.name)
            txsArr.put(txObj)
        }
        root.put("transactions", txsArr)

        // Contracts & Objectives
        root.put("contracts", contractManager.serializeToJsonArray())
        
        return root.toString()
    }

    fun deserializeFromJson(json: String) {
        try {
            val root = JSONObject(json)
            
            // Wallet balances
            val balances = mutableMapOf<CurrencyType, Long>()
            if (root.has("balances")) {
                val bObj = root.getJSONObject("balances")
                for (key in bObj.keys()) {
                    try {
                        val currency = CurrencyType.valueOf(key)
                        val bal = bObj.getLong(key)
                        balances[currency] = bal
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            // Wallet transactions
            val txs = mutableListOf<Transaction>()
            if (root.has("transactions")) {
                val txsArr = root.getJSONArray("transactions")
                for (i in 0 until txsArr.length()) {
                    val txObj = txsArr.getJSONObject(i)
                    txs.add(Transaction(
                        id = txObj.getString("id"),
                        timestamp = txObj.getLong("timestamp"),
                        amount = txObj.getLong("amount"),
                        currency = CurrencyType.valueOf(txObj.getString("currency")),
                        description = txObj.getString("description"),
                        isIncoming = txObj.getBoolean("isIncoming"),
                        status = TransactionStatus.valueOf(txObj.getString("status"))
                    ))
                }
            }
            
            walletManager.restore(balances, txs)

            // Restore contracts
            if (root.has("contracts")) {
                val contractsArr = root.getJSONArray("contracts")
                contractManager.deserializeFromJsonArray(contractsArr)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
