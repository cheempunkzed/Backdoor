package com.example.backdoor.terminal.commands

import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class TouchCommand : Command {
    override val name: String = "touch"
    override val description: String = "Create an empty file or update timestamp"
    override val usage: String = "touch <file_name>"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        if (args.isEmpty()) {
            return CommandResult(error = "touch: missing file operand")
        }
        val target = args.first()
        val parentPath = if (target.contains("/")) target.substringBeforeLast('/', context.vfs.getCwd()) else context.vfs.getCwd()
        val fileName = target.substringAfterLast('/')

        val existingNode = context.vfs.getNode(context.vfs.resolvePath(target))
        if (existingNode != null) {
            return CommandResult(output = "Updated timestamp for: ${existingNode.path}")
        }

        val success = context.vfs.createFile(parentPath, fileName, "")
        return if (success) {
            CommandResult(output = "Created file: ${context.vfs.resolvePath(target)}")
        } else {
            CommandResult(error = "touch: cannot create file '$target': Path invalid or access denied")
        }
    }
}
