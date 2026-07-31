package com.example.backdoor.corporate

import com.example.backdoor.network.engine.AbyssNetworkEngine
import com.example.backdoor.network.models.NetworkService
import com.example.backdoor.network.models.NodeStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

class CorporateGridRepository(
    private val seed: Long = 202606L
) {
    private val rng = Random(seed)

    private val _organizations = MutableStateFlow<List<Organization>>(emptyList())
    val organizations: StateFlow<List<Organization>> = _organizations.asStateFlow()

    private val _totalServersCount = MutableStateFlow(0)
    val totalServersCount: StateFlow<Int> = _totalServersCount.asStateFlow()

    private val _totalDataCentersCount = MutableStateFlow(0)
    val totalDataCentersCount: StateFlow<Int> = _totalDataCentersCount.asStateFlow()

    init {
        generateCorporateGrid()
    }

    fun generateCorporateGrid() {
        val orgList = mutableListOf<Organization>()
        val prefixes = listOf(
            "Aegis", "Apex", "Omni", "Vanguard", "Cyberdyne", "Obsidian", "Titan", "Horizon",
            "Quantum", "Nexa", "Aether", "Sovereign", "Helios", "Atlas", "Hyperion", "Chronos",
            "Genesis", "Krypton", "Nexus", "Orion", "Phalanx", "Prism", "Spectra", "Synergy",
            "Trident", "Vector", "Zenith", "Aero", "BioSynth", "Cipher", "Dynasty", "Echelon",
            "Fortress", "Giga", "Ironclad", "Kestrel", "Luminary", "Monolith", "Neuro", "Onyx",
            "Pandora", "Quantum", "Raven", "Sentinel", "Tachyon", "Ultima", "Valence", "Warp",
            "Xenon", "Yotta", "Zephyr"
        )

        val suffixes = listOf(
            "Corp", "Systems", "Health", "Defense", "Technologies", "Financial", "Telecom",
            "Cloud Services", "Research", "Media Group", "Industries", "Global", "Networks",
            "Security", "Solutions", "Dynamics", "Labs", "Capital", "Logistics", "Energy",
            "Aerospace", "Genomics", "Robotics", "Microsystems", "Informatics"
        )

        val domainsEnd = listOf("com", "net", "org", "gov", "io", "tech", "ai", "cloud")

        val locations = listOf(
            "New York (US-EAST)", "San Francisco (US-WEST)", "Frankfurt (EU-CENTRAL)",
            "London (EU-WEST)", "Tokyo (AP-NORTHEAST)", "Singapore (AP-SOUTH)",
            "Zurich (EU-SWISS)", "Sydney (AP-SOUTHEAST)", "Toronto (CA-EAST)",
            "Stockholm (EU-NORTH)", "Seoul (AP-EAST)", "Dubai (ME-SOUTH)"
        )

        var globalServerCounter = 0
        var globalDcCounter = 0

        // Generate at least 105 distinct organizations
        val industries = IndustryType.entries.toTypedArray()

        for (i in 1..105) {
            val prefix = prefixes[(i - 1) % prefixes.size]
            val suffix = suffixes[(i * 3 + rng.nextInt(5)) % suffixes.size]
            val orgName = "$prefix $suffix"
            val code = (prefix.take(3) + (100 + i)).uppercase()
            val industry = industries[(i - 1) % industries.size]
            val secLevel = rng.nextInt(1, 6)
            val reputation = rng.nextInt(40, 100)
            val budget = rng.nextLong(5_000_000L, 2_500_000_000L)
            val domain = "${prefix.lowercase()}-${suffix.lowercase().replace(" ", "")}.${domainsEnd[i % domainsEnd.size]}"
            val subnetBase = "10.${(i % 240) + 10}"
            val topology = NetworkTopologyType.entries[i % NetworkTopologyType.entries.size]
            val empCount = rng.nextInt(50, 25000)

            // Generate 1..3 Data Centers per org
            val dcCount = rng.nextInt(1, 4)
            val dcList = mutableListOf<DataCenter>()
            val serverList = mutableListOf<CorporateServer>()

            for (dcIdx in 1..dcCount) {
                globalDcCounter++
                val dcId = "dc-${code.lowercase()}-$dcIdx"
                val dcName = "$prefix DC-$dcIdx (${locations[globalDcCounter % locations.size]})"
                val routerIp = "$subnetBase.$dcIdx.1"
                val racks = rng.nextInt(8, 64)

                // Generate 12..45 servers per Data Center -> total 2500+ servers across 105 orgs
                val serversInDc = rng.nextInt(15, 48)
                dcList.add(
                    DataCenter(
                        id = dcId,
                        name = dcName,
                        orgId = code,
                        location = locations[globalDcCounter % locations.size],
                        racksCount = racks,
                        serversCount = serversInDc,
                        coolingStatus = if (rng.nextInt(10) > 8) "WARNING" else "OPTIMAL",
                        powerStatus = "PRIMARY_GRID",
                        routerIp = routerIp
                    )
                )

                val serverTypes = ServerType.entries.toTypedArray()

                for (sIdx in 1..serversInDc) {
                    globalServerCounter++
                    val sType = serverTypes[(sIdx - 1) % serverTypes.size]
                    val sName = "${code.lowercase()}-${sType.name.lowercase().take(4)}-$sIdx"
                    val ip = "$subnetBase.$dcIdx.${sIdx + 10}"
                    val mac = String.format("02:%02X:%02X:%02X:%02X:%02X", i % 256, dcIdx, sIdx % 256, (i * 7) % 256, (sIdx * 13) % 256)
                    val serverDomain = if (sIdx == 1 && sType == ServerType.WEB) domain else "$sName.$domain"

                    // Default open services
                    val svcs = mutableListOf<NetworkService>()
                    svcs.add(NetworkService(sType.defaultService, sType.defaultPort, true, "${sType.displayName} Daemon"))
                    if (sType == ServerType.WEB || sType == ServerType.PROXY) {
                        svcs.add(NetworkService("HTTP", 80, true, "Nginx Web Gateway"))
                        svcs.add(NetworkService("HTTPS", 443, true, "TLS 1.3 Secure Web"))
                    }
                    if (secLevel <= 3) {
                        svcs.add(NetworkService("SSH", 22, true, "OpenSSH 8.9"))
                    }

                    val server = CorporateServer(
                        id = "srv-$globalServerCounter",
                        name = sName,
                        type = sType,
                        ip = ip,
                        mac = mac,
                        domain = serverDomain,
                        securityLevel = secLevel,
                        rackId = "RACK-${(sIdx % racks) + 1}",
                        dataCenterId = dcId,
                        status = if (rng.nextInt(100) > 95) NodeStatus.DEGRADED else NodeStatus.ONLINE,
                        services = svcs,
                        vulnerabilities = if (secLevel <= 2) listOf("CVE-2024-3809", "WEAK_SSH_KEY") else emptyList()
                    )
                    serverList.add(server)
                }
            }

            val org = Organization(
                id = "org-$code",
                name = orgName,
                code = code,
                industry = industry,
                securityLevel = secLevel,
                reputation = reputation,
                budget = budget,
                domain = domain,
                subnet = "$subnetBase.0.0/16",
                servers = serverList,
                dataCenters = dcList,
                topologyType = topology,
                employeeCount = empCount,
                description = "Leading enterprise in ${industry.displayName} operating ${dcList.size} data centers with ${serverList.size} managed server nodes."
            )
            orgList.add(org)
        }

        _organizations.value = orgList
        _totalServersCount.value = globalServerCounter
        _totalDataCentersCount.value = globalDcCounter
    }

    /**
     * Registers all corporate nodes and domain records into the AbyssNetworkEngine.
     */
    fun registerWithNetworkEngine(engine: AbyssNetworkEngine) {
        val allOrgs = _organizations.value
        allOrgs.forEach { org ->
            // Register corporate primary domain and router
            engine.domainResolver.registerMapping(org.domain, org.servers.firstOrNull()?.ip ?: "10.0.0.1")
            engine.domainResolver.registerMapping("www.${org.domain}", org.servers.firstOrNull()?.ip ?: "10.0.0.1")

            // Register each server node
            org.servers.forEach { srv ->
                val node = srv.toNetworkNode(org.name)
                engine.repository.addNode(node)
                engine.domainResolver.registerNode(node)
            }
        }
    }

    fun searchOrganizations(query: String): List<Organization> {
        if (query.isEmpty()) return _organizations.value
        val clean = query.trim().lowercase()
        return _organizations.value.filter {
            it.name.lowercase().contains(clean) ||
                it.code.lowercase().contains(clean) ||
                it.domain.lowercase().contains(clean) ||
                it.industry.displayName.lowercase().contains(clean)
        }
    }

    fun filterOrganizations(
        industry: IndustryType? = null,
        minSecurityLevel: Int? = null,
        query: String = ""
    ): List<Organization> {
        var result = searchOrganizations(query)
        if (industry != null) {
            result = result.filter { it.industry == industry }
        }
        if (minSecurityLevel != null) {
            result = result.filter { it.securityLevel >= minSecurityLevel }
        }
        return result
    }

    fun getOrganizationByDomain(domain: String): Organization? {
        val clean = domain.trim().lowercase().removePrefix("www.")
        return _organizations.value.find { it.domain.equals(clean, ignoreCase = true) }
    }

    fun getOrganizationById(id: String): Organization? {
        return _organizations.value.find { it.id == id }
    }

    fun serializeToJson(): String {
        val jsonArray = JSONArray()
        _organizations.value.forEach { org ->
            val obj = JSONObject()
            obj.put("id", org.id)
            obj.put("name", org.name)
            obj.put("code", org.code)
            obj.put("industry", org.industry.name)
            obj.put("securityLevel", org.securityLevel)
            obj.put("reputation", org.reputation)
            obj.put("budget", org.budget)
            obj.put("domain", org.domain)
            obj.put("subnet", org.subnet)
            obj.put("topologyType", org.topologyType.name)
            obj.put("employeeCount", org.employeeCount)
            obj.put("serverCount", org.servers.size)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }
}
