package com.example.backdoor.darknet.model

import java.util.UUID

data class DigitalIdentity(
    val id: String = UUID.randomUUID().toString(),
    val nickname: String,
    val pgpFingerprint: String = generatePgpFingerprint(),
    val reputation: Int = 100,
    val trustScore: Int = 150,
    val criminalHeat: Int = 0, // 0 to 100
    val joinDate: Long = System.currentTimeMillis(),
    val aliases: List<String> = emptyList(),
    val hiddenProfile: String = "Encrypted Cypherpunk Profile",
    val isPrimary: Boolean = false
) {
    companion object {
        fun generatePgpFingerprint(): String {
            val chars = "0123456789ABCDEF"
            val block = { (1..4).map { chars.random() }.joinToString("") }
            return "${block()}-${block()}-${block()}-${block()}-${block()}"
        }
    }
}
