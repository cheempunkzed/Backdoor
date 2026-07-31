package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

class VersionCommand : Command {
    override val name: String = "version"
    override val aliases: List<String> = listOf("uname", "ver")
    override val category: CommandCategory = CommandCategory.SYSTEM
    override val descriptionKey: StringKey = StringKey.VERSION_DESC
    override val usage: String = "version"
    override val manPage: ManPage = ManPage(
        name = "version",
        synopsis = "version",
        description = "Print system architecture, kernel release, and build version.",
        options = emptyList(),
        examples = listOf("version")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        return CommandResult(
            output = """
                AbyssOS Kernel Version: 0.4.0-TERMINAL_CORE
                Build Date: 2026.07.30
                Virtual Architecture: x86_64_abyss
                Filesystem Engine: AbyssFS V2 Persistent
                Terminal Core Engine: V4.0 Active
                Process Manager: Active
            """.trimIndent()
        )
    }
}
