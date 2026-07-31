package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

/**
 * Displays network interface configuration (ipconfig/ifconfig).
 */
class IpconfigCommand(override val name: String = "ipconfig") : Command {
    override val category: CommandCategory = CommandCategory.NETWORK
    override val descriptionKey: StringKey = StringKey.IPCONFIG_DESC
    override val usage: String = "$name [all]"
    override val manPage: ManPage = ManPage(
        name = name,
        synopsis = "$name [all]",
        description = "Displays all current TCP/IP network configuration values.",
        options = listOf("all" to "Display full configuration details"),
        examples = listOf(name, "$name all")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val engine = context.networkEngine
            ?: return CommandResult(error = "$name: AbyssNet subsystem unavailable", exitCode = 1)

        val lines = engine.getIfconfig()
        return CommandResult(output = lines.joinToString("\n"))
    }
}
