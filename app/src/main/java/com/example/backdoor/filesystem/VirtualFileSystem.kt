package com.example.backdoor.filesystem

import kotlinx.coroutines.flow.StateFlow

interface VirtualFileSystem {
    val version: String get() = "0.2.0"
    val updateEvent: StateFlow<Long>

    fun getCwd(): String
    fun changeDirectory(path: String): Boolean
    fun getNode(path: String): VFSNode?
    fun listDirectory(
        path: String = getCwd(),
        includeHidden: Boolean = false,
        includeTrash: Boolean = false
    ): List<VFSNode>?

    fun readFile(path: String): String?
    fun writeFile(path: String, content: String, userHandle: String = "operator"): Boolean
    fun createFile(
        dirPath: String,
        fileName: String,
        content: String = "",
        isExecutable: Boolean = false,
        owner: String = "operator"
    ): Boolean

    fun createDirectory(
        dirPath: String,
        dirName: String,
        owner: String = "operator"
    ): Boolean

    fun deleteNode(path: String, permanent: Boolean = false, userHandle: String = "operator"): Boolean
    fun restoreFromTrash(trashPath: String, userHandle: String = "operator"): Boolean
    fun emptyTrash(userHandle: String = "operator"): Boolean

    fun copyNode(srcPath: String, dstPath: String, userHandle: String = "operator"): Boolean
    fun moveNode(srcPath: String, dstPath: String, userHandle: String = "operator"): Boolean
    fun renameNode(path: String, newName: String, userHandle: String = "operator"): Boolean

    fun findFiles(query: String, startPath: String = "/"): List<VFSNode>
    fun resolvePath(targetPath: String): String
    fun getTreeString(path: String = getCwd(), maxDepth: Int = 3): String

    fun setupUserHome(username: String)
    fun serializeToJson(): String
    fun deserializeFromJson(json: String, userHandle: String = "operator"): Boolean
    fun logAuditAction(action: String, path: String, userHandle: String = "operator")
}
