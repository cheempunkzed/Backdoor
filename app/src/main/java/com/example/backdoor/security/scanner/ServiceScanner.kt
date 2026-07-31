package com.example.backdoor.security.scanner

import com.example.backdoor.corporate.CorporateServer
import com.example.backdoor.corporate.ServerType

data class ServiceAuditSpec(
    val serviceName: String,
    val port: Int,
    val protocol: String,
    val version: String,
    val cipherSuite: String,
    val securityLevel: Int,
    val configRating: String, // e.g. "A+", "B", "C-"
    val patchCompliance: String, // e.g. "98.5% Updated"
    val recommendations: List<String>
)

data class ServerAuditOverview(
    val serverId: String,
    val serverName: String,
    val ip: String,
    val overallScore: Int, // 0..100
    val patchLevel: String,
    val configRating: String,
    val firewallRating: String,
    val serviceSpecs: List<ServiceAuditSpec>
)

/**
 * Service diagnostics and configuration compliance evaluator.
 */
class ServiceScanner {

    fun auditServer(server: CorporateServer): ServerAuditOverview {
        val baseScore = 100 - (server.securityLevel * 8) + (10..20).random()
        val score = baseScore.coerceIn(45, 99)

        val patchLevel = when {
            score >= 90 -> "100.0% Compliant (Latest LTS Patchset)"
            score >= 75 -> "92.4% Compliant (Minor Updates Pending)"
            score >= 60 -> "81.0% Compliant (Patches Required)"
            else -> "64.2% Non-Compliant (Legacy Kernel/Packages)"
        }

        val configRating = when {
            score >= 90 -> "Grade A+ (Hardened CIS Benchmark)"
            score >= 75 -> "Grade B (Standard Corporate Profile)"
            score >= 60 -> "Grade C (Permissive Cipher Suite)"
            else -> "Grade D (Weak Cryptographic Defaults)"
        }

        val firewallRating = when {
            server.securityLevel >= 4 -> "HARDENED (Stateful Inspection & Intrusion Prevention)"
            server.securityLevel >= 2 -> "STANDARD (Ingress Rule Filtering Active)"
            else -> "PERMISSIVE (Open Gateway - Minimal Filtering)"
        }

        val specs = server.services.map { svc ->
            val cipher = if (server.securityLevel >= 3) "TLS_AES_256_GCM_SHA384" else "TLS_RSA_WITH_AES_128_CBC_SHA"
            val recs = mutableListOf<String>()

            if (svc.port == 22) recs.add("Disable root SSH password authentication")
            if (svc.port == 80) recs.add("Enforce HTTP Strict Transport Security (HSTS)")
            if (svc.port == 5432) recs.add("Restrict PostgreSQL connection listeners to loopback/VPN subnet")
            if (recs.isEmpty()) recs.add("Maintain periodic system log auditing")

            ServiceAuditSpec(
                serviceName = svc.name,
                port = svc.port,
                protocol = "TCP",
                version = "2.4.1",
                cipherSuite = cipher,
                securityLevel = server.securityLevel,
                configRating = configRating,
                patchCompliance = patchLevel,
                recommendations = recs
            )
        }

        return ServerAuditOverview(
            serverId = server.id,
            serverName = server.name,
            ip = server.ip,
            overallScore = score,
            patchLevel = patchLevel,
            configRating = configRating,
            firewallRating = firewallRating,
            serviceSpecs = specs
        )
    }
}
