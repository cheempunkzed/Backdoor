package com.example.backdoor.terminal.commands

import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class CatCommand : Command {
    override val name: String = "cat"
    override val description: String = "Read and display file contents"
    override val usage: String = "cat <file_path>"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        if (args.isEmpty()) {
            return CommandResult(error = "cat: missing file operand")
        }
        val path = args.first()
        val content = context.vfs.readFile(path)
        return if (content != null) {
            CommandResult(output = content)
        } else {
            CommandResult(error = "cat: $path: No such file or directory")
        }
    }
}
