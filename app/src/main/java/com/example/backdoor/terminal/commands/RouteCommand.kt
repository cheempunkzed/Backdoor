package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

/**
 * Displays or manages IP routing table information.
 */
class RouteCommand : Command {
    override val name: String = "route"
    override val category: CommandCategory = CommandCategory.NETWORK
    override val descriptionKey: StringKey = StringKey.ROUTE_DESC
    override val usage: String = "route [print]"
    override val manPage: ManPage = ManPage(
        name = "route",
        synopsis = "route [print]",
        description = "Displays the IP routing table entries.",
        options = emptyList(),
        examples = listOf("route", "route print")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val engine = context.networkEngine
            ?: return CommandResult(error = "route: AbyssNet subsystem unavailable", exitCode = 1)

        val lines = engine.getRouteTable()
        return CommandResult(output = lines.joinToString("\n"))
    }
}
