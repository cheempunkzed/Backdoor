package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

/**
 * Executes ICMP echo requests against a network target.
 */
class PingCommand : Command {
    override val name: String = "ping"
    override val category: CommandCategory = CommandCategory.NETWORK
    override val descriptionKey: StringKey = StringKey.PING_DESC
    override val usage: String = "ping <host> [-c count]"
    override val manPage: ManPage = ManPage(
        name = "ping",
        synopsis = "ping <target> [-c count]",
        description = "Sends ICMP ECHO_REQUEST packets to network hosts.",
        options = listOf("-c count" to "Stop after sending count ECHO_REQUEST packets"),
        examples = listOf("ping 192.168.1.1", "ping router.local -c 4")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val target = parsed.positionalArgs.firstOrNull()
            ?: return CommandResult(error = "ping: missing target host", exitCode = 1)
        val engine = context.networkEngine
            ?: return CommandResult(error = "ping: AbyssNet subsystem unavailable", exitCode = 1)

        val count = parsed.options["c"]?.toIntOrNull() ?: 4
        val lines = engine.ping(target, count)

        return CommandResult(output = lines.joinToString("\n"))
    }
}
