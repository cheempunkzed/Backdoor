package com.example.backdoor.network.resolver

import com.example.backdoor.network.models.NetworkNode

/**
 * Provides DNS resolution mapping domain names / hostnames to IP addresses and vice versa.
 */
class DomainResolver {

    private val domainToIpMap = mutableMapOf<String, String>()
    private val ipToDomainMap = mutableMapOf<String, String>()

    init {
        // Default system mappings
        registerMapping("localhost", "127.0.0.1")
        registerMapping("router.local", "192.168.1.1")
        registerMapping("gateway.local", "192.168.1.1")
        registerMapping("about:network", "127.0.0.1")
        registerMapping("abyss.net", "10.0.0.1")
        registerMapping("darknet.local", "172.16.0.1")
    }

    /**
     * Registers a domain name to IP mapping.
     */
    fun registerMapping(domain: String, ip: String) {
        val cleanDomain = domain.lowercase().trim()
        domainToIpMap[cleanDomain] = ip
        ipToDomainMap[ip] = cleanDomain
    }

    /**
     * Registers a node's hostname and IP.
     */
    fun registerNode(node: NetworkNode) {
        registerMapping(node.hostname, node.ip)
        registerMapping("${node.hostname}.local", node.ip)
    }

    /**
     * Resolves domain name or hostname to IPv4 string. Returns null if unresolved.
     */
    fun resolveDomain(domain: String): String? {
        val cleanDomain = domain.lowercase().trim()
        return domainToIpMap[cleanDomain] ?: if (cleanDomain.matches(Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))) cleanDomain else null
    }

    /**
     * Performs reverse DNS lookup (IP -> Hostname).
     */
    fun reverseResolve(ip: String): String? {
        return ipToDomainMap[ip]
    }

    /**
     * Returns a map of all registered DNS records.
     */
    fun getAllRecords(): Map<String, String> {
        return domainToIpMap.toMap()
    }
}
