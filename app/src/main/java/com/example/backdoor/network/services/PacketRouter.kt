package com.example.backdoor.network.services

import com.example.backdoor.network.models.NetworkNode
import com.example.backdoor.network.models.Packet

/**
 * Handles routing of packets between virtual nodes, calculating path hops for traceroute.
 */
class PacketRouter(
    private val latencySimulator: LatencySimulator = LatencySimulator()
) {

    data class HopResult(
        val hopIndex: Int,
        val nodeIp: String,
        val hostname: String,
        val latencyMs: Long
    )

    data class RouteResult(
        val isReachable: Boolean,
        val hops: List<HopResult>,
        val totalLatencyMs: Long,
        val packetLossPercent: Float
    )

    /**
     * Calculates traceroute hops from source node to destination node.
     */
    fun traceRoute(
        sourceIp: String,
        destinationNode: NetworkNode?,
        gatewayNode: NetworkNode?
    ): RouteResult {
        if (destinationNode == null) {
            return RouteResult(
                isReachable = false,
                hops = listOf(
                    HopResult(1, gatewayNode?.ip ?: "192.168.1.1", gatewayNode?.hostname ?: "router.local", 2L)
                ),
                totalLatencyMs = 0L,
                packetLossPercent = 100f
            )
        }

        val hopsList = mutableListOf<HopResult>()
        var currentHop = 1

        // Hop 1: Local Gateway
        if (gatewayNode != null && gatewayNode.ip != destinationNode.ip) {
            val lat = latencySimulator.calculateLatencyMs(gatewayNode.nodeType, 1)
            hopsList.add(HopResult(currentHop++, gatewayNode.ip, gatewayNode.hostname, lat))
        }

        // Target Hop
        val targetLat = latencySimulator.calculateLatencyMs(destinationNode.nodeType, currentHop)
        hopsList.add(HopResult(currentHop, destinationNode.ip, destinationNode.hostname, targetLat))

        val isOnline = destinationNode.status == com.example.backdoor.network.models.NodeStatus.ONLINE
        val loss = if (isOnline) 0f else 100f

        return RouteResult(
            isReachable = isOnline,
            hops = hopsList,
            totalLatencyMs = hopsList.sumOf { it.latencyMs },
            packetLossPercent = loss
        )
    }
}
