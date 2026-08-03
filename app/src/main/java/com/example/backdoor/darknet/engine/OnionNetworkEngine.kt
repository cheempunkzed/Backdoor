package com.example.backdoor.darknet.engine

import com.example.backdoor.darknet.model.AccessLevel
import com.example.backdoor.darknet.model.AnonymousIdentity
import com.example.backdoor.darknet.model.CommunityRank
import com.example.backdoor.darknet.model.DarkMarketListing
import com.example.backdoor.darknet.model.DigitalIdentity
import com.example.backdoor.darknet.model.EncryptedMessage
import com.example.backdoor.darknet.model.Faction
import com.example.backdoor.darknet.model.FactionStanding
import com.example.backdoor.darknet.model.ForumPost
import com.example.backdoor.darknet.model.ForumThread
import com.example.backdoor.darknet.model.HiddenService
import com.example.backdoor.darknet.model.HiddenServiceType
import com.example.backdoor.darknet.model.RelayNode
import com.example.backdoor.darknet.model.UserReputation
import com.example.backdoor.security.core.DarkNetRoutingHook
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class OnionNetworkEngine : DarkNetRoutingHook {

    private val _relayNodes = MutableStateFlow<List<RelayNode>>(emptyList())
    val relayNodes: StateFlow<List<RelayNode>> = _relayNodes.asStateFlow()

    private val _hiddenServices = MutableStateFlow<List<HiddenService>>(emptyList())
    val hiddenServices: StateFlow<List<HiddenService>> = _hiddenServices.asStateFlow()

    private val _npcIdentities = MutableStateFlow<List<AnonymousIdentity>>(emptyList())
    val npcIdentities: StateFlow<List<AnonymousIdentity>> = _npcIdentities.asStateFlow()

    private val _forumThreads = MutableStateFlow<List<ForumThread>>(emptyList())
    val forumThreads: StateFlow<List<ForumThread>> = _forumThreads.asStateFlow()

    private val _marketListings = MutableStateFlow<List<DarkMarketListing>>(emptyList())
    val marketListings: StateFlow<List<DarkMarketListing>> = _marketListings.asStateFlow()

    private val _playerReputation = MutableStateFlow(UserReputation(trust = 150, fame = 60, suspicion = 10, rank = CommunityRank.NOVICE))
    val playerReputation: StateFlow<UserReputation> = _playerReputation.asStateFlow()

    private val _discoveredAddresses = MutableStateFlow<Set<String>>(
        setOf("dir.onion", "abyss-forum.onion", "blackvault.onion", "cipherroom.onion", "whistleblower.onion", "darkmarket.onion", "shadowblog.onion", "zero-day.onion")
    )
    val discoveredAddresses: StateFlow<Set<String>> = _discoveredAddresses.asStateFlow()

    // Milestone 1.4.0 Expansions: Digital Identities, Factions, Encrypted PMs, Rumors
    private val defaultPrimaryIdentity = DigitalIdentity(
        nickname = "operator",
        pgpFingerprint = "9F81-4A12-B08C-33E1-77AA",
        reputation = 250,
        trustScore = 300,
        criminalHeat = 5,
        hiddenProfile = "Primary Root Node Cipher Operator",
        isPrimary = true
    )

    private val _identities = MutableStateFlow<List<DigitalIdentity>>(listOf(defaultPrimaryIdentity))
    val identities: StateFlow<List<DigitalIdentity>> = _identities.asStateFlow()

    private val _activeIdentity = MutableStateFlow<DigitalIdentity>(defaultPrimaryIdentity)
    val activeIdentity: StateFlow<DigitalIdentity> = _activeIdentity.asStateFlow()

    private val _factions = MutableStateFlow<List<Faction>>(emptyList())
    val factions: StateFlow<List<Faction>> = _factions.asStateFlow()

    private val _factionReputation = MutableStateFlow<Map<String, Int>>(
        mapOf(
            "cipher_collective" to 120,
            "ghost_protocol" to 50,
            "null_division" to 10,
            "black_market_syndicate" to 80,
            "white_hat_alliance" to 30,
            "anonymous_researchers" to 150,
            "data_brokers" to 40
        )
    )
    val factionReputation: StateFlow<Map<String, Int>> = _factionReputation.asStateFlow()

    private val _encryptedMessages = MutableStateFlow<List<EncryptedMessage>>(emptyList())
    val encryptedMessages: StateFlow<List<EncryptedMessage>> = _encryptedMessages.asStateFlow()

    val rumorEngine = RumorEngine()

    private var isCircuitActive: Boolean = true

    init {
        initializeRelayNodes()
        initializeHiddenServices()
        initializeNPCs()
        initializeFactions()
        initializeForums()
        initializeMarketListings()
        initializeDefaultMessages()
    }

    override fun isEncryptedOnionRouteActive(): Boolean = isCircuitActive

    override fun getHiddenServiceDescriptors(): List<String> = _hiddenServices.value.map { "${it.address} [${it.type.displayName}]" }

    private fun initializeRelayNodes() {
        _relayNodes.value = listOf(
            RelayNode("node-01", "185.220.101.5", "OnionRelay-Alpha", "Iceland", 1000, 99.8f),
            RelayNode("node-02", "199.249.230.8", "OnionRelay-Bravo", "Switzerland", 750, 99.5f),
            RelayNode("node-03", "185.100.87.210", "OnionRelay-Charlie", "Sweden", 1200, 99.9f),
            RelayNode("node-04", "109.70.100.15", "ExitNode-Delta", "Netherlands", 500, 98.9f, isExitNode = true),
            RelayNode("node-05", "51.15.42.99", "ExitNode-Echo", "France", 850, 99.2f, isExitNode = true)
        )
    }

    private fun initializeHiddenServices() {
        _hiddenServices.value = listOf(
            HiddenService(
                address = "abyss-forum.onion",
                name = "Abyss Underground Cyber Forum",
                type = HiddenServiceType.FORUM,
                accessLevel = AccessLevel.PUBLIC,
                description = "Primary node discussion board for zero-days, corporate network analysis, and hardware specs.",
                ownerHandle = "v0id_walker"
            ),
            HiddenService(
                address = "blackvault.onion",
                name = "BlackVault Classified Leak Repository",
                type = HiddenServiceType.ARCHIVE,
                accessLevel = AccessLevel.PUBLIC,
                description = "Decentralized storage for leaked corporate credentials, network topology blueprints, and firmware dumps.",
                ownerHandle = "cypher_ghost"
            ),
            HiddenService(
                address = "cipherroom.onion",
                name = "CipherRoom Encrypted Relay Chat",
                type = HiddenServiceType.CHAT_ROOM,
                accessLevel = AccessLevel.REGISTERED,
                description = "Multi-hop encrypted chat rooms for security researchers and rogue network engineers.",
                ownerHandle = "shadow_weaver"
            ),
            HiddenService(
                address = "whistleblower.onion",
                name = "Aegis & Apex Whistleblower Portal",
                type = HiddenServiceType.WHISTLEBLOWER,
                accessLevel = AccessLevel.PUBLIC,
                description = "Secure upload portal for internal corporate memos, financial ledger dumps, and security audits.",
                ownerHandle = "root_daemon"
            ),
            HiddenService(
                address = "darkmarket.onion",
                name = "Shadow Exchange Market Foundation",
                type = HiddenServiceType.MARKETPLACE,
                accessLevel = AccessLevel.REGISTERED,
                description = "Decentralized directory of specialized hardware tools, exploit schematics, and zero-day advisories.",
                ownerHandle = "k3rnel_panic"
            ),
            HiddenService(
                address = "shadowblog.onion",
                name = "The Underground Synthesizer",
                type = HiddenServiceType.BLOG,
                accessLevel = AccessLevel.PUBLIC,
                description = "Technical write-ups on kernel memory manipulation, router firmware reverse engineering, and onion routing.",
                ownerHandle = "v0id_walker"
            ),
            HiddenService(
                address = "zero-day.onion",
                name = "Elite Zero-Day Research Cell",
                type = HiddenServiceType.RESEARCH_GROUP,
                accessLevel = AccessLevel.TRUSTED,
                description = "Closed research group analyzing patch vulnerabilities and corporate grid router backdoors.",
                ownerHandle = "cypher_ghost"
            )
        )
    }

    private fun initializeNPCs() {
        _npcIdentities.value = listOf(
            AnonymousIdentity("npc-1", "v0id_walker", "Philosophical Cypherpunk", "Kernel Architecture & Onion Protocols", UserReputation(4500, 3200, 50, CommunityRank.SHADOW_ADMIN), listOf("Abyss Forum Mod", "Cipher Collective")),
            AnonymousIdentity("npc-2", "cypher_ghost", "Rogue Security Auditor", "Zero-Day Exploits & Reverse Engineering", UserReputation(3800, 2900, 120, CommunityRank.CYPHERPUNK), listOf("Ghost Protocol", "BlackVault Curator")),
            AnonymousIdentity("npc-3", "k3rnel_panic", "Hardware Tinkerer & Trader", "Router Firmware & FPGA Systems", UserReputation(2900, 1800, 40, CommunityRank.OPERATOR), listOf("Black Market Syndicate")),
            AnonymousIdentity("npc-4", "shadow_weaver", "Cryptographer", "Multi-hop Onion Encryption & RSA Keys", UserReputation(3100, 2100, 30, CommunityRank.CYPHERPUNK), listOf("Cipher Collective")),
            AnonymousIdentity("npc-5", "root_daemon", "Ex-Aegis Corporate Insider", "Data Center Architecture & Corporate Leaks", UserReputation(5200, 4100, 250, CommunityRank.SHADOW_ADMIN), listOf("Null Division", "Whistleblower Portal"))
        )
    }

    private fun initializeFactions() {
        _factions.value = listOf(
            Faction(
                id = "cipher_collective",
                name = "Cipher Collective",
                tag = "[CIPHER]",
                ideology = "Total encryption, open protocols, and mathematical anonymity.",
                trustRequirement = 100,
                enemies = listOf("data_brokers"),
                allies = listOf("anonymous_researchers", "ghost_protocol"),
                hiddenForums = listOf("cipherroom.onion", "zero-day.onion"),
                description = "Pioneer group developing multi-hop onion cryptography and anti-surveillance frameworks."
            ),
            Faction(
                id = "ghost_protocol",
                name = "Ghost Protocol",
                tag = "[GHOST]",
                ideology = "Zero-footprint infiltration and stealth system exploitation.",
                trustRequirement = 200,
                enemies = listOf("white_hat_alliance"),
                allies = listOf("cipher_collective", "null_division"),
                hiddenForums = listOf("blackvault.onion"),
                description = "Elusive operative syndicate executing high-tier corporate network penetrations."
            ),
            Faction(
                id = "null_division",
                name = "Null Division",
                tag = "[NULL]",
                ideology = "Disruption of corporate data monopolies and infrastructure corruption.",
                trustRequirement = 300,
                enemies = listOf("white_hat_alliance", "data_brokers"),
                allies = listOf("ghost_protocol"),
                hiddenForums = listOf("whistleblower.onion"),
                description = "Radical hacktivist organization dedicated to wiping corporate financial ledgers."
            ),
            Faction(
                id = "black_market_syndicate",
                name = "Black Market Syndicate",
                tag = "[BMS]",
                ideology = "Unrestricted darknet commerce, zero-day monetization, and trade freedom.",
                trustRequirement = 50,
                enemies = listOf("white_hat_alliance"),
                allies = listOf("data_brokers"),
                hiddenForums = listOf("darkmarket.onion"),
                description = "Global broker network trading exploits, credentials, and specialized hacking hardware."
            ),
            Faction(
                id = "white_hat_alliance",
                name = "White Hat Alliance",
                tag = "[WHA]",
                ideology = "Responsible disclosure, patch engineering, and infrastructure defense.",
                trustRequirement = 150,
                enemies = listOf("ghost_protocol", "null_division", "black_market_syndicate"),
                allies = listOf("anonymous_researchers"),
                hiddenForums = listOf("abyss-forum.onion"),
                description = "Defensive security coalition auditing grid vulnerabilities before exploit deployment."
            ),
            Faction(
                id = "anonymous_researchers",
                name = "Anonymous Researchers",
                tag = "[ANON]",
                ideology = "Free flow of security intelligence and public education.",
                trustRequirement = 0,
                enemies = emptyList(),
                allies = listOf("cipher_collective", "white_hat_alliance"),
                hiddenForums = listOf("shadowblog.onion", "abyss-forum.onion"),
                description = "Open security research collective publishing vulnerability write-ups and guides."
            ),
            Faction(
                id = "data_brokers",
                name = "Data Brokers",
                tag = "[BROKER]",
                ideology = "Information arbitrage, corporate credential sales, and profit-first intelligence.",
                trustRequirement = 100,
                enemies = listOf("cipher_collective", "null_division"),
                allies = listOf("black_market_syndicate"),
                hiddenForums = listOf("darkmarket.onion"),
                description = "Underground data syndicates selling corporate employee identities and SSH keys."
            )
        )
    }

    private fun initializeForums() {
        val threads = mutableListOf<ForumThread>()

        val thread1 = ForumThread(
            id = "thread-1",
            title = "AbyssOS 0.9.0 Onion Layer Architecture & Relay Circuit Analysis",
            authorHandle = "v0id_walker",
            category = "Network Protocols",
            requiredAccess = AccessLevel.PUBLIC,
            posts = mutableListOf(
                ForumPost("p-1", "v0id_walker", "Welcome operators to the Dark Layer. AbyssNet now features a multi-hop onion routing network. Always check your relay exit nodes before initiating security sweeps.", isPinned = true),
                ForumPost("p-2", "cypher_ghost", "Agreed. Aegis Financial has upgraded their perimeter IDS. Running raw scans over clearnet will flag your node immediately. Route everything through onion services."),
                ForumPost("p-3", "shadow_weaver", "CipherRoom chat relay nodes have been upgraded to 4096-bit RSA keys. Verify fingerprints before handshakes.")
            )
        )

        val thread2 = ForumThread(
            id = "thread-2",
            title = "Aegis Corporation Data Center Router Backdoors Discovered",
            authorHandle = "root_daemon",
            category = "Corporate Leaks",
            requiredAccess = AccessLevel.PUBLIC,
            posts = mutableListOf(
                ForumPost("p-4", "root_daemon", "I have uploaded full data center router schematics to blackvault.onion. Check out section 4: default maintenance port 27017 banner contains default admin hash."),
                ForumPost("p-5", "k3rnel_panic", "Confirmed. Tested against Aegis secondary gateway node. Port diagnostic returns open banner.")
            )
        )

        val thread3 = ForumThread(
            id = "thread-3",
            title = "How to Increase Reputation & Access Elite Onion Service Circles",
            authorHandle = "cypher_ghost",
            category = "Guides & Tutorials",
            requiredAccess = AccessLevel.PUBLIC,
            posts = mutableListOf(
                ForumPost("p-6", "cypher_ghost", "Reputation is earned by contributing security audit reports, discovering hidden service addresses, and participating in forum discussions. Higher Trust rank unlocks Trusted and Elite onion portals.")
            )
        )

        threads.add(thread1)
        threads.add(thread2)
        threads.add(thread3)
        _forumThreads.value = threads
    }

    private fun initializeMarketListings() {
        _marketListings.value = listOf(
            DarkMarketListing("item-1", "Custom Router Firmware Flashing Kit v4", "k3rnel_panic", "Hardware Schematics", "Enables custom packet injection and high-speed port scanning across corporate grid nodes.", 450.0),
            DarkMarketListing("item-2", "Apex Financial Internal Subnet Map [2026]", "root_daemon", "Corporate Leaks", "Complete map of Apex Financial Tier IV data centers, rack IDs, and active SSH gateways.", 1200.0),
            DarkMarketListing("item-3", "Multi-Hop Encryption Protocol Spec", "shadow_weaver", "Encryption Tools", "Specifications for implementing custom onion relay nodes with zero trace signature.", 800.0),
            DarkMarketListing("item-4", "Aegis Corp SSH Master Credential Bundle", "root_daemon", "VPN Credentials", "Verified SSH identity key pair for Aegis internal gateway maintenance host.", 2500.0),
            DarkMarketListing("item-5", "Zero-Day Kernel Memory Injector", "cypher_ghost", "Zero-Day Research", "Exploit payload bypassing memory protection on Linux 6.x kernels.", 3800.0)
        )
    }

    private fun initializeDefaultMessages() {
        _encryptedMessages.value = listOf(
            EncryptedMessage(
                senderHandle = "v0id_walker",
                recipientHandle = _activeIdentity.value.nickname,
                subject = "Welcome to the Abyss Underground Network",
                body = "Operator,\nYour node PGP key has been verified on the Dark Layer. Keep your identity clean and manage your heat level carefully.\n- v0id_walker",
                isRead = false
            ),
            EncryptedMessage(
                senderHandle = "root_daemon",
                recipientHandle = _activeIdentity.value.nickname,
                subject = "Encrypted Archive Access - BlackVault Leak",
                body = "I noticed your handle on the forums. If you need corporate gateway credentials for Aegis, check out darkmarket.onion or my thread on blackvault.onion.",
                isRead = false
            )
        )
    }

    // Digital Identity Operations
    fun createIdentity(nickname: String, hiddenProfile: String): DigitalIdentity {
        val newIdent = DigitalIdentity(
            nickname = nickname,
            hiddenProfile = hiddenProfile,
            isPrimary = false
        )
        _identities.value = _identities.value + newIdent
        return newIdent
    }

    fun switchIdentity(idOrNickname: String): Boolean {
        val match = _identities.value.find { it.id == idOrNickname || it.nickname.equals(idOrNickname, ignoreCase = true) }
        if (match != null) {
            _activeIdentity.value = match
            return true
        }
        return false
    }

    // Faction Operations
    fun getFactionStanding(factionId: String): FactionStanding {
        val rep = _factionReputation.value[factionId] ?: 0
        val tier = when {
            rep >= 500 -> "Elite"
            rep >= 200 -> "Trusted"
            rep >= 50 -> "Member"
            rep >= -20 -> "Neutral"
            else -> "Hostile"
        }
        return FactionStanding(factionId = factionId, reputation = rep, trustTier = tier)
    }

    fun addFactionReputation(factionId: String, delta: Int) {
        val current = _factionReputation.value.toMutableMap()
        val old = current[factionId] ?: 0
        current[factionId] = (old + delta).coerceIn(-100, 1000)
        _factionReputation.value = current
    }

    // Encrypted Messaging Operations
    fun sendPrivateMessage(recipientHandle: String, subject: String, body: String, attachment: String? = null): EncryptedMessage {
        val msg = EncryptedMessage(
            senderHandle = _activeIdentity.value.nickname,
            recipientHandle = recipientHandle,
            subject = subject,
            body = body,
            attachmentItemOrKey = attachment,
            pgpSignature = _activeIdentity.value.pgpFingerprint,
            isRead = true
        )
        _encryptedMessages.value = listOf(msg) + _encryptedMessages.value
        addPlayerReputation(trustGain = 5, fameGain = 5)
        return msg
    }

    fun receiveMessageFromNPC(senderHandle: String, subject: String, body: String, attachment: String? = null) {
        val msg = EncryptedMessage(
            senderHandle = senderHandle,
            recipientHandle = _activeIdentity.value.nickname,
            subject = subject,
            body = body,
            attachmentItemOrKey = attachment,
            isRead = false
        )
        _encryptedMessages.value = listOf(msg) + _encryptedMessages.value
    }

    fun markMessageAsRead(messageId: String) {
        _encryptedMessages.value = _encryptedMessages.value.map {
            if (it.id == messageId) it.copy(isRead = true) else it
        }
    }

    // Market Purchase Action
    fun buyMarketItem(listingId: String): Pair<Boolean, String> {
        val list = _marketListings.value.toMutableList()
        val index = list.indexOfFirst { it.id == listingId }
        if (index == -1) return Pair(false, "Item not found in Shadow Exchange.")
        val item = list[index]
        if (!item.available) return Pair(false, "Listing is currently out of stock.")

        // Mark item purchased/unavailable
        list[index] = item.copy(available = false)
        _marketListings.value = list
        addPlayerReputation(trustGain = 10, fameGain = 15)
        addFactionReputation("black_market_syndicate", 15)

        return Pair(true, "Purchased '${item.title}' from seller @${item.sellerHandle}. Item key delivered to encrypted storage.")
    }

    // Simulation Tick for NPCs and Rumors
    fun tickSimulation() {
        // 1. Generate rumor periodically
        if ((0..100).random() < 25) {
            val rumor = rumorEngine.generateRandomRumor()
            // 2. Post thread based on rumor
            postReplyToThread(
                threadId = _forumThreads.value.random().id,
                content = "[RUMOR ALERT] ${rumor.title}: ${rumor.description}",
                authorHandle = rumor.sourceHandle
            )
        }
    }

    fun discoverAddress(address: String) {
        if (!_discoveredAddresses.value.contains(address)) {
            _discoveredAddresses.value = _discoveredAddresses.value + address
            addPlayerReputation(trustGain = 15, fameGain = 10)
        }
    }

    fun postReplyToThread(threadId: String, content: String, authorHandle: String = _activeIdentity.value.nickname): ForumPost? {
        val thread = _forumThreads.value.find { it.id == threadId } ?: return null
        val newPost = ForumPost(
            id = "p-${System.currentTimeMillis() % 100000}",
            authorHandle = authorHandle,
            content = content
        )
        thread.posts.add(newPost)
        _forumThreads.value = _forumThreads.value.toList() // Trigger recomposition
        addPlayerReputation(trustGain = 20, fameGain = 15)
        return newPost
    }

    fun createNewThread(title: String, category: String, content: String, authorHandle: String = _activeIdentity.value.nickname): ForumThread {
        val newThread = ForumThread(
            id = "thread-${System.currentTimeMillis() % 100000}",
            title = title,
            authorHandle = authorHandle,
            category = category,
            posts = mutableListOf(
                ForumPost("p-${System.currentTimeMillis() % 100000}", authorHandle, content)
            )
        )
        _forumThreads.value = listOf(newThread) + _forumThreads.value
        addPlayerReputation(trustGain = 50, fameGain = 30)
        return newThread
    }

    fun addPlayerReputation(trustGain: Int = 0, fameGain: Int = 0, suspicionChange: Int = 0) {
        val current = _playerReputation.value
        val newTrust = (current.trust + trustGain).coerceAtLeast(0)
        val newFame = (current.fame + fameGain).coerceAtLeast(0)
        val newSuspicion = (current.suspicion + suspicionChange).coerceAtLeast(0)

        val totalScore = (newTrust * 1.5 + newFame * 0.8 - newSuspicion * 2.0).toInt()
        val newRank = when {
            totalScore > 5000 -> CommunityRank.SHADOW_ADMIN
            totalScore > 2000 -> CommunityRank.CYPHERPUNK
            totalScore > 800 -> CommunityRank.OPERATOR
            totalScore > 300 -> CommunityRank.MEMEBER
            else -> CommunityRank.NOVICE
        }

        _playerReputation.value = UserReputation(newTrust, newFame, newSuspicion, newRank)
    }

    fun getHiddenServiceByAddress(address: String): HiddenService? {
        return _hiddenServices.value.find { it.address.equals(address, ignoreCase = true) }
    }

    /**
     * Serializes darknet ecosystem state for save manager.
     */
    fun toJson(): String {
        val root = JSONObject()

        val repObj = JSONObject()
        val rep = _playerReputation.value
        repObj.put("trust", rep.trust)
        repObj.put("fame", rep.fame)
        repObj.put("suspicion", rep.suspicion)
        repObj.put("rank", rep.rank.name)
        root.put("playerReputation", repObj)

        val discArray = JSONArray()
        _discoveredAddresses.value.forEach { discArray.put(it) }
        root.put("discoveredAddresses", discArray)

        val idArray = JSONArray()
        _identities.value.forEach { id ->
            val obj = JSONObject()
            obj.put("id", id.id)
            obj.put("nickname", id.nickname)
            obj.put("pgpFingerprint", id.pgpFingerprint)
            obj.put("reputation", id.reputation)
            obj.put("trustScore", id.trustScore)
            obj.put("criminalHeat", id.criminalHeat)
            obj.put("joinDate", id.joinDate)
            obj.put("hiddenProfile", id.hiddenProfile)
            obj.put("isPrimary", id.isPrimary)
            idArray.put(obj)
        }
        root.put("identities", idArray)
        root.put("activeIdentityId", _activeIdentity.value.id)

        val facObj = JSONObject()
        _factionReputation.value.forEach { (k, v) -> facObj.put(k, v) }
        root.put("factionReputation", facObj)

        val msgArray = JSONArray()
        _encryptedMessages.value.forEach { m ->
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("senderHandle", m.senderHandle)
            obj.put("recipientHandle", m.recipientHandle)
            obj.put("subject", m.subject)
            obj.put("body", m.body)
            obj.put("timestamp", m.timestamp)
            obj.put("isRead", m.isRead)
            obj.put("attachment", m.attachmentItemOrKey)
            msgArray.put(obj)
        }
        root.put("encryptedMessages", msgArray)

        root.put("rumorState", rumorEngine.toJson())

        return root.toString()
    }

    fun loadFromJson(json: String) {
        try {
            val root = JSONObject(json)
            if (root.has("playerReputation")) {
                val r = root.getJSONObject("playerReputation")
                _playerReputation.value = UserReputation(
                    trust = r.optInt("trust", 150),
                    fame = r.optInt("fame", 60),
                    suspicion = r.optInt("suspicion", 10),
                    rank = try { CommunityRank.valueOf(r.optString("rank", "NOVICE")) } catch (_: Exception) { CommunityRank.NOVICE }
                )
            }
            if (root.has("discoveredAddresses")) {
                val arr = root.getJSONArray("discoveredAddresses")
                val set = mutableSetOf<String>()
                for (i in 0 until arr.length()) {
                    set.add(arr.getString(i))
                }
                _discoveredAddresses.value = set
            }
            if (root.has("identities")) {
                val arr = root.getJSONArray("identities")
                val list = mutableListOf<DigitalIdentity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        DigitalIdentity(
                            id = obj.getString("id"),
                            nickname = obj.getString("nickname"),
                            pgpFingerprint = obj.optString("pgpFingerprint"),
                            reputation = obj.optInt("reputation", 100),
                            trustScore = obj.optInt("trustScore", 150),
                            criminalHeat = obj.optInt("criminalHeat", 0),
                            joinDate = obj.optLong("joinDate"),
                            hiddenProfile = obj.optString("hiddenProfile"),
                            isPrimary = obj.optBoolean("isPrimary", false)
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    _identities.value = list
                    val activeId = root.optString("activeIdentityId")
                    val found = list.find { it.id == activeId }
                    if (found != null) _activeIdentity.value = found
                }
            }
            if (root.has("factionReputation")) {
                val facObj = root.getJSONObject("factionReputation")
                val map = mutableMapOf<String, Int>()
                facObj.keys().forEach { k -> map[k] = facObj.getInt(k) }
                _factionReputation.value = map
            }
            if (root.has("encryptedMessages")) {
                val arr = root.getJSONArray("encryptedMessages")
                val list = mutableListOf<EncryptedMessage>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        EncryptedMessage(
                            id = obj.getString("id"),
                            senderHandle = obj.getString("senderHandle"),
                            recipientHandle = obj.getString("recipientHandle"),
                            subject = obj.getString("subject"),
                            body = obj.getString("body"),
                            timestamp = obj.optLong("timestamp"),
                            isRead = obj.optBoolean("isRead", false),
                            attachmentItemOrKey = obj.optString("attachment")
                        )
                    )
                }
                if (list.isNotEmpty()) _encryptedMessages.value = list
            }
            if (root.has("rumorState")) {
                rumorEngine.loadFromJson(root.getString("rumorState"))
            }
        } catch (_: Exception) {}
    }
}
