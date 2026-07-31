package com.example.backdoor.network.generator

import kotlin.random.Random

/**
 * Generates valid IPv4 addresses deterministically using a seed or randomly within specified subnets.
 */
class IPAddressGenerator(private val seed: Long = 1337L) {

    private var random = Random(seed)

    /**
     * Resets random generator with given seed.
     */
    fun reseed(newSeed: Long) {
        random = Random(newSeed)
    }

    /**
     * Generates a local LAN IP address in 192.168.1.x subnet.
     */
    fun generateLocalIp(hostSuffix: Int? = null): String {
        val suffix = hostSuffix ?: random.nextInt(2, 254)
        return "192.168.1.$suffix"
    }

    /**
     * Generates a private network IP address in 10.x.x.x or 172.16.x.x subnet.
     */
    fun generatePrivateIp(): String {
        return if (random.nextBoolean()) {
            "10.${random.nextInt(0, 255)}.${random.nextInt(0, 255)}.${random.nextInt(1, 254)}"
        } else {
            "172.${random.nextInt(16, 31)}.${random.nextInt(0, 255)}.${random.nextInt(1, 254)}"
        }
    }

    /**
     * Generates a public WAN IP address.
     */
    fun generatePublicIp(): String {
        val octet1 = listOf(45, 89, 104, 157, 185, 192, 203).random(random)
        return "$octet1.${random.nextInt(10, 200)}.${random.nextInt(1, 250)}.${random.nextInt(1, 254)}"
    }
}
