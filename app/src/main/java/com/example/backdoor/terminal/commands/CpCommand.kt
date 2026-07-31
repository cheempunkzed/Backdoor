package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

class CpCommand : Command {
    override val name: String = "cp"
    override val category: CommandCategory = CommandCategory.FILESYSTEM
    override val descriptionKey: StringKey = StringKey.CP_DESC
    override val usage: String = "cp [-r] <src> <dest>"
    override val manPage: ManPage = ManPage(
        name = "cp",
        synopsis = "cp [-r] <source> <destination>",
        description = "Copy SOURCE to DEST, or multiple SOURCE(s) to DIRECTORY.",
        options = listOf("-r" to "Copy directories recursively"),
        examples = listOf("cp config.cfg config.bak", "cp -r /logs /tmp/logs_backup")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        if (parsed.positionalArgs.size < 2) {
            return CommandResult(error = "cp: missing file operand. Usage: cp <src> <dest>", exitCode = 1)
        }

        val src = parsed.positionalArgs[0]
        val dest = parsed.positionalArgs[1]
        val cwd = context.vfs.getCwd()
        val user = context.session.getEnv("USER").ifEmpty { "operator" }

        val srcPath = if (src.startsWith("/")) src else if (cwd == "/") "/$src" else "$cwd/$src"
        val destPath = if (dest.startsWith("/")) dest else if (cwd == "/") "/$dest" else "$cwd/$dest"

        val success = context.vfs.copyNode(srcPath, destPath, userHandle = user)
        return if (success) {
            CommandResult(output = "")
        } else {
            CommandResult(error = "cp: cannot copy '$src' to '$dest'", exitCode = 1)
        }
    }
}
