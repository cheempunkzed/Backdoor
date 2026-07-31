package com.example.backdoor.terminal.commands

import com.example.backdoor.filesystem.*
import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

class StatCommand : Command {
    override val name: String = "stat"
    override val category: CommandCategory = CommandCategory.FILESYSTEM
    override val descriptionKey: StringKey = StringKey.STAT_DESC
    override val usage: String = "stat <path>"
    override val manPage: ManPage = ManPage(
        name = "stat",
        synopsis = "stat <path>",
        description = "Display complete file or file system node status metadata.",
        options = emptyList(),
        examples = listOf("stat welcome.txt", "stat /logs")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        if (parsed.positionalArgs.isEmpty()) {
            return CommandResult(error = "stat: missing operand", exitCode = 1)
        }

        val target = parsed.positionalArgs.first()
        val cwd = context.vfs.getCwd()
        val fullPath = if (target.startsWith("/")) target else if (cwd == "/") "/$target" else "$cwd/$target"

        val node = context.vfs.getNode(fullPath)
            ?: return CommandResult(error = "stat: cannot stat '$target': No such file or directory", exitCode = 1)

        val typeStr = if (node.isDirectory) "directory" else "regular file"
        val sizeStr = "${node.size} bytes"
        val output = """
            File: ${node.name}
            Path: ${node.path}
            Type: $typeStr
            Size: $sizeStr
            Owner: ${node.owner}
            Protected: ${node.isProtected}
            Executable: ${node.isExecutable}
            Created: ${node.createdTime}
            Modified: ${node.modifiedTime}
        """.trimIndent()

        return CommandResult(output = output)
    }
}
