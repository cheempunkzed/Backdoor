package com.example.backdoor.terminal.commands

import com.example.backdoor.filesystem.VFSNode
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class LsCommand : Command {
    override val name: String = "ls"
    override val description: String = "List directory contents"
    override val usage: String = "ls [-a] [-l] [path]"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        var showAll = false
        var showLong = false
        val pathArgs = mutableListOf<String>()

        args.forEach { arg ->
            if (arg.startsWith("-") && arg.length > 1) {
                if (arg.contains("a")) showAll = true
                if (arg.contains("l")) showLong = true
            } else {
                pathArgs.add(arg)
            }
        }

        val targetPath = if (pathArgs.isNotEmpty()) pathArgs.first() else context.vfs.getCwd()
        val nodes = context.vfs.listDirectory(targetPath, includeHidden = showAll)

        if (nodes == null) {
            return CommandResult(error = "ls: cannot access '$targetPath': No such file or directory")
        }

        if (nodes.isEmpty()) {
            return CommandResult(output = "(empty directory)")
        }

        val sb = StringBuilder()
        if (showLong) {
            nodes.forEach { node ->
                val typeInd = if (node is VFSNode.Directory) "/" else ""
                val perms = node.permissions
                val owner = node.metadata.owner.padEnd(8)
                val size = node.metadata.sizeBytes.toString().padStart(6)
                val name = node.name
                sb.append("$perms $owner $size B $name$typeInd\n")
            }
        } else {
            val items = nodes.map { node ->
                val typeInd = if (node is VFSNode.Directory) "/" else ""
                "${node.name}$typeInd"
            }
            sb.append(items.joinToString("  "))
        }

        return CommandResult(output = sb.toString().trimEnd())
    }
}
