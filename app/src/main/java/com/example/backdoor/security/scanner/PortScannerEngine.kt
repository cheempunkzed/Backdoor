package com.example.backdoor.security.scanner

import com.example.backdoor.corporate.CorporateServer
import com.example.backdoor.network.models.NetworkNode
import com.example.backdoor.network.models.NetworkService

enum class PortState {
    OPEN,
    CLOSED,
    FILTERED
}

data class PortScanResult(
    val port: Int,
    val state: PortState,
    val serviceName: String,
    val protocol: String = "TCP",
    val banner: String = "",
    val version: String = "1.0",
    val responseTimeMs: Long = 12L
)

data class NodeScanSummary(
    val targetIp: String,
    val hostname: String,
    val ownerOrg: String,
    val openPortsCount: Int,
    val totalScannedPorts: Int,
    val portResults: List<PortScanResult>
)

/**
 * Non-destructive port scanner engine for inspecting virtual servers and network nodes in AbyssNet.
 */
class PortScannerEngine {

    private val commonPorts = listOf(
        21, 22, 25, 53, 80, 88, 110, 143, 443, 445, 1194, 3306, 3389, 5432, 8000, 8080, 8081, 8443, 27017
    )

    /**
     * Scans a target CorporateServer for open ports and service details.
     */
    fun scanCorporateServer(server: CorporateServer): NodeScanSummary {
        val results = mutableListOf<PortScanResult>()
        val openPortsMap = server.services.associateBy { it.port }

        commonPorts.forEach { port ->
            val existingSvc = openPortsMap[port]
            if (existingSvc != null && existingSvc.isOpen) {
                results.add(
                    PortScanResult(
                        port = port,
                        state = PortState.OPEN,
                        serviceName = existingSvc.name,
                        protocol = "TCP",
                        banner = "${existingSvc.name} v2.4 (${server.type.displayName})",
                        version = "v2.4.1",
                        responseTimeMs = (5..28).random().toLong()
                    )
                )
            } else if (port == server.type.defaultPort) {
                results.add(
                    PortScanResult(
                        port = port,
                        state = PortState.OPEN,
                        serviceName = server.type.defaultService,
                        protocol = "TCP",
                        banner = "${server.type.defaultService} Enterprise Daemon",
                        version = "v3.1",
                        responseTimeMs = (4..18).random().toLong()
                    )
                )
            } else {
                val state = if (server.securityLevel >= 4 && (port == 22 || port == 3389)) PortState.FILTERED else PortState.CLOSED
                results.add(
                    PortScanResult(
                        port = port,
                        state = state,
                        serviceName = getKnownServiceName(port),
                        protocol = "TCP",
                        banner = "",
                        version = "",
                        responseTimeMs = (15..45).random().toLong()
                    )
                )
            }
        }

        val openCount = results.count { it.state == PortState.OPEN }
        return NodeScanSummary(
            targetIp = server.ip,
            hostname = server.name,
            ownerOrg = server.domain,
            openPortsCount = openCount,
            totalScannedPorts = commonPorts.size,
            portResults = results
        )
    }

    /**
     * Scans a generic NetworkNode from AbyssNet repository.
     */
    fun scanNetworkNode(node: NetworkNode): NodeScanSummary {
        val results = mutableListOf<PortScanResult>()
        val openPortsMap = node.services.associateBy { it.port }

        commonPorts.forEach { port ->
            val existingSvc = openPortsMap[port]
            if (existingSvc != null && existingSvc.isOpen) {
                results.add(
                    PortScanResult(
                        port = port,
                        state = PortState.OPEN,
                        serviceName = existingSvc.name,
                        protocol = "TCP",
                        banner = existingSvc.banner.ifEmpty { "${existingSvc.name} Daemon" },
                        version = "v1.8.0",
                        responseTimeMs = node.latencyMs + (2..12).random()
                    )
                )
            } else {
                results.add(
                    PortScanResult(
                        port = port,
                        state = PortState.CLOSED,
                        serviceName = getKnownServiceName(port),
                        protocol = "TCP",
                        banner = "",
                        version = "",
                        responseTimeMs = node.latencyMs + (10..30).random()
                    )
                )
            }
        }

        val openCount = results.count { it.state == PortState.OPEN }
        return NodeScanSummary(
            targetIp = node.ip,
            hostname = node.hostname,
            ownerOrg = node.ownerId,
            openPortsCount = openCount,
            totalScannedPorts = commonPorts.size,
            portResults = results
        )
    }

    private fun getKnownServiceName(port: Int): String {
        return when (port) {
            21 -> "FTP"
            22 -> "SSH"
            25 -> "SMTP"
            53 -> "DNS"
            80 -> "HTTP"
            88 -> "Kerberos"
            110 -> "POP3"
            143 -> "IMAP"
            443 -> "HTTPS"
            445 -> "SMB"
            1194 -> "OpenVPN"
            3306 -> "MySQL"
            3389 -> "RDP"
            5432 -> "PostgreSQL"
            8000 -> "HTTP-Alt"
            8080 -> "HTTP-Proxy"
            8081 -> "gRPC"
            8443 -> "HTTPS-Alt"
            27017 -> "MongoDB"
            else -> "UNKNOWN"
        }
    }
}
