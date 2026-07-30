package com.example.backdoor.terminal.commands

import com.example.backdoor.filesystem.VFSNode
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class OpenCommand : Command {
    override val name: String = "open"
    override val description: String = "Open or inspect file"
    override val usage: String = "open <file_path>"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        if (args.isEmpty()) {
            return CommandResult(error = "open: missing file operand")
        }
        val path = args.first()
        val node = context.vfs.getNode(path)
            ?: return CommandResult(error = "open: $path: No such file or directory")

        return when (node) {
            is VFSNode.Directory -> {
                context.vfs.changeDirectory(path)
                CommandResult(output = "Changed directory to: ${context.vfs.getCwd()}")
            }
            is VFSNode.File -> {
                if (node.isExecutable) {
                    CommandResult(output = "Executing binary/script: ${node.name}\n${node.content}")
                } else {
                    CommandResult(output = "--- CONTENT OF ${node.name} ---\n${node.content}")
                }
            }
        }
    }
}
