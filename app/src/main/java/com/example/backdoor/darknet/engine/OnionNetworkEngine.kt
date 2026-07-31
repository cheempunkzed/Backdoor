package com.example.backdoor.darknet.engine

import com.example.backdoor.darknet.model.AccessLevel
import com.example.backdoor.darknet.model.AnonymousIdentity
import com.example.backdoor.darknet.model.CommunityRank
import com.example.backdoor.darknet.model.DarkMarketListing
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
        setOf("dir.onion", "abyss-forum.onion", "blackvault.onion", "cipherroom.onion", "whistleblower.onion", "darkmarket.onion")
    )
    val discoveredAddresses: StateFlow<Set<String>> = _discoveredAddresses.asStateFlow()

    private var isCircuitActive: Boolean = true

    init {
        initializeRelayNodes()
        initializeHiddenServices()
        initializeNPCs()
        initializeForums()
        initializeMarketListings()
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
            AnonymousIdentity("npc-1", "v0id_walker", "Philosophical Cypherpunk", "Kernel Architecture & Onion Protocols", UserReputation(4500, 3200, 50, CommunityRank.SHADOW_ADMIN), listOf("Abyss Forum Mod", "Cypher Cell")),
            AnonymousIdentity("npc-2", "cypher_ghost", "Rogue Security Auditor", "Zero-Day Exploits & Reverse Engineering", UserReputation(3800, 2900, 120, CommunityRank.CYPHERPUNK), listOf("BlackVault Curator")),
            AnonymousIdentity("npc-3", "k3rnel_panic", "Hardware Tinkerer & Trader", "Router Firmware & FPGA Systems", UserReputation(2900, 1800, 40, CommunityRank.OPERATOR), listOf("Shadow Exchange")),
            AnonymousIdentity("npc-4", "shadow_weaver", "Cryptographer", "Multi-hop Onion Encryption & RSA Keys", UserReputation(3100, 2100, 30, CommunityRank.CYPHERPUNK), listOf("CipherRoom")),
            AnonymousIdentity("npc-5", "root_daemon", "Ex-Aegis Corporate Insider", "Data Center Architecture & Corporate Leaks", UserReputation(5200, 4100, 250, CommunityRank.SHADOW_ADMIN), listOf("Whistleblower Portal"))
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
            DarkMarketListing("item-3", "Multi-Hop Encryption Protocol Spec", "shadow_weaver", "Encryption Tools", "Specifications for implementing custom onion relay nodes with zero trace signature.", 800.0)
        )
    }

    fun discoverAddress(address: String) {
        if (!_discoveredAddresses.value.contains(address)) {
            _discoveredAddresses.value = _discoveredAddresses.value + address
            addPlayerReputation(trustGain = 15, fameGain = 10)
        }
    }

    fun postReplyToThread(threadId: String, content: String, authorHandle: String = "operator"): ForumPost? {
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

    fun createNewThread(title: String, category: String, content: String, authorHandle: String = "operator"): ForumThread {
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
     * Serializes darknet state for save manager.
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
        } catch (_: Exception) {}
    }
}
