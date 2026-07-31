package com.example.backdoor.darknet.model

enum class AccessLevel(val displayName: String, val requiredReputation: Int) {
    PUBLIC("Public Access", 0),
    REGISTERED("Registered Member", 100),
    TRUSTED("Trusted Operator", 500),
    PRIVATE("Private Circle", 1500),
    ELITE("Elite Council", 5000)
}

enum class HiddenServiceType(val displayName: String, val categoryIcon: String) {
    FORUM("Discussion Forum", "💬"),
    MARKETPLACE("Dark Market Foundation", "🛒"),
    ARCHIVE("Encrypted Archive", "📁"),
    BLOG("Shadow Blog", "✍️"),
    CHAT_ROOM("Encrypted Relay Chat", "🗨️"),
    RESEARCH_GROUP("Cyber Security Cell", "🔬"),
    WHISTLEBLOWER("Leaks & Whistleblower Portal", "🔓"),
    PRIVATE_NETWORK("Hidden Node Grid", "⚡")
}

data class RelayNode(
    val id: String,
    val ip: String,
    val alias: String,
    val country: String,
    val bandwidthMbps: Int,
    val uptimePercent: Float,
    val isExitNode: Boolean = false
)

data class HiddenService(
    val address: String, // e.g. "blackvault.onion"
    val name: String,
    val type: HiddenServiceType,
    val accessLevel: AccessLevel,
    val description: String,
    val verified: Boolean = true,
    val ownerHandle: String = "anonymous",
    val activeUsersCount: Int = (5..120).random(),
    val createdTimestamp: Long = System.currentTimeMillis() - (86400000L * (30..365).random())
)
