package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

/**
 * Displays ARP table mapping IP addresses to physical MAC addresses.
 */
class ArpCommand : Command {
    override val name: String = "arp"
    override val category: CommandCategory = CommandCategory.NETWORK
    override val descriptionKey: StringKey = StringKey.ARP_DESC
    override val usage: String = "arp [-a]"
    override val manPage: ManPage = ManPage(
        name = "arp",
        synopsis = "arp [-a]",
        description = "Displays the ARP cache table.",
        options = listOf("-a" to "Display current ARP entries"),
        examples = listOf("arp", "arp -a")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val engine = context.networkEngine
            ?: return CommandResult(error = "arp: AbyssNet subsystem unavailable", exitCode = 1)

        val lines = engine.getArpTable()

        return CommandResult(output = lines.joinToString("\n"))
    }
}
