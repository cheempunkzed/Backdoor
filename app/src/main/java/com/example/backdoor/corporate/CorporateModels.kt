package com.example.backdoor.corporate

import com.example.backdoor.network.models.NetworkNode
import com.example.backdoor.network.models.NetworkService
import com.example.backdoor.network.models.NodeStatus
import com.example.backdoor.network.models.NodeType

enum class IndustryType(
    val displayName: String,
    val category: String,
    val iconName: String
) {
    TECHNOLOGY("Technology & Software", "IT & AI Engineering", "ic_tech"),
    FINANCE("Finance & Banking", "Investment & Asset Mgmt", "ic_finance"),
    HEALTHCARE("Healthcare & Biotech", "Pharmaceuticals & Genomics", "ic_health"),
    GOVERNMENT("Government Agency", "Civil Administration", "ic_gov"),
    MILITARY("Defense & Intelligence", "Strategic Systems", "ic_military"),
    UNIVERSITY("Academic Research", "Higher Education & Physics", "ic_university"),
    ISP("Telecom & ISP", "Backbone Network Carrier", "ic_isp"),
    CLOUD_PROVIDER("Cloud Infrastructure", "Datacenters & Edge Mesh", "ic_cloud"),
    RESEARCH_LAB("R&D Enterprise", "Quantum & Synthetic Bio", "ic_lab"),
    MEDIA("Global Media Conglomerate", "Broadcasting & Content", "ic_media"),
    MANUFACTURING("Industrial Manufacturing", "Automotive & Heavy Industry", "ic_factory")
}

enum class ServerType(
    val displayName: String,
    val defaultPort: Int,
    val defaultService: String
) {
    AUTHENTICATION("Auth & Directory Server", 88, "Kerberos/LDAP"),
    DATABASE("Corporate Database Node", 5432, "PostgreSQL/Oracle"),
    WEB("Public Web Portal", 443, "HTTPS Web Server"),
    DNS("Domain Name Server", 53, "BIND9 DNS Daemon"),
    MAIL("Enterprise Mail Gateway", 25, "SMTP/Exchange"),
    BACKUP("Cold Storage Backup Vault", 8080, "Encrypted Rsync Vault"),
    FILE("Network Storage NAS", 445, "SMB/NFS Share"),
    PROXY("Reverse Proxy Load Balancer", 8000, "HAProxy Gateway"),
    VPN("Secure Access VPN Gateway", 1194, "WireGuard Tunnel"),
    INTERNAL_API("Microservice API Mesh", 8081, "gRPC Service Mesh")
}

enum class NetworkTopologyType(
    val displayName: String,
    val description: String
) {
    STAR("Star Topology", "Central core router with direct branch node connections"),
    TREE("Hierarchical Tree", "Multilevel distribution tier with core gateways"),
    MESH("Redundant Mesh", "Interconnected fault-tolerant server matrix"),
    HYBRID("Hybrid Enterprise", "Combined star-mesh design for maximum throughput")
}

data class DataCenter(
    val id: String,
    val name: String,
    val orgId: String,
    val location: String,
    val racksCount: Int,
    val serversCount: Int,
    val coolingStatus: String = "OPTIMAL",
    val powerStatus: String = "PRIMARY_GRID",
    val routerIp: String
)

data class CorporateServer(
    val id: String,
    val name: String,
    val type: ServerType,
    val ip: String,
    val mac: String,
    val domain: String,
    val securityLevel: Int,
    val rackId: String,
    val dataCenterId: String,
    val status: NodeStatus = NodeStatus.ONLINE,
    val services: List<NetworkService> = emptyList(),
    // Future hook for Milestone 7
    val vulnerabilities: List<String> = emptyList()
) {
    fun toNetworkNode(orgName: String): NetworkNode {
        return NetworkNode(
            id = id,
            hostname = domain.ifEmpty { name.lowercase().replace(" ", "-") },
            ip = ip,
            mac = mac,
            nodeType = when (type) {
                ServerType.DATABASE -> NodeType.DATABASE
                ServerType.PROXY -> NodeType.FIREWALL
                ServerType.AUTHENTICATION -> NodeType.SERVER
                else -> NodeType.SERVER
            },
            status = status,
            latencyMs = (5..35).random().toLong(),
            ownerId = orgName,
            securityLevel = securityLevel,
            isDiscovered = true,
            services = services
        )
    }
}

data class Organization(
    val id: String,
    val name: String,
    val code: String,
    val industry: IndustryType,
    val securityLevel: Int, // 1 to 5
    val reputation: Int, // 0 to 100
    val budget: Long, // USD
    val domain: String,
    val subnet: String,
    val servers: List<CorporateServer>,
    val dataCenters: List<DataCenter>,
    val topologyType: NetworkTopologyType,
    val employeeCount: Int,
    val description: String
)
