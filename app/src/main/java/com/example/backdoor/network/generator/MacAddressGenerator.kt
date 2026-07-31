package com.example.backdoor.network.generator

import kotlin.random.Random

/**
 * Generates unique standard MAC addresses (e.g. 00:1A:2B:3C:4D:5E).
 */
class MacAddressGenerator(private val seed: Long = 99L) {

    private var random = Random(seed)

    fun reseed(newSeed: Long) {
        random = Random(newSeed)
    }

    fun generateMac(): String {
        val bytes = ByteArray(6)
        random.nextBytes(bytes)
        bytes[0] = (bytes[0].toInt() and 0xFE or 0x02).toByte() // Local admin bit
        return bytes.joinToString(":") { String.format("%02X", it) }
    }
}
