package com.example.backdoor.filesystem

sealed class VFSNode {
    abstract val metadata: VFSMetadata
    val name: String get() = metadata.name
    val path: String get() = metadata.path
    val parentPath: String get() = metadata.parentPath
    val permissions: String get() = metadata.permissions
    val createdTime: Long get() = metadata.createdTimestamp

    data class Directory(
        override val metadata: VFSMetadata,
        val children: MutableList<VFSNode> = mutableListOf()
    ) : VFSNode() {
        constructor(
            name: String,
            path: String,
            parentPath: String,
            permissions: String = "drwxr-xr-x",
            createdTime: Long = System.currentTimeMillis(),
            owner: String = "root",
            isSystemProtected: Boolean = false,
            inTrash: Boolean = false,
            originalPath: String? = null
        ) : this(
            metadata = VFSMetadata(
                name = name,
                path = path,
                parentPath = parentPath,
                fileType = VFSFileType.DIRECTORY,
                sizeBytes = 0L,
                createdTimestamp = createdTime,
                modifiedTimestamp = createdTime,
                owner = owner,
                permissions = permissions,
                isSystemProtected = isSystemProtected,
                inTrash = inTrash,
                originalPath = originalPath
            )
        )
    }

    data class File(
        override val metadata: VFSMetadata,
        var content: String = "",
        val isExecutable: Boolean = false
    ) : VFSNode() {
        constructor(
            name: String,
            path: String,
            parentPath: String,
            permissions: String = "-rw-r--r--",
            createdTime: Long = System.currentTimeMillis(),
            content: String = "",
            isExecutable: Boolean = false,
            owner: String = "root",
            isSystemProtected: Boolean = false,
            inTrash: Boolean = false,
            originalPath: String? = null
        ) : this(
            metadata = VFSMetadata(
                name = name,
                path = path,
                parentPath = parentPath,
                fileType = VFSFileType.fromFileName(name, false),
                sizeBytes = content.toByteArray().size.toLong(),
                createdTimestamp = createdTime,
                modifiedTimestamp = createdTime,
                owner = owner,
                permissions = permissions,
                isSystemProtected = isSystemProtected,
                inTrash = inTrash,
                originalPath = originalPath
            ),
            content = content,
            isExecutable = isExecutable
        )
    }
}

val VFSNode.isDirectory: Boolean get() = this is VFSNode.Directory
val VFSNode.size: Long get() = metadata.sizeBytes
val VFSNode.modifiedTime: Long get() = metadata.modifiedTimestamp
val VFSNode.owner: String get() = metadata.owner
val VFSNode.isProtected: Boolean get() = metadata.isSystemProtected
val VFSNode.isExecutable: Boolean get() = (this is VFSNode.File && this.isExecutable) || metadata.permissions.contains("x")

