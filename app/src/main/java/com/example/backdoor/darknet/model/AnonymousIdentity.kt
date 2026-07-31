package com.example.backdoor.darknet.model

enum class CommunityRank(val title: String) {
    NOVICE("Novice Lurker"),
    MEMEBER("Active Contributor"),
    OPERATOR("Network Operator"),
    CYPHERPUNK("Veteran Cypherpunk"),
    SHADOW_ADMIN("Shadow Administrator")
}

data class UserReputation(
    var trust: Int = 100,       // 0 - 10000
    var fame: Int = 50,         // 0 - 10000
    var suspicion: Int = 0,     // 0 - 10000 (higher = trace risk)
    val rank: CommunityRank = CommunityRank.NOVICE
) {
    fun calculateScore(): Int = (trust * 1.5 + fame * 0.8 - suspicion * 2.0).toInt().coerceAtLeast(0)
}

data class AnonymousIdentity(
    val id: String,
    val handle: String,
    val personality: String,
    val knowledgeDomain: String,
    val reputation: UserReputation,
    val affiliations: List<String>,
    val activityPattern: String = "Occasional Night Shift",
    val avatarColorHex: String = "#00F0FF"
)
