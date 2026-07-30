package com.example.backdoor.terminal

import com.example.backdoor.terminal.commands.CatCommand
import com.example.backdoor.terminal.commands.CdCommand
import com.example.backdoor.terminal.commands.ClearCommand
import com.example.backdoor.terminal.commands.CpCommand
import com.example.backdoor.terminal.commands.DateCommand
import com.example.backdoor.terminal.commands.EchoCommand
import com.example.backdoor.terminal.commands.ExitCommand
import com.example.backdoor.terminal.commands.FindCommand
import com.example.backdoor.terminal.commands.HelpCommand
import com.example.backdoor.terminal.commands.HostnameCommand
import com.example.backdoor.terminal.commands.LsCommand
import com.example.backdoor.terminal.commands.MkdirCommand
import com.example.backdoor.terminal.commands.MvCommand
import com.example.backdoor.terminal.commands.OpenCommand
import com.example.backdoor.terminal.commands.PwdCommand
import com.example.backdoor.terminal.commands.RenameCommand
import com.example.backdoor.terminal.commands.RmCommand
import com.example.backdoor.terminal.commands.TimeCommand
import com.example.backdoor.terminal.commands.TouchCommand
import com.example.backdoor.terminal.commands.TreeCommand
import com.example.backdoor.terminal.commands.VersionCommand
import com.example.backdoor.terminal.commands.WhoAmICommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommandRegistry {
    private val commands = mutableMapOf<String, Command>()

    init {
        // Register default required commands
        registerCommand(HelpCommand())
        registerCommand(ClearCommand())
        registerCommand(EchoCommand())
        registerCommand(DateCommand())
        registerCommand(TimeCommand())
        registerCommand(WhoAmICommand())
        registerCommand(HostnameCommand())
        registerCommand(PwdCommand())
        registerCommand(LsCommand())
        registerCommand(TreeCommand())
        registerCommand(VersionCommand())
        registerCommand(ExitCommand())
        registerCommand(CdCommand())
        registerCommand(CatCommand())
        registerCommand(MkdirCommand())
        registerCommand(TouchCommand())
        registerCommand(RmCommand())
        registerCommand(MvCommand())
        registerCommand(CpCommand())
        registerCommand(FindCommand())
        registerCommand(OpenCommand())
        registerCommand(RenameCommand())
    }

    fun registerCommand(command: Command) {
        commands[command.name.lowercase()] = command
    }

    fun getCommand(name: String): Command? = commands[name.lowercase()]

    fun getAllCommands(): List<Command> = commands.values.toList()
}

class CommandExecutor(
    val registry: CommandRegistry
) {
    private val commandHistory = mutableListOf<String>()

    fun getHistory(): List<String> = commandHistory.toList()

    suspend fun execute(commandLine: String, context: CommandContext): CommandResult {
        val trimmed = commandLine.trim()
        if (trimmed.isEmpty()) {
            return CommandResult()
        }

        commandHistory.add(trimmed)

        val parts = trimmed.split("\\s+".toRegex())
        val cmdName = parts.first().lowercase()
        val args = parts.drop(1)

        val command = registry.getCommand(cmdName)
            ?: return CommandResult(error = "Command '$cmdName' not found. Type 'help' for available commands.", exitCode = 127)

        return withContext(Dispatchers.Default) {
            runCatching {
                command.execute(args, context)
            }.getOrElse { ex ->
                CommandResult(error = "Execution error in '$cmdName': ${ex.message}", exitCode = 1)
            }
        }
    }
}
