package com.example.backdoor.terminal

import com.example.backdoor.filesystem.*
import com.example.backdoor.i18n.StringKey
import com.example.backdoor.i18n.StringManager
import com.example.backdoor.terminal.commands.ArpCommand
import com.example.backdoor.terminal.commands.CatCommand
import com.example.backdoor.terminal.commands.CdCommand
import com.example.backdoor.terminal.commands.ChmodCommand
import com.example.backdoor.terminal.commands.ClearCommand
import com.example.backdoor.terminal.commands.CpCommand
import com.example.backdoor.terminal.commands.DateCommand
import com.example.backdoor.terminal.commands.EchoCommand
import com.example.backdoor.terminal.commands.ExitCommand
import com.example.backdoor.terminal.commands.FindCommand
import com.example.backdoor.terminal.commands.HelpCommand
import com.example.backdoor.terminal.commands.HistoryCommand
import com.example.backdoor.terminal.commands.HostnameCommand
import com.example.backdoor.terminal.commands.IpconfigCommand
import com.example.backdoor.terminal.commands.LsCommand
import com.example.backdoor.terminal.commands.ManCommand
import com.example.backdoor.terminal.commands.MkdirCommand
import com.example.backdoor.terminal.commands.MvCommand
import com.example.backdoor.terminal.commands.NetstatCommand
import com.example.backdoor.terminal.commands.NslookupCommand
import com.example.backdoor.terminal.commands.OpenCommand
import com.example.backdoor.terminal.commands.PingCommand
import com.example.backdoor.terminal.commands.PwdCommand
import com.example.backdoor.terminal.commands.RenameCommand
import com.example.backdoor.terminal.commands.RmCommand
import com.example.backdoor.terminal.commands.RouteCommand
import com.example.backdoor.terminal.commands.StatCommand
import com.example.backdoor.terminal.commands.TimeCommand
import com.example.backdoor.terminal.commands.TouchCommand
import com.example.backdoor.terminal.commands.TracerouteCommand
import com.example.backdoor.terminal.commands.TreeCommand
import com.example.backdoor.terminal.commands.VersionCommand
import com.example.backdoor.terminal.commands.WhoAmICommand
import com.example.backdoor.terminal.commands.WhoisCommand
import com.example.backdoor.terminal.commands.SecurityCommand
import com.example.backdoor.terminal.commands.OnionCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommandRegistry {
    private val commands = mutableMapOf<String, Command>()

    init {
        // Automatically register core built-in commands
        registerCommand(HelpCommand())
        registerCommand(ManCommand())
        registerCommand(ClearCommand())
        registerCommand(EchoCommand())
        registerCommand(PwdCommand())
        registerCommand(CdCommand())
        registerCommand(LsCommand())
        registerCommand(TreeCommand())
        registerCommand(CatCommand())
        registerCommand(TouchCommand())
        registerCommand(MkdirCommand())
        registerCommand(RmCommand())
        registerCommand(MvCommand())
        registerCommand(CpCommand())
        registerCommand(FindCommand())
        registerCommand(HistoryCommand())
        registerCommand(WhoAmICommand())
        registerCommand(HostnameCommand())
        registerCommand(DateCommand())
        registerCommand(TimeCommand())
        registerCommand(VersionCommand())
        registerCommand(ExitCommand())
        registerCommand(OpenCommand())
        registerCommand(RenameCommand())
        registerCommand(ChmodCommand())
        registerCommand(StatCommand())

        // Network Commands (AbyssNet)
        registerCommand(PingCommand())
        registerCommand(TracerouteCommand())
        registerCommand(NetstatCommand())
        registerCommand(IpconfigCommand("ipconfig"))
        registerCommand(IpconfigCommand("ifconfig"))
        registerCommand(ArpCommand())
        registerCommand(NslookupCommand())
        registerCommand(WhoisCommand())
        registerCommand(RouteCommand())

        // Security Framework Commands
        registerCommand(SecurityCommand())
        registerCommand(OnionCommand())
    }

    fun registerCommand(command: Command) {
        commands[command.name.lowercase()] = command
        command.aliases.forEach { alias ->
            commands[alias.lowercase()] = command
        }
    }

    fun getCommand(nameOrAlias: String): Command? = commands[nameOrAlias.lowercase()]

    fun getAllCommands(): List<Command> = commands.values.distinct()

    fun getCommandsByCategory(): Map<CommandCategory, List<Command>> {
        return getAllCommands().groupBy { it.category }
    }

    fun getAutocompleteSuggestions(
        query: String,
        session: TerminalSession
    ): List<String> {
        val trimmed = query.trimStart()
        if (trimmed.isEmpty()) return emptyList()

        val tokens = CommandParser.tokenize(trimmed)
        if (tokens.isEmpty()) return emptyList()

        if (tokens.size == 1 && !query.endsWith(" ")) {
            val prefix = tokens[0].lowercase()
            return commands.keys
                .filter { it.startsWith(prefix) }
                .distinct()
                .sorted()
        }

        val lastToken = tokens.last()
        val cwd = session.vfs.getCwd()
        val nodes = session.vfs.listDirectory(cwd) ?: emptyList()
        val matches = nodes
            .map { if (it.isDirectory) "${it.name}/" else it.name }
            .filter { it.lowercase().startsWith(lastToken.lowercase()) }

        return matches.sorted()
    }
}

class CommandExecutor(
    val registry: CommandRegistry
) {
    private val commandHistory = mutableListOf<String>()

    fun getHistory(): List<String> = commandHistory.toList()

    fun clearHistory() {
        commandHistory.clear()
    }

    fun setHistory(history: List<String>) {
        commandHistory.clear()
        commandHistory.addAll(history)
    }

    suspend fun execute(commandLine: String, context: CommandContext): CommandResult {
        val trimmed = commandLine.trim()
        if (trimmed.isEmpty()) {
            return CommandResult()
        }

        // Expand session environment variables
        val expandedLine = context.session.expandVariables(trimmed)
        commandHistory.add(expandedLine)

        // Parse command with argument parser
        val parsed = CommandParser.parse(expandedLine)
        if (parsed.commandName.isEmpty()) {
            return CommandResult()
        }

        // Auto Log to /logs/terminal.log
        logCommandExecution(expandedLine, context)

        val command = registry.getCommand(parsed.commandName)
            ?: return CommandResult(
                error = StringManager.get(StringKey.CMD_NOT_FOUND, parsed.commandName),
                exitCode = 127
            )

        return withContext(Dispatchers.Default) {
            runCatching {
                command.execute(parsed, context)
            }.getOrElse { ex ->
                val errorMsg = when (ex) {
                    is TerminalException -> ex.message
                    else -> StringManager.get(StringKey.CMD_EXEC_ERROR, parsed.commandName, ex.message ?: "Unknown error")
                }
                CommandResult(error = errorMsg, exitCode = 1)
            }
        }
    }

    private fun logCommandExecution(cmdLine: String, context: CommandContext) {
        try {
            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val user = context.session.getEnv("USER").ifEmpty { "operator" }
            val host = context.session.getEnv("HOSTNAME").ifEmpty { "abyss" }
            val cwd = context.session.vfs.getCwd()

            val logEntry = "[$timeStr] [$user@$host $cwd]$ $cmdLine\n"
            val existingLog = context.vfs.readFile("/logs/terminal.log") ?: ""
            context.vfs.createFile("/logs", "terminal.log", existingLog + logEntry, owner = "system")
        } catch (e: Exception) {
            // Non-blocking fallback
        }
    }
}
