package com.example.backdoor.filesystem

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class InMemoryVirtualFileSystem : VirtualFileSystem {

    private var currentPath: String = "/home"
    private var activeUser: String = "operator"

    private val _updateEvent = MutableStateFlow(System.currentTimeMillis())
    override val updateEvent: StateFlow<Long> = _updateEvent.asStateFlow()

    private val root = VFSNode.Directory(
        name = "",
        path = "/",
        parentPath = "",
        isSystemProtected = true
    )

    // Scalable O(1) indexed path lookup map
    private val pathNodeMap = mutableMapOf<String, VFSNode>()

    init {
        pathNodeMap["/"] = root
        initializeDefaultFileSystem()
    }

    private fun notifyStateChanged() {
        _updateEvent.value = System.currentTimeMillis()
    }

    private fun initializeDefaultFileSystem() {
        val rootDirs = listOf(
            "bin", "boot", "dev", "etc", "home", "tmp", "usr", "var",
            "logs", "network", "desktop", "downloads", "documents", "darknet", "system"
        )

        val protectedSet = setOf("bin", "boot", "dev", "etc", "system")

        rootDirs.forEach { dirName ->
            val path = "/$dirName"
            val isProt = protectedSet.contains(dirName)
            val dirNode = VFSNode.Directory(
                name = dirName,
                path = path,
                parentPath = "/",
                isSystemProtected = isProt,
                owner = if (isProt) "root" else "system"
            )
            root.children.add(dirNode)
            pathNodeMap[path] = dirNode
        }

        // Setup default system files
        createFileInternal(
            dirPath = "/system",
            fileName = "os.build",
            content = "BUILD_NAME=AbyssOS\nVERSION=0.2.0\nCODENAME=ABYSSFS_ENGINE",
            isExecutable = false,
            owner = "root",
            isProtected = true
        )

        createFileInternal(
            dirPath = "/system",
            fileName = "kernel.sys",
            content = "[KERNEL] AbyssOS v0.2.0 Core Active.\n[MODULE] AbyssFS Filesystem V2 Mounted.",
            isExecutable = false,
            owner = "root",
            isProtected = true
        )

        createFileInternal(
            dirPath = "/logs",
            fileName = "boot.log",
            content = "[INFO] AbyssOS 0.2.0 Kernel initialized successfully.\n[INFO] AbyssFS Mounted.\n[INFO] Services status: OK.",
            isExecutable = false,
            owner = "system"
        )

        createFileInternal(
            dirPath = "/logs",
            fileName = "fs_audit.log",
            content = "[FS_AUDIT] Filesystem initialized at timestamp ${System.currentTimeMillis()}",
            isExecutable = false,
            owner = "system"
        )

        // Populate bin executables
        val binaries = listOf("help", "clear", "echo", "date", "time", "whoami", "hostname", "pwd", "ls", "tree", "version", "exit", "cd", "cat", "mkdir", "touch", "rm", "mv", "cp", "find", "open", "rename")
        binaries.forEach { cmd ->
            createFileInternal(
                dirPath = "/bin",
                fileName = cmd,
                content = "ELF 64-bit LSB executable, AbyssOS command $cmd",
                isExecutable = true,
                owner = "root",
                isProtected = true
            )
        }

        // Setup default user home
        setupUserHome("operator")
    }

    override fun setupUserHome(username: String) {
        activeUser = username
        val userHomePath = "/home/$username"

        if (getNode(userHomePath) == null) {
            createDirectoryInternal(
                dirPath = "/home",
                dirName = username,
                owner = username,
                isProtected = false
            )
        }

        val userSubFolders = listOf("Desktop", "Downloads", "Documents", "Notes", "Scripts", "Trash")
        userSubFolders.forEach { sub ->
            val subPath = "$userHomePath/$sub"
            if (getNode(subPath) == null) {
                createDirectoryInternal(
                    dirPath = userHomePath,
                    dirName = sub,
                    owner = username,
                    isProtected = false
                )
            }
        }

        // Add welcome notes if not present
        if (getNode("$userHomePath/Notes/welcome.txt") == null) {
            createFileInternal(
                dirPath = "$userHomePath/Notes",
                fileName = "welcome.txt",
                content = "Welcome to AbyssOS 0.2.0 with AbyssFS Virtual Filesystem.\n" +
                        "Commands supported: cd, mkdir, touch, rm, mv, cp, cat, pwd, ls, tree, find, open, rename.\n" +
                        "Files deleted in user space move to Trash and can be restored.",
                owner = username
            )
        }

        if (getNode("$userHomePath/Scripts/sample_payload.sh") == null) {
            createFileInternal(
                dirPath = "$userHomePath/Scripts",
                fileName = "sample_payload.sh",
                content = "#!/bin/sh\necho 'AbyssFS execution script verified.'",
                isExecutable = true,
                owner = username
            )
        }

        currentPath = userHomePath
        notifyStateChanged()
    }

    private fun createFileInternal(
        dirPath: String,
        fileName: String,
        content: String = "",
        isExecutable: Boolean = false,
        owner: String = "operator",
        isProtected: Boolean = false
    ): Boolean {
        val resolvedDir = resolvePath(dirPath)
        val parentNode = getNode(resolvedDir) as? VFSNode.Directory ?: return false

        val filePath = if (resolvedDir == "/") "/$fileName" else "$resolvedDir/$fileName"
        if (pathNodeMap.containsKey(filePath)) return false

        val newFile = VFSNode.File(
            name = fileName,
            path = filePath,
            parentPath = resolvedDir,
            permissions = if (isExecutable) "-rwxr-xr-x" else "-rw-r--r--",
            content = content,
            isExecutable = isExecutable,
            owner = owner,
            isSystemProtected = isProtected
        )

        parentNode.children.add(newFile)
        pathNodeMap[filePath] = newFile
        return true
    }

    private fun createDirectoryInternal(
        dirPath: String,
        dirName: String,
        owner: String = "operator",
        isProtected: Boolean = false
    ): Boolean {
        val resolvedDir = resolvePath(dirPath)
        val parentNode = getNode(resolvedDir) as? VFSNode.Directory ?: return false

        val newPath = if (resolvedDir == "/") "/$dirName" else "$resolvedDir/$dirName"
        if (pathNodeMap.containsKey(newPath)) return false

        val newDir = VFSNode.Directory(
            name = dirName,
            path = newPath,
            parentPath = resolvedDir,
            owner = owner,
            isSystemProtected = isProtected
        )

        parentNode.children.add(newDir)
        pathNodeMap[newPath] = newDir
        return true
    }

    override fun getCwd(): String = currentPath

    override fun changeDirectory(path: String): Boolean {
        val resolved = resolvePath(path)
        val node = getNode(resolved)
        return if (node is VFSNode.Directory && !node.metadata.inTrash) {
            currentPath = resolved
            notifyStateChanged()
            true
        } else {
            false
        }
    }

    override fun getNode(path: String): VFSNode? {
        val resolved = resolvePath(path)
        return pathNodeMap[resolved]
    }

    override fun listDirectory(path: String, includeHidden: Boolean, includeTrash: Boolean): List<VFSNode>? {
        val node = getNode(path)
        return if (node is VFSNode.Directory) {
            node.children.filter { child ->
                (includeTrash || !child.metadata.inTrash) &&
                        (includeHidden || !child.metadata.isHidden)
            }.sortedWith(compareBy({ it !is VFSNode.Directory }, { it.name.lowercase() }))
        } else null
    }

    override fun readFile(path: String): String? {
        val node = getNode(path)
        return if (node is VFSNode.File && !node.metadata.inTrash) {
            node.content
        } else null
    }

    override fun writeFile(path: String, content: String, userHandle: String): Boolean {
        val node = getNode(path)
        if (node is VFSNode.File) {
            if (node.metadata.isSystemProtected) return false
            node.content = content
            val updatedMeta = node.metadata.copy(
                sizeBytes = content.toByteArray().size.toLong(),
                modifiedTimestamp = System.currentTimeMillis()
            )
            val updatedFile = node.copy(metadata = updatedMeta)
            updateNodeInMapAndParent(updatedFile)
            logAuditAction("Modified file", path, userHandle)
            notifyStateChanged()
            return true
        }
        return false
    }

    override fun createFile(
        dirPath: String,
        fileName: String,
        content: String,
        isExecutable: Boolean,
        owner: String
    ): Boolean {
        val success = createFileInternal(
            dirPath = dirPath,
            fileName = fileName,
            content = content,
            isExecutable = isExecutable,
            owner = owner
        )
        if (success) {
            val fullPath = resolvePath(if (dirPath == "/") "/$fileName" else "$dirPath/$fileName")
            logAuditAction("Created file", fullPath, owner)
            notifyStateChanged()
        }
        return success
    }

    override fun createDirectory(dirPath: String, dirName: String, owner: String): Boolean {
        val success = createDirectoryInternal(dirPath, dirName, owner)
        if (success) {
            val fullPath = resolvePath(if (dirPath == "/") "/$dirName" else "$dirPath/$dirName")
            logAuditAction("Created folder", fullPath, owner)
            notifyStateChanged()
        }
        return success
    }

    override fun deleteNode(path: String, permanent: Boolean, userHandle: String): Boolean {
        val resolved = resolvePath(path)
        val node = getNode(resolved) ?: return false

        if (node.metadata.isSystemProtected || resolved == "/" || resolved == "/home" || resolved == "/bin") {
            return false
        }

        if (permanent || node.metadata.inTrash) {
            // Remove completely
            removeFromParentAndMap(node)
            logAuditAction("Deleted file permanently", resolved, userHandle)
            notifyStateChanged()
            return true
        } else {
            // Move to Trash (/home/<user>/Trash)
            val trashDirPath = "/home/$userHandle/Trash"
            if (getNode(trashDirPath) == null) {
                createDirectoryInternal("/home/$userHandle", "Trash", userHandle)
            }

            val trashNode = getNode(trashDirPath) as? VFSNode.Directory ?: return false
            val origParent = getNode(node.parentPath) as? VFSNode.Directory ?: return false

            origParent.children.remove(node)
            pathNodeMap.remove(node.path)

            val trashPath = "$trashDirPath/${node.name}"
            val updatedMeta = node.metadata.copy(
                path = trashPath,
                parentPath = trashDirPath,
                inTrash = true,
                originalPath = resolved,
                modifiedTimestamp = System.currentTimeMillis()
            )

            val newTrashNode = when (node) {
                is VFSNode.Directory -> node.copy(metadata = updatedMeta)
                is VFSNode.File -> node.copy(metadata = updatedMeta)
            }

            trashNode.children.add(newTrashNode)
            pathNodeMap[trashPath] = newTrashNode

            logAuditAction("Moved to Trash", resolved, userHandle)
            notifyStateChanged()
            return true
        }
    }

    override fun restoreFromTrash(trashPath: String, userHandle: String): Boolean {
        val resolved = resolvePath(trashPath)
        val node = getNode(resolved) ?: return false
        val origPath = node.metadata.originalPath ?: return false

        val origParentPath = origPath.substringBeforeLast('/', "/")
        var parentDir = getNode(origParentPath) as? VFSNode.Directory

        if (parentDir == null) {
            // Re-create parent directory structure
            createDirectoryInternal(
                dirPath = origParentPath.substringBeforeLast('/', "/"),
                dirName = origParentPath.substringAfterLast('/'),
                owner = userHandle
            )
            parentDir = getNode(origParentPath) as? VFSNode.Directory ?: return false
        }

        removeFromParentAndMap(node)

        val restoredMeta = node.metadata.copy(
            path = origPath,
            parentPath = origParentPath,
            inTrash = false,
            originalPath = null,
            modifiedTimestamp = System.currentTimeMillis()
        )

        val restoredNode = when (node) {
            is VFSNode.Directory -> node.copy(metadata = restoredMeta)
            is VFSNode.File -> node.copy(metadata = restoredMeta)
        }

        parentDir.children.add(restoredNode)
        pathNodeMap[origPath] = restoredNode

        logAuditAction("Restored file", origPath, userHandle)
        notifyStateChanged()
        return true
    }

    override fun emptyTrash(userHandle: String): Boolean {
        val trashDirPath = "/home/$userHandle/Trash"
        val trashNode = getNode(trashDirPath) as? VFSNode.Directory ?: return false

        val trashItems = trashNode.children.toList()
        trashItems.forEach { child ->
            removeFromParentAndMap(child)
        }
        trashNode.children.clear()

        logAuditAction("Emptied Trash", trashDirPath, userHandle)
        notifyStateChanged()
        return true
    }

    override fun copyNode(srcPath: String, dstPath: String, userHandle: String): Boolean {
        val resolvedSrc = resolvePath(srcPath)
        val srcNode = getNode(resolvedSrc) ?: return false

        val resolvedDst = resolvePath(dstPath)
        var targetDir = getNode(resolvedDst) as? VFSNode.Directory

        val newName: String
        val targetDirPath: String

        if (targetDir != null) {
            newName = srcNode.name
            targetDirPath = resolvedDst
        } else {
            targetDirPath = resolvedDst.substringBeforeLast('/', "/")
            newName = resolvedDst.substringAfterLast('/')
            targetDir = getNode(targetDirPath) as? VFSNode.Directory ?: return false
        }

        val newPath = if (targetDirPath == "/") "/$newName" else "$targetDirPath/$newName"
        if (pathNodeMap.containsKey(newPath)) return false

        val copiedNode = duplicateNodeRecursive(srcNode, newPath, targetDirPath, newName, userHandle)
        targetDir.children.add(copiedNode)
        pathNodeMap[newPath] = copiedNode

        logAuditAction("Copied file", "$resolvedSrc -> $newPath", userHandle)
        notifyStateChanged()
        return true
    }

    private fun duplicateNodeRecursive(
        node: VFSNode,
        newPath: String,
        newParentPath: String,
        newName: String,
        owner: String
    ): VFSNode {
        val meta = node.metadata.copy(
            name = newName,
            path = newPath,
            parentPath = newParentPath,
            owner = owner,
            createdTimestamp = System.currentTimeMillis(),
            modifiedTimestamp = System.currentTimeMillis(),
            isSystemProtected = false
        )

        return when (node) {
            is VFSNode.File -> {
                val f = VFSNode.File(metadata = meta, content = node.content, isExecutable = node.isExecutable)
                pathNodeMap[newPath] = f
                f
            }
            is VFSNode.Directory -> {
                val newDir = VFSNode.Directory(metadata = meta)
                pathNodeMap[newPath] = newDir
                node.children.forEach { child ->
                    val childNewPath = if (newPath == "/") "/${child.name}" else "$newPath/${child.name}"
                    val childCopy = duplicateNodeRecursive(child, childNewPath, newPath, child.name, owner)
                    newDir.children.add(childCopy)
                }
                newDir
            }
        }
    }

    override fun moveNode(srcPath: String, dstPath: String, userHandle: String): Boolean {
        val resolvedSrc = resolvePath(srcPath)
        val srcNode = getNode(resolvedSrc) ?: return false

        if (srcNode.metadata.isSystemProtected) return false

        val resolvedDst = resolvePath(dstPath)
        var targetDir = getNode(resolvedDst) as? VFSNode.Directory

        val newName: String
        val targetDirPath: String

        if (targetDir != null) {
            newName = srcNode.name
            targetDirPath = resolvedDst
        } else {
            targetDirPath = resolvedDst.substringBeforeLast('/', "/")
            newName = resolvedDst.substringAfterLast('/')
            targetDir = getNode(targetDirPath) as? VFSNode.Directory ?: return false
        }

        val newPath = if (targetDirPath == "/") "/$newName" else "$targetDirPath/$newName"
        if (pathNodeMap.containsKey(newPath) && newPath != resolvedSrc) return false

        removeFromParentAndMap(srcNode)

        updateNodePathsRecursive(srcNode, newPath, targetDirPath, newName)
        targetDir.children.add(srcNode)

        logAuditAction("Moved directory/file", "$resolvedSrc -> $newPath", userHandle)
        notifyStateChanged()
        return true
    }

    override fun renameNode(path: String, newName: String, userHandle: String): Boolean {
        val resolved = resolvePath(path)
        val node = getNode(resolved) ?: return false
        if (node.metadata.isSystemProtected) return false

        val parentPath = node.parentPath
        val newPath = if (parentPath == "/") "/$newName" else "$parentPath/$newName"
        if (pathNodeMap.containsKey(newPath) && newPath != resolved) return false

        removeFromParentAndMap(node)
        updateNodePathsRecursive(node, newPath, parentPath, newName)

        val parentDir = getNode(parentPath) as? VFSNode.Directory
        parentDir?.children?.add(node)

        logAuditAction("Renamed file", "$resolved -> $newPath", userHandle)
        notifyStateChanged()
        return true
    }

    private fun updateNodePathsRecursive(node: VFSNode, newPath: String, newParentPath: String, newName: String) {
        val oldPath = node.path
        pathNodeMap.remove(oldPath)

        val updatedMeta = node.metadata.copy(
            name = newName,
            path = newPath,
            parentPath = newParentPath,
            modifiedTimestamp = System.currentTimeMillis()
        )

        when (node) {
            is VFSNode.File -> {
                val updatedFile = node.copy(metadata = updatedMeta)
                pathNodeMap[newPath] = updatedFile
            }
            is VFSNode.Directory -> {
                val updatedDir = node.copy(metadata = updatedMeta)
                pathNodeMap[newPath] = updatedDir
                updatedDir.children.forEach { child ->
                    val childNewPath = if (newPath == "/") "/${child.name}" else "$newPath/${child.name}"
                    updateNodePathsRecursive(child, childNewPath, newPath, child.name)
                }
            }
        }
    }

    override fun findFiles(query: String, startPath: String): List<VFSNode> {
        val resolvedStart = resolvePath(startPath)
        val lower = query.lowercase()
        val result = mutableListOf<VFSNode>()

        val startNode = getNode(resolvedStart) ?: return emptyList()
        searchRecursive(startNode, lower, result)
        return result
    }

    private fun searchRecursive(node: VFSNode, query: String, results: MutableList<VFSNode>) {
        if (!node.metadata.inTrash && node.name.lowercase().contains(query)) {
            results.add(node)
        }
        if (node is VFSNode.Directory && !node.metadata.inTrash) {
            node.children.forEach { child ->
                searchRecursive(child, query, results)
            }
        }
    }

    private fun removeFromParentAndMap(node: VFSNode) {
        val parentDir = getNode(node.parentPath) as? VFSNode.Directory
        parentDir?.children?.remove(node)
        removeRecursiveFromMap(node)
    }

    private fun removeRecursiveFromMap(node: VFSNode) {
        pathNodeMap.remove(node.path)
        if (node is VFSNode.Directory) {
            node.children.forEach { child ->
                removeRecursiveFromMap(child)
            }
        }
    }

    private fun updateNodeInMapAndParent(node: VFSNode) {
        pathNodeMap[node.path] = node
        val parentDir = getNode(node.parentPath) as? VFSNode.Directory
        if (parentDir != null) {
            val idx = parentDir.children.indexOfFirst { it.path == node.path }
            if (idx != -1) {
                parentDir.children[idx] = node
            }
        }
    }

    override fun resolvePath(targetPath: String): String {
        val path = when {
            targetPath == "~" -> "/home/$activeUser"
            targetPath.startsWith("~/") -> "/home/$activeUser" + targetPath.removePrefix("~")
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
        if (node.metadata.inTrash) return
        sb.append(node.name.ifEmpty { "/" })
        if (node is VFSNode.Directory) {
            sb.append("/\n")
            if (currentDepth >= maxDepth) return
            val validChildren = node.children.filter { !it.metadata.inTrash }
            validChildren.forEachIndexed { index, child ->
                val isLast = index == validChildren.lastIndex
                val pointer = if (isLast) "└── " else "├── "
                val newPrefix = prefix + if (isLast) "    " else "│   "
                sb.append(prefix).append(pointer)
                buildTreeRecursive(child, newPrefix, sb, currentDepth + 1, maxDepth)
            }
        } else if (node is VFSNode.File) {
            sb.append("\n")
        }
    }

    override fun logAuditAction(action: String, path: String, userHandle: String) {
        val logFile = getNode("/logs/fs_audit.log") as? VFSNode.File
        val entry = "[FS_AUDIT] [$userHandle] $action: $path"
        if (logFile != null) {
            logFile.content += "\n$entry"
        } else {
            createFileInternal("/logs", "fs_audit.log", entry, false, "system")
        }
    }

    override fun serializeToJson(): String {
        val jsonArray = JSONArray()
        pathNodeMap.values.forEach { node ->
            val obj = JSONObject().apply {
                put("name", node.name)
                put("path", node.path)
                put("parentPath", node.parentPath)
                put("isDirectory", node is VFSNode.Directory)
                put("fileType", node.metadata.fileType.name)
                put("sizeBytes", node.metadata.sizeBytes)
                put("createdTimestamp", node.metadata.createdTimestamp)
                put("modifiedTimestamp", node.metadata.modifiedTimestamp)
                put("owner", node.metadata.owner)
                put("permissions", node.metadata.permissions)
                put("isHidden", node.metadata.isHidden)
                put("isSystemProtected", node.metadata.isSystemProtected)
                put("inTrash", node.metadata.inTrash)
                put("originalPath", node.metadata.originalPath ?: "")
                if (node is VFSNode.File) {
                    put("content", node.content)
                    put("isExecutable", node.isExecutable)
                }
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    override fun deserializeFromJson(json: String, userHandle: String): Boolean {
        if (json.isEmpty()) return false
        return try {
            val jsonArray = JSONArray(json)
            root.children.clear()
            pathNodeMap.clear()
            pathNodeMap["/"] = root

            val tempNodes = mutableListOf<JSONObject>()
            for (i in 0 until jsonArray.length()) {
                tempNodes.add(jsonArray.getJSONObject(i))
            }

            // Sort by depth so parent directories are registered before children
            tempNodes.sortBy { it.getString("path").split("/").size }

            tempNodes.forEach { obj ->
                val name = obj.getString("name")
                val path = obj.getString("path")
                if (path == "/") return@forEach

                val parentPath = obj.getString("parentPath")
                val isDir = obj.getBoolean("isDirectory")
                val fileTypeName = obj.optString("fileType", "TXT")
                val sizeBytes = obj.optLong("sizeBytes", 0L)
                val createdTs = obj.optLong("createdTimestamp", System.currentTimeMillis())
                val modifiedTs = obj.optLong("modifiedTimestamp", System.currentTimeMillis())
                val owner = obj.optString("owner", "operator")
                val permissions = obj.optString("permissions", if (isDir) "drwxr-xr-x" else "-rw-r--r--")
                val isHidden = obj.optBoolean("isHidden", false)
                val isProt = obj.optBoolean("isSystemProtected", false)
                val inTrash = obj.optBoolean("inTrash", false)
                val origPath = obj.optString("originalPath", "").ifEmpty { null }

                val fileType = runCatching { VFSFileType.valueOf(fileTypeName) }.getOrDefault(
                    if (isDir) VFSFileType.DIRECTORY else VFSFileType.fromFileName(name, false)
                )

                val meta = VFSMetadata(
                    name = name,
                    path = path,
                    parentPath = parentPath,
                    fileType = fileType,
                    sizeBytes = sizeBytes,
                    createdTimestamp = createdTs,
                    modifiedTimestamp = modifiedTs,
                    owner = owner,
                    permissions = permissions,
                    isHidden = isHidden,
                    isSystemProtected = isProt,
                    inTrash = inTrash,
                    originalPath = origPath
                )

                val parentDir = getNode(parentPath) as? VFSNode.Directory

                if (isDir) {
                    val dirNode = VFSNode.Directory(metadata = meta)
                    parentDir?.children?.add(dirNode)
                    pathNodeMap[path] = dirNode
                } else {
                    val content = obj.optString("content", "")
                    val isExe = obj.optBoolean("isExecutable", false)
                    val fileNode = VFSNode.File(metadata = meta, content = content, isExecutable = isExe)
                    parentDir?.children?.add(fileNode)
                    pathNodeMap[path] = fileNode
                }
            }

            activeUser = userHandle
            notifyStateChanged()
            true
        } catch (ex: Exception) {
            false
        }
    }
}
