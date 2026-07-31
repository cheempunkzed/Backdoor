package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandAction
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

class ExitCommand : Command {
    override val name: String = "exit"
    override val category: CommandCategory = CommandCategory.SYSTEM
    override val descriptionKey: StringKey = StringKey.EXIT_DESC
    override val usage: String = "exit"
    override val manPage: ManPage = ManPage(
        name = "exit",
        synopsis = "exit",
        description = "Exit terminal session and close application window.",
        options = emptyList(),
        examples = listOf("exit")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        return CommandResult(action = CommandAction.ExitTerminal)
    }
}
