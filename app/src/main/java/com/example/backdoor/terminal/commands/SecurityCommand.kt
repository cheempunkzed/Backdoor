package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

/**
 * Terminal command `security` (alias `sec`, `scan`) for invoking the Offensive Security Framework.
 */
class SecurityCommand : Command {
    override val name: String = "security"
    override val category: CommandCategory = CommandCategory.NETWORK
    override val descriptionKey: StringKey = StringKey.NETSTAT_DESC
    override val aliases: List<String> = listOf("sec", "scan")
    override val usage: String = "security <subcommand> [target]"
    override val manPage: ManPage = ManPage(
        name = "security",
        synopsis = "security <modules|scan|assess|wiki|reports> [options]",
        description = "Central command interface for the AbyssOS Offensive Security Framework.",
        options = listOf(
            "modules" to "List registered security analysis modules",
            "scan <target>" to "Run host and service discovery sweep",
            "assess <target>" to "Run security clearance and vulnerability assessment",
            "wiki [query]" to "Search in-game technical knowledge database",
            "reports" to "List saved audit reports in AbyssFS"
        ),
        examples = listOf("sec modules", "sec scan aegis-corp.com", "sec assess apex-financial.net", "sec wiki Kerberos")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val subCmd = parsed.positionalArgs.firstOrNull()?.lowercase() ?: "help"

        if (subCmd == "help") {
            val helpMsg = """
                === OFFENSIVE SECURITY FRAMEWORK (v0.7.0) ===
                Usage: security <subcommand> [options]
                Aliases: sec, scan
                
                Subcommands:
                  modules              List registered analysis modules
                  scan <target>        Run host and service discovery sweep
                  assess <target>      Run security clearance and vulnerability assessment
                  wiki [query]         Search in-game technical knowledge base
                  reports              List generated reports saved in AbyssFS
                
                Examples:
                  sec modules
                  sec scan aegis-corp.com
                  sec assess apex-financial.net
                  sec wiki Kerberos
            """.trimIndent()
            return CommandResult(output = helpMsg)
        }

        return when (subCmd) {
            "modules", "list" -> {
                val modules = listOf(
                    "mod-host-discovery: Host & Network Discovery [RECONNAISSANCE]",
                    "mod-service-discovery: Port & Service Diagnostics [PORT_ANALYSIS]",
                    "mod-topology-analysis: Network Topology & Routing Mapper [TOPOLOGY_ANALYSIS]",
                    "mod-security-assessment: Vulnerability & Clearance Rating [SECURITY_ASSESSMENT]"
                )
                val sb = StringBuilder()
                sb.appendLine("=== REGISTERED OFFENSIVE SECURITY MODULES (${modules.size}) ===")
                modules.forEach { sb.appendLine("  • $it") }
                CommandResult(output = sb.toString().trimEnd())
            }

            "scan" -> {
                val target = parsed.positionalArgs.getOrNull(1) ?: "router.local"
                CommandResult(output = "[SEC ENGINE] Service discovery task launched for target: $target. Analysis in progress... Check Network App or /home/operator/reports/.")
            }

            "assess" -> {
                val target = parsed.positionalArgs.getOrNull(1) ?: "aegis-corp.com"
                CommandResult(output = "[SEC ENGINE] Security assessment audit launched for target: $target. Score metrics logged to Framework.")
            }

            "wiki" -> {
                val query = parsed.positionalArgs.drop(1).joinToString(" ")
                CommandResult(output = "=== ABYSSOS KNOWLEDGE DATABASE ===\nQuery: \"$query\"\nFor interactive article reading, browse 'wiki.abyss' in Abyss Browser.")
            }

            "reports" -> {
                val reports = listOf(
                    "sec-rep-aegis.md: Security Audit Report - aegis-corp.com [Score: 88/100]",
                    "sec-rep-apex.md: Security Audit Report - apex-financial.net [Score: 92/100]"
                )
                val sb = StringBuilder()
                sb.appendLine("=== SAVED SECURITY AUDIT REPORTS (${reports.size}) ===")
                reports.forEach { sb.appendLine("  • $it") }
                CommandResult(output = sb.toString().trimEnd())
            }

            else -> {
                val target = parsed.positionalArgs.first()
                CommandResult(output = "[SEC ENGINE] Launched security sweep against $target. Check Network App for results.")
            }
        }
    }
}
