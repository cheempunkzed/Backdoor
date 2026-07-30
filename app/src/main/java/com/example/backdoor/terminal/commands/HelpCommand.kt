package com.example.backdoor.terminal.commands

import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class HelpCommand : Command {
    override val name: String = "help"
    override val description: String = "Display list of available commands and usage"
    override val usage: String = "help [command_name]"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        if (args.isNotEmpty()) {
            val cmdName = args.first()
            val cmd = context.commandRegistry.getCommand(cmdName)
            return if (cmd != null) {
                CommandResult(output = "${cmd.name} - ${cmd.description}\nUsage: ${cmd.usage}")
            } else {
                CommandResult(error = "help: unknown command '$cmdName'")
            }
        }

        val commands = context.commandRegistry.getAllCommands()
        val sb = StringBuilder("=== AbyssOS 0.0.1 Terminal Commands ===\n")
        commands.sortedBy { it.name }.forEach { cmd ->
            val padding = " ".repeat((12 - cmd.name.length).coerceAtLeast(1))
            sb.append("${cmd.name}$padding- ${cmd.description}\n")
        }
        sb.append("\nType 'help <command>' for specific usage details.")
        return CommandResult(output = sb.toString())
    }
}
