package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateCommand : Command {
    override val name: String = "date"
    override val category: CommandCategory = CommandCategory.SYSTEM
    override val descriptionKey: StringKey = StringKey.DATE_DESC
    override val usage: String = "date"
    override val manPage: ManPage = ManPage(
        name = "date",
        synopsis = "date",
        description = "Display current system date and time.",
        options = emptyList(),
        examples = listOf("date")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US)
        return CommandResult(output = dateFormat.format(Date()))
    }
}

class TimeCommand : Command {
    override val name: String = "time"
    override val category: CommandCategory = CommandCategory.SYSTEM
    override val descriptionKey: StringKey = StringKey.TIME_DESC
    override val usage: String = "time"
    override val manPage: ManPage = ManPage(
        name = "time",
        synopsis = "time",
        description = "Display current time and kernel uptime in seconds.",
        options = emptyList(),
        examples = listOf("time")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
        val timeStr = timeFormat.format(Date())
        val uptime = context.systemStatus.uptimeSeconds
        return CommandResult(output = "System Time: $timeStr | Uptime: ${uptime}s")
    }
}
