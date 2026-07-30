package com.example.backdoor.save

data class UserProfile(
    val username: String,
    val passwordHash: String,
    val registeredAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val hostname: String = "abyss-node-01"
)
