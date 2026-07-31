package com.example.backdoor.network.services

import com.example.backdoor.network.models.NetworkConnection
import com.example.backdoor.network.models.NetworkNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks active sockets and virtual link connections across AbyssNet nodes.
 */
class ConnectionManager {

    private val _activeConnections = MutableStateFlow<List<NetworkConnection>>(emptyList())
    val activeConnections: StateFlow<List<NetworkConnection>> = _activeConnections.asStateFlow()

    fun establishConnection(source: NetworkNode, target: NetworkNode): NetworkConnection {
        val connection = NetworkConnection(
            sourceNodeId = source.id,
            targetNodeId = target.id,
            bandwidthMbps = 1000,
            activePackets = 1,
            isEncrypted = true
        )
        _activeConnections.value = _activeConnections.value + connection
        return connection
    }

    fun terminateConnection(connectionId: String) {
        _activeConnections.value = _activeConnections.value.filterNot { it.id == connectionId }
    }

    fun clearAll() {
        _activeConnections.value = emptyList()
    }
}
