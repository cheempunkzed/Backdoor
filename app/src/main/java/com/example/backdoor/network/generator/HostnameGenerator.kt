package com.example.backdoor.network.generator

import com.example.backdoor.network.models.NodeType
import kotlin.random.Random

/**
 * Generates descriptive hostnames for virtual devices based on node type and seed.
 */
class HostnameGenerator(private val seed: Long = 42L) {

    private var random = Random(seed)

    fun reseed(newSeed: Long) {
        random = Random(newSeed)
    }

    fun generateHostname(nodeType: NodeType): String {
        val prefixes = when (nodeType) {
            NodeType.ROUTER -> listOf("router-home", "gateway-core", "edge-router", "net-gateway")
            NodeType.SWITCH -> listOf("sw-floor1", "core-switch", "managed-sw", "vlan-switch")
            NodeType.PERSONAL_DEVICE -> listOf("player-pc", "workstation-01", "thinkpad-dev", "desktop-main")
            NodeType.SERVER -> listOf("nas-storage", "auth-srv", "app-cluster", "file-srv", "sys-log")
            NodeType.DATABASE -> listOf("db-primary", "sql-cluster", "redis-cache", "data-vault")
            NodeType.FIREWALL -> listOf("fw-perimeter", "shield-wall", "utm-gateway", "sec-guard")
            NodeType.IOT_DEVICE -> listOf("smart-tv", "iot-camera", "thermostat-01", "hub-gateway")
            NodeType.UNKNOWN_DEVICE -> listOf("anon-node", "unknown-mac", "ghost-device", "unbound-host")
        }
        val prefix = prefixes.random(random)
        val tag = random.nextInt(10, 99)
        return "$prefix-$tag"
    }
}
