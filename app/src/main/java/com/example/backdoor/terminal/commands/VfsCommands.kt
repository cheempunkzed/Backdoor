package com.example.backdoor.terminal.commands

import com.example.backdoor.filesystem.VFSNode
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandAction
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class PwdCommand : Command {
    override val name: String = "pwd"
    override val description: String = "Print current working directory"
    override val usage: String = "pwd"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        return CommandResult(output = context.vfs.getCwd())
    }
}

class LsCommand : Command {
    override val name: String = "ls"
    override val description: String = "List directory contents"
    override val usage: String = "ls [path]"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        val targetPath = if (args.isNotEmpty()) args.first() else context.vfs.getCwd()
        val nodes = context.vfs.listDirectory(targetPath)

        if (nodes == null) {
            return CommandResult(error = "ls: cannot access '$targetPath': No such directory")
        }

        if (nodes.isEmpty()) {
            return CommandResult(output = "(empty directory)")
        }

        val sb = StringBuilder()
        nodes.forEach { node ->
            val typeIndicator = if (node is VFSNode.Directory) "/" else ""
            val permissions = node.permissions
            val name = node.name
            sb.append("$permissions  $name$typeIndicator\n")
        }
        return CommandResult(output = sb.toString().trimEnd())
    }
}

class TreeCommand : Command {
    override val name: String = "tree"
    override val description: String = "Display directory tree hierarchy"
    override val usage: String = "tree [path]"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        val targetPath = if (args.isNotEmpty()) args.first() else context.vfs.getCwd()
        val treeStr = context.vfs.getTreeString(targetPath)
        return CommandResult(output = treeStr)
    }
}

class VersionCommand : Command {
    override val name: String = "version"
    override val description: String = "Display AbyssOS kernel version info"
    override val usage: String = "version"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        return CommandResult(
            output = """
                AbyssOS Kernel Version: 0.0.1-BACKDOOR-CORE
                Build Date: 2026.07.30
                Virtual Architecture: x86_64_abyss
                Security Protocol: Encrypted Stage-1
            """.trimIndent()
        )
    }
}

class ExitCommand : Command {
    override val name: String = "exit"
    override val description: String = "Exit current terminal session"
    override val usage: String = "exit"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        context.onExitRequested()
        return CommandResult(action = CommandAction.ExitTerminal, output = "Session terminated.")
    }
}

class CdCommand : Command {
    override val name: String = "cd"
    override val description: String = "Change current working directory"
    override val usage: String = "cd <directory_path>"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        val targetPath = if (args.isNotEmpty()) args.first() else "/home"
        val success = context.vfs.changeDirectory(targetPath)
        return if (success) {
            CommandResult(output = context.vfs.getCwd())
        } else {
            CommandResult(error = "cd: no such file or directory: $targetPath")
        }
    }
}

class CatCommand : Command {
    override val name: String = "cat"
    override val description: String = "Read and concatenate file contents"
    override val usage: String = "cat <file_path>"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        if (args.isEmpty()) {
            return CommandResult(error = "cat: missing file operand")
        }
        val path = args.first()
        val content = context.vfs.readFile(path)
        return if (content != null) {
            CommandResult(output = content)
        } else {
            CommandResult(error = "cat: $path: No such file or directory")
        }
    }
}
