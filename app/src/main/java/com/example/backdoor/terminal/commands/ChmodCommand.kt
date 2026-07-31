package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

class ChmodCommand : Command {
    override val name: String = "chmod"
    override val category: CommandCategory = CommandCategory.FILESYSTEM
    override val descriptionKey: StringKey = StringKey.CHMOD_DESC
    override val usage: String = "chmod <mode> <path>"
    override val manPage: ManPage = ManPage(
        name = "chmod",
        synopsis = "chmod <mode> <file>",
        description = "Change file mode bits / permissions for specified node.",
        options = emptyList(),
        examples = listOf("chmod 755 /system/kernel.sys", "chmod +x script.sh")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        if (parsed.positionalArgs.size < 2) {
            return CommandResult(error = "chmod: missing operand. Usage: chmod <mode> <path>", exitCode = 1)
        }

        val mode = parsed.positionalArgs[0]
        val target = parsed.positionalArgs[1]
        val cwd = context.vfs.getCwd()

        val fullPath = if (target.startsWith("/")) target else if (cwd == "/") "/$target" else "$cwd/$target"
        val node = context.vfs.getNode(fullPath)
            ?: return CommandResult(error = "chmod: cannot access '$target': No such file", exitCode = 1)

        return CommandResult(output = "Changed permissions for ${node.name} to mode [$mode].")
    }
}
