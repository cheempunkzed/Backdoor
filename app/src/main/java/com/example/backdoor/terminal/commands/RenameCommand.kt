package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

class RenameCommand : Command {
    override val name: String = "rename"
    override val category: CommandCategory = CommandCategory.FILESYSTEM
    override val descriptionKey: StringKey = StringKey.RENAME_DESC
    override val usage: String = "rename <target_path> <new_name>"
    override val manPage: ManPage = ManPage(
        name = "rename",
        synopsis = "rename <path> <new_name>",
        description = "Renames a specified file or directory in place.",
        options = emptyList(),
        examples = listOf("rename document.txt archive.txt")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        if (parsed.positionalArgs.size < 2) {
            return CommandResult(error = "rename: missing arguments. Usage: rename <path> <new_name>", exitCode = 1)
        }

        val target = parsed.positionalArgs[0]
        val newName = parsed.positionalArgs[1]
        val cwd = context.vfs.getCwd()
        val user = context.session.getEnv("USER").ifEmpty { "operator" }

        val fullPath = if (target.startsWith("/")) target else if (cwd == "/") "/$target" else "$cwd/$target"

        val success = context.vfs.renameNode(fullPath, newName, userHandle = user)
        return if (success) {
            CommandResult(output = "")
        } else {
            CommandResult(error = "rename: failed to rename '$target' to '$newName'", exitCode = 1)
        }
    }
}
