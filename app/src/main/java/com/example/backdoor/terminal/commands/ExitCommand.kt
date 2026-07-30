package com.example.backdoor.terminal.commands

import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandAction
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class ExitCommand : Command {
    override val name: String = "exit"
    override val description: String = "Exit current terminal session"
    override val usage: String = "exit"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        context.onExitRequested()
        return CommandResult(action = CommandAction.ExitTerminal, output = "Session terminated.")
    }
}
