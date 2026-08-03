package com.example.backdoor.web.engine

import com.example.backdoor.core.SystemEvent
import com.example.backdoor.core.SystemEventBus
import com.example.backdoor.corporate.CorporateGridRepository
import com.example.backdoor.corporate.Organization
import com.example.backdoor.darknet.engine.OnionNetworkEngine
import com.example.backdoor.economy.engine.NewsService
import com.example.backdoor.web.models.WebEntity
import com.example.backdoor.web.models.WebEntityType
import com.example.backdoor.web.models.WebPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WebContentEngine(
    private val scope: CoroutineScope,
    private val eventBus: SystemEventBus,
    private val corporateRepository: CorporateGridRepository,
    private val darknetEngine: OnionNetworkEngine,
    private val newsService: NewsService
) {
    private val _webEntities = MutableStateFlow<Map<String, WebEntity>>(emptyMap())
    val webEntities: StateFlow<Map<String, WebEntity>> = _webEntities.asStateFlow()

    init {
        // Observe system events to update website states dynamically
        scope.launch {
            eventBus.events.collect { event ->
                handleSystemEvent(event)
            }
        }
    }

    /**
     * Resolves or generates a WebEntity for the requested domain/URL.
     * Implements lazy generation and caching.
     */
    fun getWebEntity(domain: String): WebEntity {
        val normalizedDomain = domain.trim().lowercase()
        val existing = _webEntities.value[normalizedDomain]
        if (existing != null) {
            return existing
        }

        // Lazy generation based on domain
        val generated = generateWebEntity(normalizedDomain)
        val updatedMap = _webEntities.value.toMutableMap()
        updatedMap[normalizedDomain] = generated
        _webEntities.value = updatedMap
        return generated
    }

    private fun generateWebEntity(domain: String): WebEntity {
        // Check if matching corporate organization exists
        val corpOrg = corporateRepository.getOrganizationByDomain(domain)
            ?: corporateRepository.organizations.value.find { 
                it.domain.equals(domain, ignoreCase = true) || domain.contains(it.code.lowercase()) 
            }

        if (corpOrg != null) {
            return generateCorporateWebsite(corpOrg, domain)
        }

        return when {
            domain.endsWith(".onion", ignoreCase = true) || domain.equals("darknet", ignoreCase = true) -> {
                generateDarknetHiddenServiceEntity(domain)
            }
            domain.contains("news", ignoreCase = true) || domain.contains("media", ignoreCase = true) -> {
                generateNewsWebsiteEntity(domain)
            }
            domain.contains("wiki", ignoreCase = true) || domain.contains("docs", ignoreCase = true) || domain.contains("developer", ignoreCase = true) -> {
                generateDocumentationWebsiteEntity(domain)
            }
            domain.contains("blog", ignoreCase = true) || domain.contains("v0id", ignoreCase = true) || domain.contains("cypher", ignoreCase = true) -> {
                generatePersonalBlogEntity(domain)
            }
            else -> {
                generateGenericWebsiteEntity(domain)
            }
        }
    }

    private fun generateCorporateWebsite(org: Organization, domain: String): WebEntity {
        val pages = listOf(
            WebPage(
                slug = "home",
                title = "${org.name} - Official Corporate Network",
                sectionName = "HOME",
                content = """
                    Welcome to ${org.name}.
                    
                    ${org.description}
                    
                    Industry Sector: ${org.industry.displayName}
                    Subnet Segment: ${org.subnet}
                    Global Grid Clearance: Tier ${org.securityLevel}
                    
                    LATEST ANNOUNCEMENT:
                    "${org.name} continues deployment of next-generation perimeter security protocols across all ${org.dataCenters.size} regional data centers."
                """.trimIndent()
            ),
            WebPage(
                slug = "about",
                title = "About ${org.name}",
                sectionName = "ABOUT",
                content = """
                    CORPORATE PROFILE
                    -----------------
                    Organization Code: ${org.code}
                    Industry: ${org.industry.displayName}
                    Employee Force: ${org.employeeCount} verified personnel
                    Primary Subnet Gateway: ${org.subnet}.1.1
                    
                    PHILOSOPHY:
                    Providing high-reliability infrastructure and zero-compromise services across global network grids.
                """.trimIndent()
            ),
            WebPage(
                slug = "services",
                title = "Services & Infrastructure Solutions",
                sectionName = "SERVICES",
                content = """
                    MANAGED ENTERPRISE SOLUTIONS
                    ---------------------------
                    • High-Density Server Racks (${org.servers.size} active servers online)
                    • Data Center Interconnects (${org.dataCenters.size} data centers)
                    • Tier ${org.securityLevel} Encrypted Data Vaults
                    • Real-time Automated Threat Scanning & IDS
                """.trimIndent()
            ),
            WebPage(
                slug = "security-status",
                title = "Security & Network Health Status",
                sectionName = "SECURITY STATUS",
                content = """
                    SYSTEM SECURITY METRICS
                    ----------------------
                    Clearance Tier: Level ${org.securityLevel}
                    Perimeter Protection: ACTIVE
                    Firewall Policy: STRICT MATRIX
                    Active Data Centers: ${org.dataCenters.size}
                    Public Server Nodes: ${org.servers.size}
                    
                    AUDIT HISTORY:
                    [OK] All primary subnets operational. Zero unauthorized entry flags reported.
                """.trimIndent()
            ),
            WebPage(
                slug = "press",
                title = "Press Releases & News",
                sectionName = "PRESS",
                content = """
                    PRESS RELEASES
                    --------------
                    [${getCurrentDateString()}] ${org.name} Deploys Tier ${org.securityLevel} Automated Sentinel Defense System.
                    [${getCurrentDateString()}] Quarterly Infrastructure Audit Completed across ${org.dataCenters.size} Data Centers.
                """.trimIndent()
            ),
            WebPage(
                slug = "contact",
                title = "Technical Contact & NOC Portal",
                sectionName = "CONTACT",
                content = """
                    NETWORK OPERATIONS CENTER (NOC)
                    --------------------------------
                    Primary Gateway: ${org.subnet}.1.1
                    Main Domain: ${org.domain}
                    Security Contact: sec-ops@${org.domain}
                    Admin Portal: admin.${org.domain}
                """.trimIndent()
            )
        )

        return WebEntity(
            domain = domain,
            name = org.name,
            ownerOrganizationId = org.id,
            serverIp = org.servers.firstOrNull()?.ip,
            securityLevel = org.securityLevel,
            contentType = WebEntityType.CORPORATE,
            pages = pages,
            dynamicData = mapOf(
                "employeeCount" to org.employeeCount.toString(),
                "serverCount" to org.servers.size.toString(),
                "dataCenterCount" to org.dataCenters.size.toString()
            ),
            eventHistory = listOf("Corporate network initialized on grid.")
        )
    }

    private fun generateDarknetHiddenServiceEntity(domain: String): WebEntity {
        val hiddenService = darknetEngine.getHiddenServiceByAddress(domain)
        val pages = listOf(
            WebPage(
                slug = "home",
                title = hiddenService?.name ?: "Abyss DarkNet Node ($domain)",
                sectionName = "OVERVIEW",
                content = """
                    ABYSS DARKNET HIDDEN SERVICE NODE
                    ----------------------------------
                    Address: $domain
                    Access Clearance: ${hiddenService?.accessLevel?.displayName ?: "Encrypted Public"}
                    Owner: @${hiddenService?.ownerHandle ?: "v0id_walker"}
                    
                    ${hiddenService?.description ?: "Encrypted darknet node routing through multi-hop onion circuits."}
                """.trimIndent()
            )
        )

        return WebEntity(
            domain = domain,
            name = hiddenService?.name ?: "Abyss DarkNet Service",
            securityLevel = 5,
            contentType = WebEntityType.DARKNET_HIDDEN_SERVICE,
            pages = pages,
            eventHistory = listOf("Onion circuit established.")
        )
    }

    private fun generateNewsWebsiteEntity(domain: String): WebEntity {
        val latestArticles = newsService.feed.value
        val newsContent = if (latestArticles.isNotEmpty()) {
            latestArticles.take(5).joinToString("\n\n---\n\n") { art ->
                "[${art.category.name}] ${art.title}\nSource: ${art.source}\n${art.content}"
            }
        } else {
            "GLOBAL NEWS MATRIX ONLINE.\nMonitoring corporate developments, security incidents, and market movements 24/7."
        }

        val pages = listOf(
            WebPage(
                slug = "home",
                title = "Global News Matrix",
                sectionName = "HEADLINES",
                content = newsContent
            )
        )

        return WebEntity(
            domain = domain,
            name = "Global News Network",
            contentType = WebEntityType.NEWS,
            pages = pages
        )
    }

    private fun generateDocumentationWebsiteEntity(domain: String): WebEntity {
        val pages = listOf(
            WebPage(
                slug = "home",
                title = "AbyssOS Technical Manuals & Knowledge Portal",
                sectionName = "MANUALS",
                content = """
                    ABYSSOS SYSTEM ARCHITECTURE & COMMAND MANUALS
                    ----------------------------------------------
                    Welcome to the official technical database.
                    
                    AVAILABLE SECTIONS:
                    • POSIX Shell Commands & VFS Syntax
                    • AbyssNet Virtual Socket API & Domain Resolution
                    • Offensive Security Framework & Exploit Payloads
                    • DarkNet Onion Multi-Hop Routing Specifications
                """.trimIndent()
            )
        )

        return WebEntity(
            domain = domain,
            name = "AbyssOS Documentation Portal",
            contentType = WebEntityType.DOCUMENTATION,
            pages = pages
        )
    }

    private fun generatePersonalBlogEntity(domain: String): WebEntity {
        val pages = listOf(
            WebPage(
                slug = "home",
                title = "The Underground Synthesizer Blog",
                sectionName = "ARTICLES",
                content = """
                    NOTES FROM THE CYPHERPUNK FRONT
                    -------------------------------
                    Author: @v0id_walker
                    
                    "Corporate firewalls are moving towards predictive neural IDS.
                    Always ensure your onion relay circuits use randomized packet padding."
                    
                    POSTS:
                    1. Kernel Memory Exploitation in Tier 4 Routers
                    2. RSA-4096 Key Fingerprint Verification Protocols
                    3. Why Clearnet Corporate Scanning Is a Trap
                """.trimIndent()
            )
        )

        return WebEntity(
            domain = domain,
            name = "Cypherpunk Research Blog",
            contentType = WebEntityType.PERSONAL_BLOG,
            pages = pages
        )
    }

    private fun generateGenericWebsiteEntity(domain: String): WebEntity {
        val pages = listOf(
            WebPage(
                slug = "home",
                title = domain.uppercase(),
                sectionName = "HOME",
                content = "Welcome to $domain. Server active on AbyssNet."
            )
        )

        return WebEntity(
            domain = domain,
            name = domain,
            contentType = WebEntityType.CORPORATE,
            pages = pages
        )
    }

    private fun handleSystemEvent(event: SystemEvent) {
        when (event) {
            is SystemEvent.ServerCompromised -> {
                val targetHost = event.targetHost
                val compId = event.companyId
                val org = if (compId != null) corporateRepository.getOrganizationById(compId)
                          else corporateRepository.organizations.value.find { o -> o.servers.any { s -> s.domain.equals(targetHost, ignoreCase = true) || s.ip == targetHost } }

                if (org != null) {
                    val entity = getWebEntity(org.domain)
                    val history = entity.eventHistory + "SECURITY ADVISORY: Unauthorized access detected on host $targetHost at ${getCurrentDateString()}."
                    
                    val updatedPages = entity.pages.map { page ->
                        if (page.sectionName == "SECURITY STATUS") {
                            page.copy(
                                content = page.content + "\n\n[ALERT] SECURITY INCIDENT REPORTED: Unauthorized intrusion on server $targetHost."
                            )
                        } else if (page.sectionName == "PRESS") {
                            page.copy(
                                content = "[URGENT] ${org.name} Investigates Network Intrusion on Host $targetHost.\n" + page.content
                            )
                        } else {
                            page
                        }
                    }

                    val updatedEntity = entity.copy(
                        pages = updatedPages,
                        eventHistory = history,
                        lastUpdated = System.currentTimeMillis()
                    )

                    val newMap = _webEntities.value.toMutableMap()
                    newMap[org.domain] = updatedEntity
                    _webEntities.value = newMap
                }
            }

            is SystemEvent.CompanySecurityChanged -> {
                val org = corporateRepository.getOrganizationById(event.companyId)
                if (org != null) {
                    val entity = getWebEntity(org.domain)
                    val history = entity.eventHistory + "Security level updated to Tier ${event.newSecurityLevel}."
                    
                    val updatedPages = entity.pages.map { page ->
                        if (page.sectionName == "SECURITY STATUS") {
                            page.copy(
                                content = page.content.replace(Regex("Clearance Tier: Level \\d+"), "Clearance Tier: Level ${event.newSecurityLevel}")
                            )
                        } else {
                            page
                        }
                    }

                    val updatedEntity = entity.copy(
                        securityLevel = event.newSecurityLevel,
                        pages = updatedPages,
                        eventHistory = history,
                        lastUpdated = System.currentTimeMillis()
                    )

                    val newMap = _webEntities.value.toMutableMap()
                    newMap[org.domain] = updatedEntity
                    _webEntities.value = newMap
                }
            }

            is SystemEvent.DataLeakDetected -> {
                val org = corporateRepository.getOrganizationById(event.companyId)
                if (org != null) {
                    val entity = getWebEntity(org.domain)
                    val updatedPages = entity.pages.map { page ->
                        if (page.sectionName == "PRESS") {
                            page.copy(content = "[STATEMENT] ${org.name} Responds to Data Leak Advisory: ${event.leakTitle}\n\n" + page.content)
                        } else page
                    }
                    val updatedEntity = entity.copy(pages = updatedPages, lastUpdated = System.currentTimeMillis())
                    val newMap = _webEntities.value.toMutableMap()
                    newMap[org.domain] = updatedEntity
                    _webEntities.value = newMap
                }
            }

            else -> {}
        }
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        return sdf.format(Date())
    }

    fun serializeToJson(): String {
        val root = JSONObject()
        val entitiesArr = JSONArray()

        for ((_, entity) in _webEntities.value) {
            val eObj = JSONObject()
            eObj.put("domain", entity.domain)
            eObj.put("name", entity.name)
            entity.ownerOrganizationId?.let { eObj.put("ownerOrganizationId", it) }
            entity.serverIp?.let { eObj.put("serverIp", it) }
            eObj.put("securityLevel", entity.securityLevel)
            eObj.put("contentType", entity.contentType.name)
            eObj.put("lastUpdated", entity.lastUpdated)

            val pArr = JSONArray()
            for (page in entity.pages) {
                val pObj = JSONObject()
                pObj.put("id", page.id)
                pObj.put("slug", page.slug)
                pObj.put("title", page.title)
                pObj.put("sectionName", page.sectionName)
                pObj.put("content", page.content)
                pArr.put(pObj)
            }
            eObj.put("pages", pArr)

            val histArr = JSONArray()
            entity.eventHistory.forEach { histArr.put(it) }
            eObj.put("eventHistory", histArr)

            entitiesArr.put(eObj)
        }

        root.put("webEntities", entitiesArr)
        return root.toString()
    }

    fun deserializeFromJson(json: String) {
        try {
            val root = JSONObject(json)
            if (root.has("webEntities")) {
                val arr = root.getJSONArray("webEntities")
                val map = mutableMapOf<String, WebEntity>()

                for (i in 0 until arr.length()) {
                    val eObj = arr.getJSONObject(i)
                    val domain = eObj.getString("domain")
                    val name = eObj.getString("name")
                    val ownerId = if (eObj.has("ownerOrganizationId")) eObj.getString("ownerOrganizationId") else null
                    val serverIp = if (eObj.has("serverIp")) eObj.getString("serverIp") else null
                    val secLevel = eObj.optInt("securityLevel", 1)
                    val cType = try { WebEntityType.valueOf(eObj.optString("contentType", "CORPORATE")) } catch (_: Exception) { WebEntityType.CORPORATE }
                    val lastUpdated = eObj.optLong("lastUpdated", System.currentTimeMillis())

                    val pArr = eObj.getJSONArray("pages")
                    val pages = mutableListOf<WebPage>()
                    for (j in 0 until pArr.length()) {
                        val pObj = pArr.getJSONObject(j)
                        pages.add(
                            WebPage(
                                id = pObj.getString("id"),
                                slug = pObj.getString("slug"),
                                title = pObj.getString("title"),
                                sectionName = pObj.getString("sectionName"),
                                content = pObj.getString("content")
                            )
                        )
                    }

                    val histArr = if (eObj.has("eventHistory")) eObj.getJSONArray("eventHistory") else JSONArray()
                    val history = mutableListOf<String>()
                    for (k in 0 until histArr.length()) {
                        history.add(histArr.getString(k))
                    }

                    map[domain.lowercase()] = WebEntity(
                        domain = domain,
                        name = name,
                        ownerOrganizationId = ownerId,
                        serverIp = serverIp,
                        securityLevel = secLevel,
                        contentType = cType,
                        pages = pages,
                        eventHistory = history,
                        lastUpdated = lastUpdated
                    )
                }

                _webEntities.value = map
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
