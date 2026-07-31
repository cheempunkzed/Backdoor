package com.example.backdoor.security.reports

import com.example.backdoor.filesystem.VirtualFileSystem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SecurityReport(
    val id: String,
    val title: String,
    val target: String,
    val timestamp: Long,
    val author: String = "Operator",
    val securityScore: Int,
    val hostsFound: Int,
    val openPortsFound: Int,
    val patchLevel: String,
    val firewallRating: String,
    val markdownContent: String,
    val filePath: String
)

/**
 * Generator for technical security audit reports saved directly into AbyssFS.
 */
class SecurityReportGenerator(
    private val vfs: VirtualFileSystem
) {

    fun generateAndSaveReport(
        targetName: String,
        targetIpOrSubnet: String,
        author: String = "Operator",
        securityScore: Int,
        hostsFound: Int,
        openPortsFound: Int,
        patchLevel: String,
        firewallRating: String,
        detailsMap: Map<String, Any>
    ): SecurityReport {
        val timestamp = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val dateStr = dateFormat.format(Date(timestamp))
        val reportId = "rpt-${timestamp % 1000000}"
        val sanitizedTarget = targetName.lowercase().replace(" ", "_").replace(".", "_")
        val filePath = "/home/operator/reports/audit_${sanitizedTarget}_$reportId.md"

        val markdown = StringBuilder().apply {
            appendLine("# OFFENSIVE SECURITY AUDIT REPORT")
            appendLine("---")
            appendLine("**Report ID**: `$reportId`")
            appendLine("**Target Asset**: `$targetName ($targetIpOrSubnet)`")
            appendLine("**Audit Date**: `$dateStr UTC`")
            appendLine("**Lead Auditor**: `$author`")
            appendLine("**System Engine**: `AbyssOS 0.7.0 Offensive Security Framework`")
            appendLine()
            appendLine("## 1. Executive Summary")
            appendLine("This technical report documents the infrastructure reconnaissance and security posture assessment conducted on `$targetName`.")
            appendLine()
            appendLine("- **Overall Security Score**: `$securityScore / 100`")
            appendLine("- **Active Network Hosts**: `$hostsFound`")
            appendLine("- **Open Services / Ports**: `$openPortsFound`")
            appendLine("- **Patch Compliance**: `$patchLevel`")
            appendLine("- **Firewall Perimeter Rating**: `$firewallRating`")
            appendLine()
            appendLine("## 2. Infrastructure Inventory & Diagnostic Log")
            appendLine("```yaml")
            detailsMap.forEach { (key, value) ->
                appendLine("  $key: \"$value\"")
            }
            appendLine("```")
            appendLine()
            appendLine("## 3. Remediation & Hardening Roadmap")
            appendLine("1. **Access Control**: Enforce strict ingress ACL filters on core routers and gateway proxies.")
            appendLine("2. **Patch Management**: Apply minor kernel updates and service daemon releases.")
            appendLine("3. **Cryptographic Hardening**: Upgrade TLS cipher suites to TLS 1.3 and SCRAM-SHA-256 passwords.")
            appendLine("4. **Continuous Auditing**: Maintain weekly automated port and topology scans via `AbyssNet`.")
            appendLine()
            appendLine("---")
            appendLine("*Generated automatically by Backdoor Security Engine v0.7.0.*")
        }.toString()

        // Ensure parent folder exists and write report file to AbyssFS
        vfs.createDirectory("/home/operator", "reports", "operator")
        vfs.writeFile(filePath, markdown, author)

        return SecurityReport(
            id = reportId,
            title = "Security Audit Report - $targetName",
            target = targetIpOrSubnet,
            timestamp = timestamp,
            author = author,
            securityScore = securityScore,
            hostsFound = hostsFound,
            openPortsFound = openPortsFound,
            patchLevel = patchLevel,
            firewallRating = firewallRating,
            markdownContent = markdown,
            filePath = filePath
        )
    }
}
