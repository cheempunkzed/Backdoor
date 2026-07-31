package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.i18n.StringManager
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

class HelpCommand : Command {
    override val name: String = "help"
    override val aliases: List<String> = listOf("?", "commands")
    override val category: CommandCategory = CommandCategory.SYSTEM
    override val descriptionKey: StringKey = StringKey.HELP_DESC
    override val usage: String = "help [command]"
    override val manPage: ManPage = ManPage(
        name = "help",
        synopsis = "help [command_name]",
        description = "Displays the complete catalog of AbyssOS commands, or detailed usage instructions for a specific command.",
        options = emptyList(),
        examples = listOf("help", "help ls", "help cd")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        if (parsed.positionalArgs.isNotEmpty()) {
            val target = parsed.positionalArgs.first()
            val cmd = context.commandRegistry.getCommand(target)
                ?: return CommandResult(error = StringManager.get(StringKey.CMD_NOT_FOUND, target), exitCode = 1)

            val desc = StringManager.get(cmd.descriptionKey)
            val output = """
                COMMAND: ${cmd.name.uppercase()} ${if (cmd.aliases.isNotEmpty()) "(Aliases: ${cmd.aliases.joinToString(", ")})" else ""}
                CATEGORY: ${cmd.category.title}
                USAGE: ${cmd.usage}
                DESCRIPTION: $desc
                MANUAL: Type 'man ${cmd.name}' for full manual page documentation.
            """.trimIndent()
            return CommandResult(output = output)
        }

        val categories = context.commandRegistry.getCommandsByCategory()
        val sb = StringBuilder()
        sb.append("=== AbyssOS 0.4.0 Terminal Core Command Catalog ===\n\n")

        categories.forEach { (cat, cmds) ->
            sb.append("${cat.title}:\n")
            cmds.sortedBy { it.name }.forEach { cmd ->
                val desc = StringManager.get(cmd.descriptionKey)
                sb.append("  ${cmd.name.padEnd(12)} - $desc\n")
            }
            sb.append("\n")
        }

        sb.append("Tip: Use 'man <command>' or 'help <command>' for detailed command options.")
        return CommandResult(output = sb.toString().trimEnd())
    }
}

class ManCommand : Command {
    override val name: String = "man"
    override val category: CommandCategory = CommandCategory.SYSTEM
    override val descriptionKey: StringKey = StringKey.MAN_DESC
    override val usage: String = "man <command>"
    override val manPage: ManPage = ManPage(
        name = "man",
        synopsis = "man <command>",
        description = "Format and display manual pages for system commands.",
        options = emptyList(),
        examples = listOf("man ls", "man cat", "man help")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        if (parsed.positionalArgs.isEmpty()) {
            return CommandResult(error = "What manual page do you want? Example: 'man ls'", exitCode = 1)
        }

        val target = parsed.positionalArgs.first()
        val cmd = context.commandRegistry.getCommand(target)
            ?: return CommandResult(error = "No manual entry for '$target'.", exitCode = 1)

        val man = cmd.manPage
        val sb = StringBuilder()
        sb.append("NAME\n")
        sb.append("       ${man.name} - ${StringManager.get(cmd.descriptionKey)}\n\n")
        sb.append("SYNOPSIS\n")
        sb.append("       ${man.synopsis}\n\n")
        sb.append("DESCRIPTION\n")
        sb.append("       ${man.description}\n\n")

        if (man.options.isNotEmpty()) {
            sb.append("OPTIONS\n")
            man.options.forEach { (opt, desc) ->
                sb.append("       ${opt.padEnd(16)} $desc\n")
            }
            sb.append("\n")
        }

        if (man.examples.isNotEmpty()) {
            sb.append("EXAMPLES\n")
            man.examples.forEach { ex ->
                sb.append("       $ $ex\n")
            }
        }

        return CommandResult(output = sb.toString().trimEnd())
    }
}
