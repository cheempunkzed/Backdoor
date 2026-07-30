package com.example.backdoor.terminal.commands

import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateCommand : Command {
    override val name: String = "date"
    override val description: String = "Display current system date"
    override val usage: String = "date"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        val formatter = SimpleDateFormat("EEE MMM dd yyyy", Locale.US)
        return CommandResult(output = formatter.format(Date()))
    }
}

class TimeCommand : Command {
    override val name: String = "time"
    override val description: String = "Display current system time"
    override val usage: String = "time"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        val formatter = SimpleDateFormat("HH:mm:ss z", Locale.US)
        return CommandResult(output = formatter.format(Date()))
    }
}

class WhoAmICommand : Command {
    override val name: String = "whoami"
    override val description: String = "Print the current active user handle"
    override val usage: String = "whoami"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        return CommandResult(output = context.systemStatus.userHandle)
    }
}

class HostnameCommand : Command {
    override val name: String = "hostname"
    override val description: String = "Print system node hostname"
    override val usage: String = "hostname"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        return CommandResult(output = context.systemStatus.hostname)
    }
}
