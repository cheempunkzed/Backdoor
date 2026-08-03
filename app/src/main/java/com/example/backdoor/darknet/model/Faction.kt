package com.example.backdoor.darknet.model

data class Faction(
    val id: String,
    val name: String,
    val tag: String,
    val ideology: String,
    val trustRequirement: Int,
    val enemies: List<String>,
    val allies: List<String>,
    val hiddenForums: List<String>,
    val contractCount: Int = (3..12).random(),
    val rewards: List<String> = emptyList(),
    val description: String = ""
)

data class FactionStanding(
    val factionId: String,
    val reputation: Int = 0, // -100 to 1000
    val trustTier: String = "Neutral" // Hostile, Suspicious, Neutral, Trusted, Elite, Leader
)
