package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandAction
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

class ClearCommand : Command {
    override val name: String = "clear"
    override val aliases: List<String> = listOf("cls")
    override val category: CommandCategory = CommandCategory.SYSTEM
    override val descriptionKey: StringKey = StringKey.CLEAR_DESC
    override val usage: String = "clear"
    override val manPage: ManPage = ManPage(
        name = "clear",
        synopsis = "clear",
        description = "Clears your terminal screen and output buffer.",
        options = emptyList(),
        examples = listOf("clear")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        return CommandResult(action = CommandAction.ClearScreen)
    }
}

class EchoCommand : Command {
    override val name: String = "echo"
    override val category: CommandCategory = CommandCategory.SYSTEM
    override val descriptionKey: StringKey = StringKey.ECHO_DESC
    override val usage: String = "echo [text...]"
    override val manPage: ManPage = ManPage(
        name = "echo",
        synopsis = "echo [string...]",
        description = "Displays a line of text or environment variable value to standard output.",
        options = emptyList(),
        examples = listOf("echo Hello World", "echo \$USER", "echo \$PATH")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val text = parsed.positionalArgs.joinToString(" ")
        val expanded = context.session.expandVariables(text)
        return CommandResult(output = expanded)
    }
}
