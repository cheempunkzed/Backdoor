package com.example.backdoor.terminal.commands

import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class FindCommand : Command {
    override val name: String = "find"
    override val description: String = "Search for files and directories"
    override val usage: String = "find [start_path] <query>"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        if (args.isEmpty()) {
            return CommandResult(error = "find: missing search query")
        }

        val startPath: String
        val query: String

        if (args.size == 1) {
            startPath = "/"
            query = args.first()
        } else {
            startPath = args[0]
            query = args[1]
        }

        val results = context.vfs.findFiles(query, startPath)
        if (results.isEmpty()) {
            return CommandResult(output = "No matches found for query: '$query'")
        }

        val sb = StringBuilder()
        results.forEach { node ->
            sb.append("${node.path} [${node.metadata.fileType.name}]\n")
        }
        return CommandResult(output = sb.toString().trimEnd())
    }
}
