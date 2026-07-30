package com.example.backdoor.filesystem

interface VirtualFileSystem {
    fun getCwd(): String
    fun changeDirectory(path: String): Boolean
    fun getNode(path: String): VFSNode?
    fun listDirectory(path: String = getCwd()): List<VFSNode>?
    fun readFile(path: String): String?
    fun createFile(dirPath: String, fileName: String, content: String = "", isExecutable: Boolean = false): Boolean
    fun createDirectory(dirPath: String, dirName: String): Boolean
    fun deleteNode(path: String): Boolean
    fun resolvePath(targetPath: String): String
    fun getTreeString(path: String = getCwd(), maxDepth: Int = 3): String
}
