package com.example.backdoor.terminal.commands

import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class RenameCommand : Command {
    override val name: String = "rename"
    override val description: String = "Rename file or directory"
    override val usage: String = "rename <path> <new_name>"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        if (args.size < 2) {
            return CommandResult(error = "rename: missing target or new name operand")
        }
        val targetPath = args[0]
        val newName = args[1]

        val success = context.vfs.renameNode(targetPath, newName)
        return if (success) {
            CommandResult(output = "Renamed '$targetPath' -> '$newName'")
        } else {
            CommandResult(error = "rename: cannot rename '$targetPath': Target protected or name exists")
        }
    }
}
