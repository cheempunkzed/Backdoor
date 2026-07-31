package com.example.backdoor.network.engine

import com.example.backdoor.network.generator.HostnameGenerator
import com.example.backdoor.network.generator.IPAddressGenerator
import com.example.backdoor.network.generator.MacAddressGenerator
import com.example.backdoor.network.models.NetworkConnection
import com.example.backdoor.network.models.NetworkEvent
import com.example.backdoor.network.models.NetworkNode
import com.example.backdoor.network.models.NetworkService
import com.example.backdoor.network.models.NodeStatus
import com.example.backdoor.network.models.NodeType
import com.example.backdoor.network.repository.InMemoryNetworkRepository
import com.example.backdoor.network.repository.NetworkRepository
import com.example.backdoor.network.resolver.DomainResolver
import com.example.backdoor.network.services.ConnectionManager
import com.example.backdoor.network.services.LatencySimulator
import com.example.backdoor.network.services.NetworkScanner
import com.example.backdoor.network.services.PacketRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Main coordinator component for AbyssNet virtual network infrastructure.
 * Manages local network topology, address resolvers, packet routing, and network CLI execution.
 */
class AbyssNetworkEngine(
    val repository: NetworkRepository = InMemoryNetworkRepository(),
    val domainResolver: DomainResolver = DomainResolver(),
    val latencySimulator: LatencySimulator = LatencySimulator(),
    val packetRouter: PacketRouter = PacketRouter(latencySimulator),
    val networkScanner: NetworkScanner = NetworkScanner(),
    val connectionManager: ConnectionManager = ConnectionManager(),
    private val seed: Long = 202605L
) {
    private val ipGen = IPAddressGenerator(seed)
    private val hostGen = HostnameGenerator(seed)
    private val macGen = MacAddressGenerator(seed)

    val nodes: StateFlow<List<NetworkNode>> get() = repository.nodes
    val connections: StateFlow<List<NetworkConnection>> get() = repository.connections

    init {
        initializeDefaultLocalNetwork()
    }

    /**
     * Initializes the player's home local network (Home Router, Player PC, Smart TV, NAS, IoT Device).
     */
    fun initializeDefaultLocalNetwork() {
        if (repository.nodes.value.isNotEmpty()) return

        val router = NetworkNode(
            id = "node-router-01",
            hostname = "router.local",
            ip = "192.168.1.1",
            mac = "00:11:22:33:44:55",
            nodeType = NodeType.ROUTER,
            status = NodeStatus.ONLINE,
            latencyMs = 1L,
            ownerId = "player_gateway",
            securityLevel = 1,
            isDiscovered = true,
            services = listOf(
                NetworkService("HTTP", 80, true, "AbyssRouter WebGUI v2.1"),
                NetworkService("DNS", 53, true, "AbyssDNS Server"),
                NetworkService("DHCP", 67, true, "DHCP Daemon")
            )
        )

        val playerPc = NetworkNode(
            id = "node-player-pc",
            hostname = "player-pc",
            ip = "192.168.1.100",
            mac = "00:1A:2B:3C:4D:5E",
            nodeType = NodeType.PERSONAL_DEVICE,
            status = NodeStatus.ONLINE,
            latencyMs = 0L,
            ownerId = "operator",
            securityLevel = 1,
            isDiscovered = true,
            services = listOf(
                NetworkService("SSH", 22, true, "OpenSSH 8.9p1 AbyssOS"),
                NetworkService("TERMINAL", 8080, true, "Terminal Web Socket")
            )
        )

        val smartTv = NetworkNode(
            id = "node-smart-tv",
            hostname = "smart-tv",
            ip = "192.168.1.105",
            mac = "AA:BB:CC:11:22:33",
            nodeType = NodeType.IOT_DEVICE,
            status = NodeStatus.ONLINE,
            latencyMs = 14L,
            ownerId = "home",
            securityLevel = 1,
            isDiscovered = true,
            services = listOf(
                NetworkService("UPnP", 1900, true, "Media Streaming Service")
            )
        )

        val nasServer = NetworkNode(
            id = "node-nas-storage",
            hostname = "nas-storage",
            ip = "192.168.1.150",
            mac = "54:EE:75:88:99:AA",
            nodeType = NodeType.SERVER,
            status = NodeStatus.ONLINE,
            latencyMs = 4L,
            ownerId = "operator",
            securityLevel = 2,
            isDiscovered = true,
            services = listOf(
                NetworkService("FTP", 21, true, "vsftpd 3.0.3"),
                NetworkService("SMB", 445, true, "Samba Storage Share"),
                NetworkService("HTTP", 8081, true, "NAS Admin Portal")
            )
        )

        val unknownIot = NetworkNode(
            id = "node-unknown-cam",
            hostname = "unknown-device",
            ip = "192.168.1.200",
            mac = "78:90:AB:CD:EF:01",
            nodeType = NodeType.UNKNOWN_DEVICE,
            status = NodeStatus.DEGRADED,
            latencyMs = 42L,
            ownerId = "unknown",
            securityLevel = 3,
            isDiscovered = true,
            services = listOf(
                NetworkService("RTSP", 554, true, "IP Camera Video Feed")
            )
        )

        val gatewayDns = NetworkNode(
            id = "node-abyss-dns",
            hostname = "abyss.net",
            ip = "10.0.0.1",
            mac = "02:00:10:00:00:01",
            nodeType = NodeType.SERVER,
            status = NodeStatus.ONLINE,
            latencyMs = 28L,
            ownerId = "abyss_net",
            securityLevel = 4,
            isDiscovered = true,
            services = listOf(
                NetworkService("DNS", 53, true, "AbyssNet Root Nameserver"),
                NetworkService("HTTPS", 443, true, "AbyssNet Portal")
            )
        )

        val defaultNodes = listOf(router, playerPc, smartTv, nasServer, unknownIot, gatewayDns)
        defaultNodes.forEach {
            repository.addNode(it)
            domainResolver.registerNode(it)
        }

        // Default local connections
        val c1 = connectionManager.establishConnection(playerPc, router)
        val c2 = connectionManager.establishConnection(smartTv, router)
        val c3 = connectionManager.establishConnection(nasServer, router)
        val c4 = connectionManager.establishConnection(unknownIot, router)
        val c5 = connectionManager.establishConnection(router, gatewayDns)

        listOf(c1, c2, c3, c4, c5).forEach { repository.addConnection(it) }
    }

    fun scanNetwork(): List<NetworkNode> {
        val result = networkScanner.scanSubnet(repository.nodes.value)
        result.discoveredNodes.forEach {
            repository.updateNode(it)
            repository.emitEvent(NetworkEvent.NodeDiscovered(it))
        }
        return repository.nodes.value
    }

    fun ping(targetHost: String, count: Int = 4): List<String> {
        val resolvedIp = domainResolver.resolveDomain(targetHost) ?: targetHost
        val targetNode = repository.getNodeByIp(resolvedIp) ?: repository.getNodeByHostname(targetHost)

        val lines = mutableListOf<String>()
        lines.add("PING $targetHost ($resolvedIp) 56(84) bytes of data.")

        if (targetNode == null || targetNode.status == NodeStatus.OFFLINE) {
            for (i in 1..count) {
                lines.add("From 192.168.1.1 icmp_seq=$i Destination Host Unreachable")
            }
            lines.add("--- $targetHost ping statistics ---")
            lines.add("$count packets transmitted, 0 received, 100% packet loss, time ${count * 1000}ms")
            repository.emitEvent(NetworkEvent.PacketTimeout(resolvedIp))
            return lines
        }

        var received = 0
        var totalTime = 0L

        for (i in 1..count) {
            val lat = latencySimulator.calculateLatencyMs(targetNode.nodeType)
            totalTime += lat
            received++
            lines.add("64 bytes from ${targetNode.ip} (${targetNode.hostname}): icmp_seq=$i ttl=64 time=${lat}ms")
        }

        val avgTime = if (received > 0) totalTime / received else 0L
        lines.add("--- $targetHost ping statistics ---")
        lines.add("$count packets transmitted, $received received, 0% packet loss, time ${count * 250}ms")
        lines.add("rtt min/avg/max = ${avgTime - 1}/${avgTime}/${avgTime + 2} ms")
        return lines
    }

    fun traceRoute(targetHost: String): List<String> {
        val resolvedIp = domainResolver.resolveDomain(targetHost) ?: targetHost
        val targetNode = repository.getNodeByIp(resolvedIp) ?: repository.getNodeByHostname(targetHost)
        val routerNode = repository.getNodeByIp("192.168.1.1")

        val routeResult = packetRouter.traceRoute("192.168.1.100", targetNode, routerNode)
        val lines = mutableListOf<String>()
        lines.add("traceroute to $targetHost ($resolvedIp), 30 hops max, 60 byte packets")

        routeResult.hops.forEach { hop ->
            lines.add(" ${hop.hopIndex}  ${hop.hostname} (${hop.nodeIp})  ${hop.latencyMs} ms  ${hop.latencyMs + 1} ms  ${hop.latencyMs + 2} ms")
        }

        if (!routeResult.isReachable) {
            lines.add(" ${routeResult.hops.size + 1}  * * * Request timed out.")
        }

        return lines
    }

    fun getNetstat(): List<String> {
        val lines = mutableListOf<String>()
        lines.add("Active Internet connections (w organisation / servers)")
        lines.add("Proto Recv-Q Send-Q Local Address           Foreign Address         State")

        repository.nodes.value.forEach { node ->
            node.services.filter { it.isOpen }.forEach { service ->
                lines.add(
                    String.format(
                        "tcp        0      0 192.168.1.100:%-5d     %s:%-5d     ESTABLISHED",
                        (1024..65000).random(),
                        node.ip,
                        service.port
                    )
                )
            }
        }
        return lines
    }

    fun getArpTable(): List<String> {
        val lines = mutableListOf<String>()
        lines.add("Address                  HWtype  HWaddress           Flags Mask            Iface")
        repository.nodes.value.forEach { node ->
            lines.add(String.format("%-24s 0x1     %-19s C                     eth0", node.ip, node.mac))
        }
        return lines
    }

    fun getIfconfig(): List<String> {
        return listOf(
            "eth0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500",
            "        inet 192.168.1.100  netmask 255.255.255.0  broadcast 192.168.1.255",
            "        ether 00:1A:2B:3C:4D:5E  txqueuelen 1000  (Ethernet)",
            "        RX packets 14209  bytes 9812401 (9.8 MB)",
            "        TX packets 11042  bytes 1420981 (1.4 MB)",
            "",
            "wlan0: flags=4099<UP,BROADCAST,MULTICAST>  mtu 1500",
            "        inet 10.0.0.42  netmask 255.255.255.0  broadcast 10.0.0.255",
            "        ether 00:1A:2B:3C:4D:5F  txqueuelen 1000  (Wi-Fi)",
            "        RX packets 0  bytes 0 (0.0 B)",
            "        TX packets 0  bytes 0 (0.0 B)",
            "",
            "lo: flags=73<UP,LOOPBACK,RUNNING>  mtu 65536",
            "        inet 127.0.0.1  netmask 255.0.0.0",
            "        loop  txqueuelen 1000  (Local Loopback)"
        )
    }

    fun getRouteTable(): List<String> {
        return listOf(
            "Kernel IP routing table",
            "Destination     Gateway         Genmask         Flags Metric Ref    Use Iface",
            "default         192.168.1.1     0.0.0.0         UG    100    0        0 eth0",
            "10.0.0.0        0.0.0.0         255.255.255.0   U     600    0        0 wlan0",
            "192.168.1.0     0.0.0.0         255.255.255.0   U     100    0        0 eth0"
        )
    }
}
