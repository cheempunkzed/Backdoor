package com.example.backdoor.terminal.commands

import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class RmCommand : Command {
    override val name: String = "rm"
    override val description: String = "Remove file or move to trash"
    override val usage: String = "rm [-r] [-f] <path>"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        if (args.isEmpty()) {
            return CommandResult(error = "rm: missing operand")
        }

        var isPermanent = false
        val pathArgs = mutableListOf<String>()

        args.forEach { arg ->
            if (arg.startsWith("-")) {
                if (arg.contains("f")) isPermanent = true
            } else {
                pathArgs.add(arg)
            }
        }

        if (pathArgs.isEmpty()) {
            return CommandResult(error = "rm: missing target path")
        }

        val targetPath = pathArgs.first()
        val node = context.vfs.getNode(targetPath)
            ?: return CommandResult(error = "rm: cannot remove '$targetPath': No such file or directory")

        if (node.metadata.isSystemProtected) {
            return CommandResult(error = "rm: cannot remove '$targetPath': Permission denied (System Protected)")
        }

        val success = context.vfs.deleteNode(targetPath, permanent = isPermanent)
        return if (success) {
            val msg = if (isPermanent) "Permanently removed: $targetPath" else "Moved to Trash: $targetPath"
            CommandResult(output = msg)
        } else {
            CommandResult(error = "rm: failed to remove '$targetPath'")
        }
    }
}
