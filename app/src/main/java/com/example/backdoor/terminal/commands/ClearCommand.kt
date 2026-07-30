package com.example.backdoor.terminal.commands

import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandAction
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class ClearCommand : Command {
    override val name: String = "clear"
    override val description: String = "Clear the terminal screen output"
    override val usage: String = "clear"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        return CommandResult(action = CommandAction.ClearScreen)
    }
}
