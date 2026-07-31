package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

class CatCommand : Command {
    override val name: String = "cat"
    override val category: CommandCategory = CommandCategory.FILESYSTEM
    override val descriptionKey: StringKey = StringKey.CAT_DESC
    override val usage: String = "cat <file_path...>"
    override val manPage: ManPage = ManPage(
        name = "cat",
        synopsis = "cat <file_path...>",
        description = "Concatenate FILE(s) to standard output.",
        options = emptyList(),
        examples = listOf("cat welcome.txt", "cat /system/kernel.sys")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        if (parsed.positionalArgs.isEmpty()) {
            return CommandResult(error = "cat: missing file argument", exitCode = 1)
        }

        val outputs = mutableListOf<String>()
        for (filePath in parsed.positionalArgs) {
            val fullPath = if (filePath.startsWith("/")) filePath else "${context.vfs.getCwd()}/$filePath"
            val content = context.vfs.readFile(fullPath)
            if (content == null) {
                return CommandResult(error = "cat: $filePath: No such file", exitCode = 1)
            }
            outputs.add(content)
        }

        return CommandResult(output = outputs.joinToString("\n"))
    }
}

class TouchCommand : Command {
    override val name: String = "touch"
    override val category: CommandCategory = CommandCategory.FILESYSTEM
    override val descriptionKey: StringKey = StringKey.TOUCH_DESC
    override val usage: String = "touch <file_path>"
    override val manPage: ManPage = ManPage(
        name = "touch",
        synopsis = "touch <filename>",
        description = "Update file timestamp or create empty file if it does not exist.",
        options = emptyList(),
        examples = listOf("touch note.txt", "touch /logs/custom.log")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        if (parsed.positionalArgs.isEmpty()) {
            return CommandResult(error = "touch: missing file operand", exitCode = 1)
        }

        val target = parsed.positionalArgs.first()
        val cwd = context.vfs.getCwd()
        val user = context.session.getEnv("USER").ifEmpty { "operator" }

        val created = if (target.startsWith("/")) {
            val lastSlash = target.lastIndexOf('/')
            val parent = if (lastSlash == 0) "/" else target.substring(0, lastSlash)
            val name = target.substring(lastSlash + 1)
            context.vfs.createFile(parent, name, "", owner = user)
        } else {
            context.vfs.createFile(cwd, target, "", owner = user)
        }

        return if (created) {
            CommandResult(output = "")
        } else {
            CommandResult(error = "touch: cannot create file '$target'", exitCode = 1)
        }
    }
}
