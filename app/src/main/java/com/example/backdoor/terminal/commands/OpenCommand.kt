package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandAction
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

class OpenCommand : Command {
    override val name: String = "open"
    override val category: CommandCategory = CommandCategory.UTILITY
    override val descriptionKey: StringKey = StringKey.OPEN_DESC
    override val usage: String = "open <app_name>"
    override val manPage: ManPage = ManPage(
        name = "open",
        synopsis = "open <application_name>",
        description = "Launch GUI system application (e.g. files, settings, logs, monitor, darknet, browser, network).",
        options = emptyList(),
        examples = listOf("open files", "open settings", "open monitor")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        if (parsed.positionalArgs.isEmpty()) {
            return CommandResult(error = "open: missing app name. Examples: 'open files', 'open settings'", exitCode = 1)
        }

        val appName = parsed.positionalArgs.first().lowercase()
        return CommandResult(
            output = "Launching application '$appName'...",
            action = CommandAction.OpenApp(appName)
        )
    }
}
