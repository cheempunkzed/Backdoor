package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

class MvCommand : Command {
    override val name: String = "mv"
    override val category: CommandCategory = CommandCategory.FILESYSTEM
    override val descriptionKey: StringKey = StringKey.MV_DESC
    override val usage: String = "mv <src> <dest>"
    override val manPage: ManPage = ManPage(
        name = "mv",
        synopsis = "mv <source> <destination>",
        description = "Move or rename SOURCE to DEST, or move SOURCE(s) to DIRECTORY.",
        options = emptyList(),
        examples = listOf("mv file.txt /home/operator/Notes/", "mv old.txt new.txt")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        if (parsed.positionalArgs.size < 2) {
            return CommandResult(error = "mv: missing file operand. Usage: mv <src> <dest>", exitCode = 1)
        }

        val src = parsed.positionalArgs[0]
        val dest = parsed.positionalArgs[1]
        val cwd = context.vfs.getCwd()
        val user = context.session.getEnv("USER").ifEmpty { "operator" }

        val srcPath = if (src.startsWith("/")) src else if (cwd == "/") "/$src" else "$cwd/$src"
        val destPath = if (dest.startsWith("/")) dest else if (cwd == "/") "/$dest" else "$cwd/$dest"

        val success = context.vfs.moveNode(srcPath, destPath, userHandle = user)
        return if (success) {
            CommandResult(output = "")
        } else {
            CommandResult(error = "mv: cannot move '$src' to '$dest'", exitCode = 1)
        }
    }
}
