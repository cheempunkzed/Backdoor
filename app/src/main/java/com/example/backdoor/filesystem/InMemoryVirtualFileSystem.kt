package com.example.backdoor.filesystem

class InMemoryVirtualFileSystem : VirtualFileSystem {

    private var currentPath: String = "/home"
    private val root = VFSNode.Directory(name = "", path = "/", parentPath = "")

    init {
        initializeDefaultFileSystem()
    }

    private fun initializeDefaultFileSystem() {
        // Create root directories
        val home = VFSNode.Directory("home", "/home", "/")
        val downloads = VFSNode.Directory("downloads", "/downloads", "/")
        val desktop = VFSNode.Directory("desktop", "/desktop", "/")
        val logs = VFSNode.Directory("logs", "/logs", "/")
        val system = VFSNode.Directory("system", "/system", "/")
        val bin = VFSNode.Directory("bin", "/bin", "/")
        val tmp = VFSNode.Directory("tmp", "/tmp", "/")

        root.children.addAll(listOf(home, downloads, desktop, logs, system, bin, tmp))

        // Populate home
        home.children.add(
            VFSNode.File(
                name = "readme.txt",
                path = "/home/readme.txt",
                parentPath = "/home",
                content = "Welcome to AbyssOS 0.0.1 Terminal.\nType 'help' for a list of commands.\nYour system node ID is #8094-ALPHA."
            )
        )
        home.children.add(
            VFSNode.File(
                name = "notes.txt",
                path = "/home/notes.txt",
                parentPath = "/home",
                content = "TODO:\n1. Verify kernel modularity.\n2. Prepare Backdoor Phase 1 save slots.\n3. Encrypt node communications."
            )
        )

        // Populate downloads
        downloads.children.add(
            VFSNode.File(
                name = "sample_payload.sh",
                path = "/downloads/sample_payload.sh",
                parentPath = "/downloads",
                permissions = "-rwxr-xr-x",
                content = "#!/bin/sh\necho 'Backdoor simulation payload initialized.'",
                isExecutable = true
            )
        )

        // Populate desktop
        desktop.children.add(
            VFSNode.File(
                name = "shortcut_terminal.lnk",
                path = "/desktop/shortcut_terminal.lnk",
                parentPath = "/desktop",
                content = "AppTarget: Terminal"
            )
        )

        // Populate logs
        logs.children.add(
            VFSNode.File(
                name = "boot.log",
                path = "/logs/boot.log",
                parentPath = "/logs",
                content = "[INFO] AbyssOS 0.0.1 Kernel initialized successfully.\n[INFO] VFS Mounted.\n[INFO] Services status: OK."
            )
        )
        logs.children.add(
            VFSNode.File(
                name = "syslog.log",
                path = "/logs/syslog.log",
                parentPath = "/logs",
                content = "[SYS] System time synced.\n[SYS] Battery virtual monitor running at 88%.\n[SYS] Backdoor core standby."
            )
        )

        // Populate system
        system.children.add(
            VFSNode.File(
                name = "os.build",
                path = "/system/os.build",
                parentPath = "/system",
                content = "BUILD_NAME=AbyssOS\nVERSION=0.0.1\nCODENAME=BACKDOOR_STAGE1"
            )
        )
        system.children.add(
            VFSNode.File(
                name = "config.json",
                path = "/system/config.json",
                parentPath = "/system",
                content = "{\n  \"theme\": \"matrix_green\",\n  \"crt_effect\": true,\n  \"network_auto_connect\": true\n}"
            )
        )

        // Populate bin with executables matching commands
        val binaries = listOf("help", "clear", "echo", "date", "time", "whoami", "hostname", "pwd", "ls", "tree", "version", "exit")
        binaries.forEach { cmd ->
            bin.children.add(
                VFSNode.File(
                    name = cmd,
                    path = "/bin/$cmd",
                    parentPath = "/bin",
                    permissions = "-rwxr-xr-x",
                    content = "ELF 64-bit LSB executable, AbyssOS command $cmd",
                    isExecutable = true
                )
            )
        }
    }

    override fun getCwd(): String = currentPath

    override fun changeDirectory(path: String): Boolean {
        val resolved = resolvePath(path)
        val node = getNode(resolved)
        return if (node is VFSNode.Directory) {
            currentPath = resolved
            true
        } else {
            false
        }
    }

    override fun getNode(path: String): VFSNode? {
        val resolved = resolvePath(path)
        if (resolved == "/") return root

        val segments = resolved.split("/").filter { it.isNotEmpty() }
        var currentDir: VFSNode.Directory = root

        for (i in segments.indices) {
            val segment = segments[i]
            val found = currentDir.children.find { it.name == segment } ?: return null
            if (i == segments.lastIndex) {
                return found
            } else if (found is VFSNode.Directory) {
                currentDir = found
            } else {
                return null
            }
        }
        return null
    }

    override fun listDirectory(path: String): List<VFSNode>? {
        val node = getNode(path)
        return if (node is VFSNode.Directory) {
            node.children.toList()
        } else null
    }

    override fun readFile(path: String): String? {
        val node = getNode(path)
        return if (node is VFSNode.File) {
            node.content
        } else null
    }

    override fun createFile(dirPath: String, fileName: String, content: String, isExecutable: Boolean): Boolean {
        val resolvedDir = resolvePath(dirPath)
        val parentNode = getNode(resolvedDir)
        if (parentNode is VFSNode.Directory) {
            if (parentNode.children.any { it.name == fileName }) return false
            val filePath = if (resolvedDir == "/") "/$fileName" else "$resolvedDir/$fileName"
            val newFile = VFSNode.File(
                name = fileName,
                path = filePath,
                parentPath = resolvedDir,
                permissions = if (isExecutable) "-rwxr-xr-x" else "-rw-r--r--",
                content = content,
                isExecutable = isExecutable
            )
            parentNode.children.add(newFile)
            return true
        }
        return false
    }

    override fun createDirectory(dirPath: String, dirName: String): Boolean {
        val resolvedDir = resolvePath(dirPath)
        val parentNode = getNode(resolvedDir)
        if (parentNode is VFSNode.Directory) {
            if (parentNode.children.any { it.name == dirName }) return false
            val newPath = if (resolvedDir == "/") "/$dirName" else "$resolvedDir/$dirName"
            val newDir = VFSNode.Directory(
                name = dirName,
                path = newPath,
                parentPath = resolvedDir
            )
            parentNode.children.add(newDir)
            return true
        }
        return false
    }

    override fun deleteNode(path: String): Boolean {
        val resolved = resolvePath(path)
        if (resolved == "/" || resolved == "/home" || resolved == "/bin") return false
        val node = getNode(resolved) ?: return false
        val parentNode = getNode(node.parentPath) as? VFSNode.Directory ?: return false
        return parentNode.children.remove(node)
    }

    override fun resolvePath(targetPath: String): String {
        val path = when {
            targetPath == "~" -> "/home"
            targetPath.startsWith("~/") -> "/home" + targetPath.removePrefix("~")
            targetPath.startsWith("/") -> targetPath
            else -> if (currentPath == "/") "/$targetPath" else "$currentPath/$targetPath"
        }

        val rawSegments = path.split("/").filter { it.isNotEmpty() }
        val stack = mutableListOf<String>()

        for (segment in rawSegments) {
            when (segment) {
                "." -> continue
                ".." -> if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
                else -> stack.add(segment)
            }
        }

        return "/" + stack.joinToString("/")
    }

    override fun getTreeString(path: String, maxDepth: Int): String {
        val node = getNode(path) ?: return "Path not found: $path"
        val sb = StringBuilder()
        buildTreeRecursive(node, "", sb, 0, maxDepth)
        return sb.toString()
    }

    private fun buildTreeRecursive(node: VFSNode, prefix: String, sb: StringBuilder, currentDepth: Int, maxDepth: Int) {
        sb.append(node.name.ifEmpty { "/" })
        if (node is VFSNode.Directory) {
            sb.append("/\n")
            if (currentDepth >= maxDepth) return
            val children = node.children
            children.forEachIndexed { index, child ->
                val isLast = index == children.lastIndex
                val pointer = if (isLast) "└── " else "├── "
                val newPrefix = prefix + if (isLast) "    " else "│   "
                sb.append(prefix).append(pointer)
                buildTreeRecursive(child, newPrefix, sb, currentDepth + 1, maxDepth)
            }
        } else if (node is VFSNode.File) {
            sb.append("\n")
        }
    }
}
