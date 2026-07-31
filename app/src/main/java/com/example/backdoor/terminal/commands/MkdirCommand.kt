package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

class MkdirCommand : Command {
    override val name: String = "mkdir"
    override val category: CommandCategory = CommandCategory.FILESYSTEM
    override val descriptionKey: StringKey = StringKey.MKDIR_DESC
    override val usage: String = "mkdir [-p] <dir_path>"
    override val manPage: ManPage = ManPage(
        name = "mkdir",
        synopsis = "mkdir [-p] <directory>",
        description = "Create the DIRECTORY(ies), if they do not already exist.",
        options = listOf(
            "-p" to "Make parent directories as needed, no error if existing"
        ),
        examples = listOf("mkdir Projects", "mkdir -p /home/operator/nested/dir")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        if (parsed.positionalArgs.isEmpty()) {
            return CommandResult(error = "mkdir: missing operand", exitCode = 1)
        }

        val target = parsed.positionalArgs.first()
        val user = context.session.getEnv("USER").ifEmpty { "operator" }
        val cwd = context.vfs.getCwd()

        val fullPath = if (target.startsWith("/")) target else if (cwd == "/") "/$target" else "$cwd/$target"

        val lastSlash = fullPath.lastIndexOf('/')
        val parent = if (lastSlash == 0) "/" else fullPath.substring(0, lastSlash)
        val name = fullPath.substring(lastSlash + 1)

        val success = context.vfs.createDirectory(parent, name, owner = user)
        return if (success) {
            CommandResult(output = "")
        } else {
            CommandResult(error = "mkdir: cannot create directory '$target': File or directory exists", exitCode = 1)
        }
    }
}

class RmCommand : Command {
    override val name: String = "rm"
    override val category: CommandCategory = CommandCategory.FILESYSTEM
    override val descriptionKey: StringKey = StringKey.RM_DESC
    override val usage: String = "rm [-rf] <target_path>"
    override val manPage: ManPage = ManPage(
        name = "rm",
        synopsis = "rm [-r] [-f] <target>",
        description = "Remove (unlink) the FILE(s) or directory.",
        options = listOf(
            "-r" to "Remove directories and their contents recursively",
            "-f" to "Ignore nonexistent files and arguments, never prompt"
        ),
        examples = listOf("rm old.txt", "rm -rf /tmp/test")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        if (parsed.positionalArgs.isEmpty()) {
            return CommandResult(error = "rm: missing operand", exitCode = 1)
        }

        val target = parsed.positionalArgs.first()
        val force = parsed.hasFlag('f') || parsed.hasLongFlag("force")
        val cwd = context.vfs.getCwd()
        val user = context.session.getEnv("USER").ifEmpty { "operator" }

        val fullPath = if (target.startsWith("/")) target else if (cwd == "/") "/$target" else "$cwd/$target"

        val success = context.vfs.deleteNode(fullPath, permanent = true, userHandle = user)
        return if (success) {
            CommandResult(output = "")
        } else if (force) {
            CommandResult(output = "")
        } else {
            CommandResult(error = "rm: cannot remove '$target': No such file or permission denied", exitCode = 1)
        }
    }
}
