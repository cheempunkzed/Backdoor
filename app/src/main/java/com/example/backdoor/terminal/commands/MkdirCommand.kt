package com.example.backdoor.terminal.commands

import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class MkdirCommand : Command {
    override val name: String = "mkdir"
    override val description: String = "Create a directory"
    override val usage: String = "mkdir <directory_name>"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        if (args.isEmpty()) {
            return CommandResult(error = "mkdir: missing operand")
        }
        val target = args.first()
        val parentPath = if (target.contains("/")) target.substringBeforeLast('/', context.vfs.getCwd()) else context.vfs.getCwd()
        val dirName = target.substringAfterLast('/')

        val success = context.vfs.createDirectory(parentPath, dirName)
        return if (success) {
            CommandResult(output = "Created directory: ${context.vfs.resolvePath(target)}")
        } else {
            CommandResult(error = "mkdir: cannot create directory '$target': File or directory exists or path invalid")
        }
    }
}
