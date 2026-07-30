package com.example.backdoor.filesystem

sealed class VFSNode {
    abstract val name: String
    abstract val path: String
    abstract val parentPath: String
    abstract val permissions: String
    abstract val createdTime: Long

    data class Directory(
        override val name: String,
        override val path: String,
        override val parentPath: String,
        override val permissions: String = "drwxr-xr-x",
        override val createdTime: Long = System.currentTimeMillis(),
        val children: MutableList<VFSNode> = mutableListOf()
    ) : VFSNode()

    data class File(
        override val name: String,
        override val path: String,
        override val parentPath: String,
        override val permissions: String = "-rw-r--r--",
        override val createdTime: Long = System.currentTimeMillis(),
        var content: String = "",
        val isExecutable: Boolean = false
    ) : VFSNode()
}
