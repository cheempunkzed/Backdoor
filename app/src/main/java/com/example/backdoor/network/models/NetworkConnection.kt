package com.example.backdoor.network.models

import java.util.UUID

/**
 * Represents an active network link between two virtual nodes.
 *
 * @property id Connection link UUID
 * @property sourceNodeId Origin node ID
 * @property targetNodeId Target node ID
 * @property bandwidthMbps Link speed rating
 * @property activePackets Count of active in-flight packets
 * @property isEncrypted Transport encryption state
 */
data class NetworkConnection(
    val id: String = UUID.randomUUID().toString(),
    val sourceNodeId: String,
    val targetNodeId: String,
    val bandwidthMbps: Int = 1000,
    val activePackets: Int = 0,
    val isEncrypted: Boolean = true
)
