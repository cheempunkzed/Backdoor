package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

/**
 * Displays active network connections and listening ports.
 */
class NetstatCommand : Command {
    override val name: String = "netstat"
    override val category: CommandCategory = CommandCategory.NETWORK
    override val descriptionKey: StringKey = StringKey.NETSTAT_DESC
    override val usage: String = "netstat [-a]"
    override val manPage: ManPage = ManPage(
        name = "netstat",
        synopsis = "netstat [-a]",
        description = "Print network connections, routing tables, interface statistics.",
        options = listOf("-a" to "Show all listening and active sockets"),
        examples = listOf("netstat", "netstat -a")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val engine = context.networkEngine
            ?: return CommandResult(error = "netstat: AbyssNet subsystem unavailable", exitCode = 1)

        val lines = engine.getNetstat()

        return CommandResult(output = lines.joinToString("\n"))
    }
}
