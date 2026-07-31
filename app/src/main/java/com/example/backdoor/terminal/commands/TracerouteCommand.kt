package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

/**
 * Traces packet route hops to a destination network target.
 */
class TracerouteCommand : Command {
    override val name: String = "traceroute"
    override val category: CommandCategory = CommandCategory.NETWORK
    override val descriptionKey: StringKey = StringKey.TRACEROUTE_DESC
    override val usage: String = "traceroute <host>"
    override val manPage: ManPage = ManPage(
        name = "traceroute",
        synopsis = "traceroute <target>",
        description = "Prints the route packets trace to network host.",
        options = emptyList(),
        examples = listOf("traceroute 192.168.1.1", "traceroute abyss.net")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val target = parsed.positionalArgs.firstOrNull()
            ?: return CommandResult(error = "traceroute: missing target host", exitCode = 1)
        val engine = context.networkEngine
            ?: return CommandResult(error = "traceroute: AbyssNet subsystem unavailable", exitCode = 1)

        val lines = engine.traceRoute(target)

        return CommandResult(output = lines.joinToString("\n"))
    }
}
