package com.example.backdoor.terminal

import com.example.backdoor.core.SystemStatus
import com.example.backdoor.filesystem.VirtualFileSystem
import com.example.backdoor.i18n.StringKey

data class ManPage(
    val name: String,
    val synopsis: String,
    val description: String,
    val options: List<Pair<String, String>> = emptyList(),
    val examples: List<String> = emptyList()
)

enum class CommandCategory(val title: String) {
    FILESYSTEM("FILESYSTEM COMMANDS"),
    SYSTEM("SYSTEM COMMANDS"),
    UTILITY("UTILITY COMMANDS"),
    NETWORK("NETWORK COMMANDS")
}

data class CommandContext(
    val vfs: VirtualFileSystem,
    val systemStatus: SystemStatus,
    val commandRegistry: CommandRegistry,
    val session: TerminalSession,
    val networkEngine: com.example.backdoor.network.engine.AbyssNetworkEngine? = null,
    val eventBus: com.example.backdoor.core.SystemEventBus? = null,
    val onionEngine: com.example.backdoor.darknet.engine.OnionNetworkEngine? = null,
    val onExitRequested: () -> Unit = {},
    val onOpenAppRequested: (String) -> Unit = {}
)

sealed class CommandAction {
    object None : CommandAction()
    object ClearScreen : CommandAction()
    object ExitTerminal : CommandAction()
    data class OpenApp(val appName: String) : CommandAction()
}

data class CommandResult(
    val output: String = "",
    val error: String? = null,
    val exitCode: Int = 0,
    val action: CommandAction = CommandAction.None
)

interface Command {
    val name: String
    val aliases: List<String> get() = emptyList()
    val category: CommandCategory get() = CommandCategory.SYSTEM
    val descriptionKey: StringKey
    val usage: String
    val manPage: ManPage
    suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult
}
