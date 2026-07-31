package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

class PwdCommand : Command {
    override val name: String = "pwd"
    override val category: CommandCategory = CommandCategory.FILESYSTEM
    override val descriptionKey: StringKey = StringKey.PWD_DESC
    override val usage: String = "pwd"
    override val manPage: ManPage = ManPage(
        name = "pwd",
        synopsis = "pwd",
        description = "Print full filename/path of the current working directory.",
        options = emptyList(),
        examples = listOf("pwd")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        return CommandResult(output = context.vfs.getCwd())
    }
}

class CdCommand : Command {
    override val name: String = "cd"
    override val category: CommandCategory = CommandCategory.FILESYSTEM
    override val descriptionKey: StringKey = StringKey.CD_DESC
    override val usage: String = "cd [directory_path]"
    override val manPage: ManPage = ManPage(
        name = "cd",
        synopsis = "cd [dir]",
        description = "Change current directory to specified path. If no path is given, changes to user home directory.",
        options = emptyList(),
        examples = listOf("cd /home/operator", "cd ..", "cd ~")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val targetPath = if (parsed.positionalArgs.isEmpty()) {
            context.session.getEnv("HOME").ifEmpty { "/home/operator" }
        } else {
            parsed.positionalArgs.first()
        }

        val success = context.vfs.changeDirectory(targetPath)
        return if (success) {
            context.session.setEnv("PWD", context.vfs.getCwd())
            CommandResult(output = "")
        } else {
            CommandResult(error = "cd: no such file or directory: $targetPath", exitCode = 1)
        }
    }
}
