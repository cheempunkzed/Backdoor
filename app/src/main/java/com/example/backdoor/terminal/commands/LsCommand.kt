package com.example.backdoor.terminal.commands

import com.example.backdoor.filesystem.*
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

class LsCommand : Command {
    override val name: String = "ls"
    override val aliases: List<String> = listOf("dir")
    override val category: CommandCategory = CommandCategory.FILESYSTEM
    override val descriptionKey: StringKey = StringKey.LS_DESC
    override val usage: String = "ls [-la] [path]"
    override val manPage: ManPage = ManPage(
        name = "ls",
        synopsis = "ls [-l] [-a] [path]",
        description = "List information about files in the current or target directory.",
        options = listOf(
            "-l" to "Use long listing format (permissions, owner, size, date)",
            "-a" to "Do not ignore entries starting with ."
        ),
        examples = listOf("ls", "ls -la", "ls /system")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val path = if (parsed.positionalArgs.isNotEmpty()) parsed.positionalArgs.first() else context.vfs.getCwd()
        val showHidden = parsed.hasFlag('a') || parsed.hasLongFlag("all")
        val longFormat = parsed.hasFlag('l') || parsed.hasLongFlag("long")

        val nodes = context.vfs.listDirectory(path) ?: emptyList()
        if (nodes.isEmpty() && context.vfs.getNode(path) == null) {
            return CommandResult(error = "ls: cannot access '$path': No such file or directory", exitCode = 1)
        }

        val filtered = nodes.filter { showHidden || !it.name.startsWith(".") }

        if (longFormat) {
            val sb = StringBuilder()
            val dateFormat = SimpleDateFormat("MMM dd HH:mm", Locale.US)

            filtered.forEach { node ->
                val typeChar = if (node.isDirectory) "d" else "-"
                val permStr = if (node.isDirectory) "rwxr-xr-x" else "rw-r--r--"
                val sizeStr = node.size.toString().padStart(6)
                val dateStr = dateFormat.format(Date(node.modifiedTime))
                val nameStr = if (node.isDirectory) "${node.name}/" else node.name

                sb.append("$typeChar$permStr  ${node.owner.padEnd(8)} $sizeStr  $dateStr  $nameStr\n")
            }
            return CommandResult(output = sb.toString().trimEnd())
        } else {
            val formatted = filtered.joinToString("  ") { node ->
                if (node.isDirectory) "${node.name}/" else node.name
            }
            return CommandResult(output = formatted)
        }
    }
}

class TreeCommand : Command {
    override val name: String = "tree"
    override val category: CommandCategory = CommandCategory.FILESYSTEM
    override val descriptionKey: StringKey = StringKey.TREE_DESC
    override val usage: String = "tree [path]"
    override val manPage: ManPage = ManPage(
        name = "tree",
        synopsis = "tree [path]",
        description = "List contents of directories in a tree-like format.",
        options = emptyList(),
        examples = listOf("tree", "tree /home")
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val rootPath = if (parsed.positionalArgs.isNotEmpty()) parsed.positionalArgs.first() else context.vfs.getCwd()
        val rootNode = context.vfs.getNode(rootPath)
            ?: return CommandResult(error = "tree: '$rootPath': No such directory", exitCode = 1)

        val sb = StringBuilder()
        sb.append("${rootNode.name}/\n")
        buildTree(context.vfs, rootPath, "", sb)

        return CommandResult(output = sb.toString().trimEnd())
    }

    private fun buildTree(vfs: com.example.backdoor.filesystem.VirtualFileSystem, currentPath: String, indent: String, sb: StringBuilder) {
        val children = vfs.listDirectory(currentPath) ?: emptyList()
        children.forEachIndexed { index, child ->
            val isLast = index == children.size - 1
            val prefix = if (isLast) "└── " else "├── "
            val childIndent = if (isLast) "    " else "│   "

            sb.append("$indent$prefix${child.name}${if (child.isDirectory) "/" else ""}\n")
            if (child.isDirectory) {
                buildTree(vfs, child.path, indent + childIndent, sb)
            }
        }
    }
}
