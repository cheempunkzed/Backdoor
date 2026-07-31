package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

class FindCommand : Command {
    override val name: String = "find"
    override val category: CommandCategory = CommandCategory.FILESYSTEM
    override val descriptionKey: StringKey = StringKey.FIND_DESC
    override val usage: String = "find [path] [query]"
    override val manPage: ManPage = ManPage(
        name = "find",
        synopsis = "find [path] [pattern]",
        description = "Search for files and directories in a directory hierarchy.",
        options = emptyList(),
        examples = listOf("find / log", "find . txt")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val rootPath = if (parsed.positionalArgs.isNotEmpty()) parsed.positionalArgs[0] else context.vfs.getCwd()
        val query = if (parsed.positionalArgs.size > 1) parsed.positionalArgs[1] else ""

        val results = context.vfs.findFiles(query, startPath = rootPath)
        if (results.isEmpty()) {
            return CommandResult(output = "No matching nodes found for '$query'.")
        }

        val formatted = results.joinToString("\n") { it.path }
        return CommandResult(output = formatted)
    }
}
