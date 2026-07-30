package com.example.backdoor.terminal.commands

import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class CdCommand : Command {
    override val name: String = "cd"
    override val description: String = "Change working directory"
    override val usage: String = "cd <directory_path>"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        val targetPath = if (args.isNotEmpty()) args.first() else "~"
        val success = context.vfs.changeDirectory(targetPath)
        return if (success) {
            CommandResult(output = context.vfs.getCwd())
        } else {
            CommandResult(error = "cd: no such file or directory: $targetPath")
        }
    }
}
