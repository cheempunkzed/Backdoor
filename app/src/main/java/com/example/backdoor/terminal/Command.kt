package com.example.backdoor.terminal

import com.example.backdoor.core.SystemStatus
import com.example.backdoor.filesystem.VirtualFileSystem

data class CommandContext(
    val vfs: VirtualFileSystem,
    val systemStatus: SystemStatus,
    val commandRegistry: CommandRegistry,
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
    val description: String
    val usage: String
    suspend fun execute(args: List<String>, context: CommandContext): CommandResult
}
