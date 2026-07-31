package com.example.backdoor.network.repository

import com.example.backdoor.network.models.NetworkConnection
import com.example.backdoor.network.models.NetworkEvent
import com.example.backdoor.network.models.NetworkNode
import com.example.backdoor.network.models.NetworkService
import com.example.backdoor.network.models.NodeStatus
import com.example.backdoor.network.models.NodeType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

interface NetworkRepository {
    val nodes: StateFlow<List<NetworkNode>>
    val connections: StateFlow<List<NetworkConnection>>
    val events: SharedFlow<NetworkEvent>

    fun addNode(node: NetworkNode)
    fun updateNode(node: NetworkNode)
    fun getNodeById(id: String): NetworkNode?
    fun getNodeByIp(ip: String): NetworkNode?
    fun getNodeByHostname(hostname: String): NetworkNode?

    fun addConnection(connection: NetworkConnection)
    fun removeConnection(connectionId: String)

    fun emitEvent(event: NetworkEvent)

    fun serializeToJson(): String
    fun deserializeFromJson(json: String)
}

class InMemoryNetworkRepository : NetworkRepository {

    private val _nodes = MutableStateFlow<List<NetworkNode>>(emptyList())
    override val nodes: StateFlow<List<NetworkNode>> = _nodes.asStateFlow()

    private val _connections = MutableStateFlow<List<NetworkConnection>>(emptyList())
    override val connections: StateFlow<List<NetworkConnection>> = _connections.asStateFlow()

    private val _events = MutableSharedFlow<NetworkEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<NetworkEvent> = _events.asSharedFlow()

    override fun addNode(node: NetworkNode) {
        val current = _nodes.value.toMutableList()
        current.removeAll { it.id == node.id || it.ip == node.ip }
        current.add(node)
        _nodes.value = current
    }

    override fun updateNode(node: NetworkNode) {
        addNode(node)
    }

    override fun getNodeById(id: String): NetworkNode? {
        return _nodes.value.find { it.id == id }
    }

    override fun getNodeByIp(ip: String): NetworkNode? {
        return _nodes.value.find { it.ip == ip }
    }

    override fun getNodeByHostname(hostname: String): NetworkNode? {
        val clean = hostname.lowercase().trim().removeSuffix(".local")
        return _nodes.value.find { it.hostname.equals(clean, ignoreCase = true) }
    }

    override fun addConnection(connection: NetworkConnection) {
        val current = _connections.value.toMutableList()
        current.removeAll { it.id == connection.id }
        current.add(connection)
        _connections.value = current
    }

    override fun removeConnection(connectionId: String) {
        _connections.value = _connections.value.filterNot { it.id == connectionId }
    }

    override fun emitEvent(event: NetworkEvent) {
        _events.tryEmit(event)
    }

    override fun serializeToJson(): String {
        val root = JSONObject()
        val nodesArr = JSONArray()
        _nodes.value.forEach { node ->
            val obj = JSONObject().apply {
                put("id", node.id)
                put("hostname", node.hostname)
                put("ip", node.ip)
                put("mac", node.mac)
                put("nodeType", node.nodeType.name)
                put("status", node.status.name)
                put("latencyMs", node.latencyMs)
                put("ownerId", node.ownerId)
                put("securityLevel", node.securityLevel)
                put("isDiscovered", node.isDiscovered)
                put("lastSeenTimestamp", node.lastSeenTimestamp)

                val servicesArr = JSONArray()
                node.services.forEach { s ->
                    val sObj = JSONObject().apply {
                        put("name", s.name)
                        put("port", s.port)
                        put("isOpen", s.isOpen)
                        put("banner", s.banner)
                    }
                    servicesArr.put(sObj)
                }
                put("services", servicesArr)
            }
            nodesArr.put(obj)
        }
        root.put("nodes", nodesArr)

        val connArr = JSONArray()
        _connections.value.forEach { c ->
            val cObj = JSONObject().apply {
                put("id", c.id)
                put("sourceNodeId", c.sourceNodeId)
                put("targetNodeId", c.targetNodeId)
                put("bandwidthMbps", c.bandwidthMbps)
                put("activePackets", c.activePackets)
                put("isEncrypted", c.isEncrypted)
            }
            connArr.put(cObj)
        }
        root.put("connections", connArr)

        return root.toString()
    }

    override fun deserializeFromJson(json: String) {
        if (json.isEmpty()) return
        try {
            val root = JSONObject(json)
            val nodesArr = root.optJSONArray("nodes")
            if (nodesArr != null) {
                val list = mutableListOf<NetworkNode>()
                for (i in 0 until nodesArr.length()) {
                    val obj = nodesArr.getJSONObject(i)
                    val sArr = obj.optJSONArray("services")
                    val services = mutableListOf<NetworkService>()
                    if (sArr != null) {
                        for (j in 0 until sArr.length()) {
                            val sObj = sArr.getJSONObject(j)
                            services.add(
                                NetworkService(
                                    name = sObj.getString("name"),
                                    port = sObj.getInt("port"),
                                    isOpen = sObj.optBoolean("isOpen", true),
                                    banner = sObj.optString("banner", "")
                                )
                            )
                        }
                    }
                    list.add(
                        NetworkNode(
                            id = obj.getString("id"),
                            hostname = obj.getString("hostname"),
                            ip = obj.getString("ip"),
                            mac = obj.getString("mac"),
                            nodeType = NodeType.valueOf(obj.getString("nodeType")),
                            status = NodeStatus.valueOf(obj.getString("status")),
                            latencyMs = obj.getLong("latencyMs"),
                            ownerId = obj.getString("ownerId"),
                            securityLevel = obj.getInt("securityLevel"),
                            isDiscovered = obj.getBoolean("isDiscovered"),
                            lastSeenTimestamp = obj.getLong("lastSeenTimestamp"),
                            services = services
                        )
                    )
                }
                _nodes.value = list
            }

            val connArr = root.optJSONArray("connections")
            if (connArr != null) {
                val cList = mutableListOf<NetworkConnection>()
                for (i in 0 until connArr.length()) {
                    val cObj = connArr.getJSONObject(i)
                    cList.add(
                        NetworkConnection(
                            id = cObj.getString("id"),
                            sourceNodeId = cObj.getString("sourceNodeId"),
                            targetNodeId = cObj.getString("targetNodeId"),
                            bandwidthMbps = cObj.optInt("bandwidthMbps", 1000),
                            activePackets = cObj.optInt("activePackets", 0),
                            isEncrypted = cObj.optBoolean("isEncrypted", true)
                        )
                    )
                }
                _connections.value = cList
            }
        } catch (e: Exception) {
            // Soft failure, fallback to current memory
        }
    }
}
