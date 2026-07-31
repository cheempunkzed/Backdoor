package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

/**
 * Searches WHOIS database for domain registration records.
 */
class WhoisCommand : Command {
    override val name: String = "whois"
    override val category: CommandCategory = CommandCategory.NETWORK
    override val descriptionKey: StringKey = StringKey.WHOIS_DESC
    override val usage: String = "whois <domain>"
    override val manPage: ManPage = ManPage(
        name = "whois",
        synopsis = "whois <domain>",
        description = "Client for the WHOIS directory service.",
        options = emptyList(),
        examples = listOf("whois router.local", "whois abyss.net")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val query = parsed.positionalArgs.firstOrNull()
            ?: return CommandResult(error = "whois: missing domain argument", exitCode = 1)
        val engine = context.networkEngine
            ?: return CommandResult(error = "whois: AbyssNet subsystem unavailable", exitCode = 1)

        val node = engine.repository.getNodeByHostname(query)
            ?: engine.domainResolver.resolveDomain(query)?.let { engine.repository.getNodeByIp(it) }

        if (node == null) {
            return CommandResult(error = "No match for domain \"$query\" in WHOIS database.", exitCode = 1)
        }

        val outputs = listOf(
            "Domain Name: ${node.hostname.uppercase()}",
            "Registry Domain ID: ABYSS-REG-${node.id}",
            "Registrar WHOIS Server: whois.abyss.net",
            "Updated Date: 2026-07-31T00:00:00Z",
            "Creation Date: 2025-01-01T00:00:00Z",
            "Registrant Organization: ${node.ownerId}",
            "Node Type: ${node.nodeType.displayName}",
            "Security Level: Level ${node.securityLevel}",
            "Name Server: NS1.ABYSSNET.LOCAL",
            "Name Server: NS2.ABYSSNET.LOCAL",
            "DNSSEC: unsigned",
            "Status: active (online)"
        )

        return CommandResult(output = outputs.joinToString("\n"))
    }
}
