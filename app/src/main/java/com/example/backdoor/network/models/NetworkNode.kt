package com.example.backdoor.network.models

import java.util.UUID

/**
 * Core domain representation of a virtual device connected to AbyssNet.
 *
 * @property id Unique UUID of the node
 * @property hostname Domain or machine name
 * @property ip Assigned IPv4 address
 * @property mac Hardware MAC address
 * @property nodeType Category of device
 * @property status Current operational state
 * @property latencyMs Base network ping latency in milliseconds
 * @property ownerId Entity or user owner identifier
 * @property securityLevel Security clearance requirement (1-10)
 * @property isDiscovered Whether the local scanner has discovered this node
 * @property lastSeenTimestamp Unix timestamp of last active response
 * @property services List of active open network services
 */
data class NetworkNode(
    val id: String = UUID.randomUUID().toString(),
    val hostname: String,
    val ip: String,
    val mac: String,
    val nodeType: NodeType,
    val status: NodeStatus = NodeStatus.ONLINE,
    val latencyMs: Long = 12L,
    val ownerId: String = "system",
    val securityLevel: Int = 1,
    val isDiscovered: Boolean = true,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val services: List<NetworkService> = emptyList()
)
