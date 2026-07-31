package com.example.backdoor.network.services

import com.example.backdoor.network.models.NodeType
import kotlin.random.Random

/**
 * Simulates realistic latency and jitter for network packets based on node type and distance.
 */
class LatencySimulator {

    fun calculateLatencyMs(nodeType: NodeType, hops: Int = 1): Long {
        val baseLatency = when (nodeType) {
            NodeType.ROUTER -> 2L
            NodeType.SWITCH -> 1L
            NodeType.PERSONAL_DEVICE -> 8L
            NodeType.SERVER -> 15L
            NodeType.DATABASE -> 18L
            NodeType.FIREWALL -> 5L
            NodeType.IOT_DEVICE -> 22L
            NodeType.UNKNOWN_DEVICE -> 35L
        }
        val jitter = Random.nextLong(-2L, 4L)
        val hopFactor = hops * 4L
        return (baseLatency + hopFactor + jitter).coerceAtLeast(1L)
    }
}
