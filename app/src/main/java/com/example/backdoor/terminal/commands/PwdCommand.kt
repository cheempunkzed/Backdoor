package com.example.backdoor.terminal.commands

import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class PwdCommand : Command {
    override val name: String = "pwd"
    override val description: String = "Print name of current/working directory"
    override val usage: String = "pwd"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        return CommandResult(output = context.vfs.getCwd())
    }
}
