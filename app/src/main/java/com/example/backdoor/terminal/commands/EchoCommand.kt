package com.example.backdoor.terminal.commands

import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class EchoCommand : Command {
    override val name: String = "echo"
    override val description: String = "Print text or arguments to the terminal"
    override val usage: String = "echo [text...]"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        return CommandResult(output = args.joinToString(" "))
    }
}
