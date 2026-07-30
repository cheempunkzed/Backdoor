package com.example.backdoor.terminal.commands

import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class MvCommand : Command {
    override val name: String = "mv"
    override val description: String = "Move or rename files and directories"
    override val usage: String = "mv <source> <destination>"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        if (args.size < 2) {
            return CommandResult(error = "mv: missing source or destination operand")
        }
        val src = args[0]
        val dst = args[1]

        val success = context.vfs.moveNode(src, dst)
        return if (success) {
            CommandResult(output = "Moved '$src' -> '$dst'")
        } else {
            CommandResult(error = "mv: cannot move '$src' to '$dst': Permission denied or path error")
        }
    }
}
