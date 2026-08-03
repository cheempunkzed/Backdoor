package com.example.backdoor.security.database

data class WikiArticle(
    val id: String,
    val title: String,
    val category: String, // "Devices", "Protocols", "Industries", "Security Metrics", "Topologies"
    val summary: String,
    val content: String,
    val tags: List<String>
)

/**
 * In-game technical Knowledge Database and Wiki for AbyssOS.
 * Contains technical documentation, network architecture guides, and infrastructure manuals.
 */
class KnowledgeDatabase {

    private val articles = mutableListOf<WikiArticle>()

    init {
        populateDefaultArticles()
    }

    private fun populateDefaultArticles() {
        // --- GAMEPLAY MANUAL & MECHANICS ---
        articles.add(
            WikiArticle(
                id = "manual-getting-started",
                title = "Getting Started: The Operator's Guide",
                category = "Manual",
                summary = "Introduction to AbyssOS and your role as a freelance operator.",
                content = """
                    # Getting Started in AbyssOS
                    
                    Welcome to AbyssOS. You are a freelance cyber-operator equipped with a state-of-the-art terminal and darknet access.
                    
                    ## Your Interface
                    - **Desktop**: The main workspace. You can open multiple applications (Terminal, Browser, Network Scanner, etc.).
                    - **Dock**: Pin frequently used apps to the bottom dock for quick access.
                    - **System Monitor**: Keep an eye on your CPU/RAM usage. Running too many intensive tasks will slow down your rig.
                    
                    ## First Steps
                    1. Open the **Terminal** and type `help` to see available commands.
                    2. Open the **Browser** and navigate to `wiki.abyss` (you are here!) or `dir.onion` to explore the network.
                    3. Launch the **DarkNet** app to monitor your reputation and active onion circuits.
                    4. Check the **Contracts** app to find freelance hacking jobs and earn Credits.
                """.trimIndent(),
                tags = listOf("Manual", "Beginner", "Interface", "Getting Started")
            )
        )

        articles.add(
            WikiArticle(
                id = "manual-hacking-101",
                title = "Hacking 101: Recon and Exploitation",
                category = "Manual",
                summary = "A comprehensive guide on how to hack servers and complete contracts.",
                content = """
                    # Hacking 101
                    
                    Hacking in AbyssOS follows a realistic workflow: Reconnaissance, Scanning, Exploitation, and Exfiltration.
                    
                    ## Step 1: Reconnaissance (Terminal)
                    Use the `whois` or `nslookup` commands on a target domain (e.g., `whois aegis-corp.com`) to find its IP address.
                    Use `ping <ip>` to verify the target is online.
                    
                    ## Step 2: Scanning
                    Use the **Network Scanner** app or terminal commands to scan the target IP for open ports.
                    Terminal: `sec scan <ip>` will run an active nmap-style scan to reveal open ports and running services (e.g., SSH on port 22, HTTP on port 80).
                    
                    ## Step 3: Assessment
                    Once you know the open ports, use `sec assess <ip>` to find known vulnerabilities on those specific services.
                    
                    ## Step 4: Exploitation & Contracts
                    When you accept a contract in the **Contracts** app, you will be given a target and an objective (e.g., steal a file, deface a website).
                    You must use the terminal and specialized tools (unlocked later) to penetrate the network, bypass firewalls, and execute your payload.
                    
                    Always remember to cover your tracks by clearing logs in `/var/log` if you gain root access!
                """.trimIndent(),
                tags = listOf("Manual", "Hacking", "Recon", "Scanning", "Exploitation")
            )
        )

        articles.add(
            WikiArticle(
                id = "manual-darknet",
                title = "The Darknet: Onion Routing and Reputation",
                category = "Manual",
                summary = "Understanding AbyssNet, onion circuits, and how to stay anonymous.",
                content = """
                    # The Darknet (AbyssNet)
                    
                    The Darknet is a decentralized network of encrypted nodes accessed via `.onion` addresses.
                    
                    ## Onion Routing
                    Your connection to any `.onion` site (like the forum or market) is routed through multiple relay nodes. This hides your real IP address.
                    You can view your active circuit in the **Browser** header or manage relays in the **DarkNet** app.
                    
                    ## Reputation System
                    As a shadow operator, your reputation is everything.
                    - **Trust**: Earned by completing contracts successfully and interacting positively on the forum.
                    - **Fame**: Earned by completing high-profile hacks or selling valuable exploits on the market.
                    
                    Higher reputation unlocks access to exclusive hidden services, advanced hacking tools, and more lucrative contracts.
                    
                    ## Key Hidden Services
                    - `dir.onion`: The hidden service directory. Start here to find other nodes.
                    - `abyss-forum.onion`: The underground forum. Socialize, buy/sell 0-days, and gather intel.
                    - `darkmarket.onion`: The shadow exchange for hardware and exploit modules.
                """.trimIndent(),
                tags = listOf("Manual", "Darknet", "Onion", "Reputation", "Anonymity")
            )
        )
        
        articles.add(
            WikiArticle(
                id = "manual-terminal",
                title = "Terminal Commands Reference",
                category = "Manual",
                summary = "A quick reference guide for essential terminal commands.",
                content = """
                    # Terminal Commands Reference
                    
                    Your terminal is your most powerful tool. Here are the core commands:
                    
                    ## File System Navigation
                    - `ls [dir]`: List directory contents.
                    - `cd <dir>`: Change current directory.
                    - `pwd`: Print working directory.
                    
                    ## File Operations
                    - `cat <file>`: Read file contents.
                    - `mkdir <dir>`: Create a new directory.
                    - `cp <src> <dest>`: Copy a file or directory.
                    - `mv <src> <dest>`: Move or rename a file.
                    - `rm <file>`: Remove a file.
                    - `chmod <perms> <file>`: Change file permissions.
                    
                    ## Networking & Security
                    - `ping <host>`: Check connectivity to a host.
                    - `nslookup <domain>`: Resolve domain name to IP.
                    - `whois <domain>`: Get WHOIS registration info.
                    - `netstat`: List active network connections.
                    - `sec scan <ip>`: Run a port scan on a target.
                    - `sec assess <ip>`: Assess vulnerabilities on a target.
                    
                    ## System
                    - `history`: View command history.
                    - `clear`: Clear the terminal screen.
                """.trimIndent(),
                tags = listOf("Manual", "Terminal", "Commands", "CLI")
            )
        )

        // --- ORIGINAL TECHNICAL ARTICLES ---
        articles.add(
            WikiArticle(
                id = "dev-core-router",
                title = "Backbone Core Router Architecture",
                category = "Devices",
                summary = "High-throughput packet routing engines operating at the network core.",
                content = """
                    # Backbone Core Router Architecture
                    
                    Core Routers form the fundamental switching backbone of corporate subnets and ISP backbones in AbyssNet.
                    
                    ## Key Features
                    - **Packet Forwarding Rate**: 100+ Gbps hardware switching fabric.
                    - **BGP / OSPF Dynamic Routing**: Auto-discovers neighbor subnets and optimizes autonomous system paths.
                    - **ACL Security Rules**: Ingress and egress access control list enforcement.
                    - **Default Gateway Services**: Standard IPv4 gateway (`10.x.x.1` or `192.168.1.1`).
                    
                    ## Security Considerations
                    Core routers must be protected against ICMP flood conditions and unauthorized administrative SSH access.
                """.trimIndent(),
                tags = listOf("Router", "BGP", "Subnet", "Gateway", "Networking")
            )
        )

        articles.add(
            WikiArticle(
                id = "dev-directory-server",
                title = "Kerberos & LDAP Directory Server",
                category = "Devices",
                summary = "Centralized identity provider and single-sign-on gateway for corporate domains.",
                content = """
                    # Kerberos & LDAP Directory Server
                    
                    Directory Servers manage corporate domain user tickets, group policies, and identity access control.
                    
                    ## System Roles
                    - **Kerberos Key Distribution Center (KDC)**: Port 88 authentication service.
                    - **LDAP / Active Directory Directory Service**: Port 389 / 636 encrypted object query engine.
                    - **Domain Controller Sync**: Replicates authorization tickets across data centers.
                    
                    ## Security Assessment
                    Directory servers are high-priority assets (Security Tier 4 or 5). Compromising directory tickets grants enterprise-wide domain administrative privileges.
                """.trimIndent(),
                tags = listOf("Kerberos", "LDAP", "Directory", "Authentication", "Security Tier 5")
            )
        )

        articles.add(
            WikiArticle(
                id = "proto-postgresql",
                title = "PostgreSQL Enterprise Relational Database",
                category = "Protocols",
                summary = "Multi-version concurrency control relational storage engine.",
                content = """
                    # PostgreSQL Enterprise Relational Database
                    
                    PostgreSQL operates on standard TCP Port 5432 and powers mission-critical financial, genomic, and analytics storage.
                    
                    ## Architecture Specs
                    - **Transport Layer**: Encrypted SSL/TLS connection streams.
                    - **Role Permissions**: Fine-grained table schema grants and column-level encryption.
                    - **Replication**: Synchronous physical streaming replication to cold backup vaults.
                    
                    ## Hardening Guidelines
                    - Restrict `pg_hba.conf` listening addresses exclusively to authorized internal microservice subnets.
                    - Enforce SCRAM-SHA-256 password hashing protocols.
                """.trimIndent(),
                tags = listOf("PostgreSQL", "Database", "Port 5432", "Storage", "SQL")
            )
        )

        articles.add(
            WikiArticle(
                id = "proto-wireguard",
                title = "WireGuard Stealth VPN Tunnel",
                category = "Protocols",
                summary = "Modern UDP-based crypto VPN protocol utilizing Noise protocol framework.",
                content = """
                    # WireGuard Stealth VPN Tunneling
                    
                    WireGuard operates on UDP Port 1194/51820 and provides high-speed encrypted peer-to-peer tunnels across data centers.
                    
                    ## Cryptographic Primitives
                    - **Curve25519**: Elliptic-curve key exchange.
                    - **ChaCha20**: Symmetric stream encryption.
                    - **Poly1305**: Message authentication code.
                    
                    ## Operational Impact
                    WireGuard tunnels hide internal routing topologies from external traceroute probes.
                """.trimIndent(),
                tags = listOf("VPN", "WireGuard", "Encryption", "Tunneling", "UDP")
            )
        )

        articles.add(
            WikiArticle(
                id = "sec-clearance-tiers",
                title = "Corporate Security Clearance Tiers",
                category = "Security Metrics",
                summary = "Classification framework for corporate server nodes and data center facilities.",
                content = """
                    # Corporate Security Clearance Tiers (1 to 5)
                    
                    AbyssOS classifies all network infrastructure into 5 security clearance tiers:
                    
                    - **Tier 1 (Public / Unclassified)**: Web portals, public DNS servers, media relays.
                    - **Tier 2 (Internal Business)**: Mail gateways, file shares, employee intranet.
                    - **Tier 3 (Confidential Enterprise)**: Internal microservice API mesh, staging databases.
                    - **Tier 4 (Restricted Financial / R&D)**: Core transaction databases, source code vaults.
                    - **Tier 5 (Top Secret / Military Grade)**: Kerberos KDC, quantum research nodes, air-gapped backup vaults.
                """.trimIndent(),
                tags = listOf("Security Tier", "Clearance", "Hardening", "Policy")
            )
        )

        articles.add(
            WikiArticle(
                id = "topo-hierarchical-tree",
                title = "Hierarchical Enterprise Tree Topology",
                category = "Topologies",
                summary = "Multi-tiered distribution architecture separating core, aggregation, and access switches.",
                content = """
                    # Hierarchical Enterprise Tree Topology
                    
                    Enterprise networks organize servers into logical tree tiers to optimize packet latency and isolate broadcast domains.
                    
                    ## Tiers
                    1. **Core Layer**: High-speed backbone routers.
                    2. **Distribution Layer**: Firewalls and VPN load balancers enforcing policy.
                    3. **Access Layer**: Server rack switches directly connected to hardware nodes.
                """.trimIndent(),
                tags = listOf("Topology", "Tree", "Hierarchy", "Architecture")
            )
        )
    }

    fun getAllArticles(): List<WikiArticle> = articles

    fun searchArticles(query: String): List<WikiArticle> {
        if (query.isBlank()) return articles
        val clean = query.lowercase().trim()
        return articles.filter {
            it.title.lowercase().contains(clean) ||
                it.summary.lowercase().contains(clean) ||
                it.category.lowercase().contains(clean) ||
                it.tags.any { tag -> tag.lowercase().contains(clean) }
        }
    }

    fun getArticlesByCategory(category: String): List<WikiArticle> {
        return articles.filter { it.category.equals(category, ignoreCase = true) }
    }

    fun getArticleById(id: String): WikiArticle? {
        return articles.find { it.id == id }
    }
}
