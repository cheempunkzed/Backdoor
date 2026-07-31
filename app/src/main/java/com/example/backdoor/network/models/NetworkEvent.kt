package com.example.backdoor.network.models

/**
 * Event hierarchy for system-wide network state changes.
 */
sealed class NetworkEvent {
    data class ConnectionEstablished(val connection: NetworkConnection) : NetworkEvent()
    data class ConnectionLost(val connectionId: String) : NetworkEvent()
    data class NodeDiscovered(val node: NetworkNode) : NetworkEvent()
    data class DeviceOnline(val nodeIp: String) : NetworkEvent()
    data class DeviceOffline(val nodeIp: String) : NetworkEvent()
    data class PacketTimeout(val destinationIp: String) : NetworkEvent()
    data class UnknownHost(val hostname: String) : NetworkEvent()
}
