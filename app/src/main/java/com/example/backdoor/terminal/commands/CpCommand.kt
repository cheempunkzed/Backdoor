package com.example.backdoor.terminal.commands

import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class CpCommand : Command {
    override val name: String = "cp"
    override val description: String = "Copy files and directories"
    override val usage: String = "cp <source> <destination>"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        val nonFlagArgs = args.filter { !it.startsWith("-") }
        if (nonFlagArgs.size < 2) {
            return CommandResult(error = "cp: missing source or destination operand")
        }
        val src = nonFlagArgs[0]
        val dst = nonFlagArgs[1]

        val success = context.vfs.copyNode(src, dst)
        return if (success) {
            CommandResult(output = "Copied '$src' -> '$dst'")
        } else {
            CommandResult(error = "cp: cannot copy '$src' to '$dst': Source not found or path error")
        }
    }
}
