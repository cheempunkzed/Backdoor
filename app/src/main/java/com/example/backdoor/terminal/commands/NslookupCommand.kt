package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

/**
 * Performs DNS domain name queries.
 */
class NslookupCommand : Command {
    override val name: String = "nslookup"
    override val category: CommandCategory = CommandCategory.NETWORK
    override val descriptionKey: StringKey = StringKey.NSLOOKUP_DESC
    override val usage: String = "nslookup <domain|ip>"
    override val manPage: ManPage = ManPage(
        name = "nslookup",
        synopsis = "nslookup <domain|ip>",
        description = "Query Internet name servers interactively or non-interactively.",
        options = emptyList(),
        examples = listOf("nslookup router.local", "nslookup 192.168.1.1")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val query = parsed.positionalArgs.firstOrNull()
            ?: return CommandResult(error = "nslookup: missing domain/IP query", exitCode = 1)
        val engine = context.networkEngine
            ?: return CommandResult(error = "nslookup: AbyssNet subsystem unavailable", exitCode = 1)

        val resolvedIp = engine.domainResolver.resolveDomain(query)
        val outputs = mutableListOf(
            "Server:    192.168.1.254",
            "Address:   192.168.1.254#53",
            ""
        )

        if (resolvedIp != null) {
            outputs.add("Name:    $query")
            outputs.add("Address: $resolvedIp")
        } else {
            return CommandResult(error = "** server can't find $query: NXDOMAIN", exitCode = 1)
        }

        return CommandResult(output = outputs.joinToString("\n"))
    }
}
