package com.example.backdoor.terminal.commands

import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult

class TreeCommand : Command {
    override val name: String = "tree"
    override val description: String = "List directory contents in a tree-like format"
    override val usage: String = "tree [path]"

    override suspend fun execute(args: List<String>, context: CommandContext): CommandResult {
        val targetPath = if (args.isNotEmpty()) args.first() else context.vfs.getCwd()
        val treeStr = context.vfs.getTreeString(targetPath)
        return CommandResult(output = treeStr)
    }
}
