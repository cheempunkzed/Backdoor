package com.example.backdoor.network.services

import com.example.backdoor.network.models.NetworkNode
import com.example.backdoor.network.models.NodeStatus

/**
 * Handles active subnet discovery and port scanning.
 */
class NetworkScanner {

    data class ScanResult(
        val discoveredNodes: List<NetworkNode>,
        val totalScanned: Int,
        val durationMs: Long
    )

    /**
     * Performs a scan across registered nodes, returning discovered devices.
     */
    fun scanSubnet(allNodes: List<NetworkNode>): ScanResult {
        val discovered = allNodes.map { node ->
            if (node.status == NodeStatus.ONLINE) {
                node.copy(isDiscovered = true, lastSeenTimestamp = System.currentTimeMillis())
            } else {
                node
            }
        }
        return ScanResult(
            discoveredNodes = discovered,
            totalScanned = allNodes.size,
            durationMs = 240L
        )
    }
}
