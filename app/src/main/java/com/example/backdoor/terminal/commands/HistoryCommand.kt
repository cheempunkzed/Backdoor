package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

class HistoryCommand : Command {
    override val name: String = "history"
    override val category: CommandCategory = CommandCategory.SYSTEM
    override val descriptionKey: StringKey = StringKey.HISTORY_DESC
    override val usage: String = "history [-c]"
    override val manPage: ManPage = ManPage(
        name = "history",
        synopsis = "history [-c]",
        description = "Display list of previously executed commands in current session.",
        options = listOf("-c" to "Clear command execution history"),
        examples = listOf("history", "history -c")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        if (parsed.hasFlag('c')) {
            context.commandRegistry.let {
                // Command executor clears history
            }
            return CommandResult(output = "Command history buffer cleared.")
        }

        return CommandResult(output = "Command history recorded in /logs/terminal.log and session history.")
    }
}

class WhoAmICommand : Command {
    override val name: String = "whoami"
    override val category: CommandCategory = CommandCategory.SYSTEM
    override val descriptionKey: StringKey = StringKey.WHOAMI_DESC
    override val usage: String = "whoami"
    override val manPage: ManPage = ManPage(
        name = "whoami",
        synopsis = "whoami",
        description = "Print the user name associated with the current effective user ID.",
        options = emptyList(),
        examples = listOf("whoami")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val user = context.session.getEnv("USER").ifEmpty { context.systemStatus.userHandle }
        return CommandResult(output = user)
    }
}

class HostnameCommand : Command {
    override val name: String = "hostname"
    override val category: CommandCategory = CommandCategory.SYSTEM
    override val descriptionKey: StringKey = StringKey.HOSTNAME_DESC
    override val usage: String = "hostname"
    override val manPage: ManPage = ManPage(
        name = "hostname",
        synopsis = "hostname",
        description = "Show system host name.",
        options = emptyList(),
        examples = listOf("hostname")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val host = context.session.getEnv("HOSTNAME").ifEmpty { context.systemStatus.hostname }
        return CommandResult(output = host)
    }
}

