package com.example.backdoor.filesystem

enum class VFSFileType(val extension: String, val description: String) {
    DIRECTORY("", "Directory"),
    TXT(".txt", "Text Document"),
    LOG(".log", "System Log"),
    CFG(".cfg", "Configuration File"),
    SYS(".sys", "System Binary"),
    KEY(".key", "Encryption Key"),
    ENC(".enc", "Encrypted File"),
    EXE(".exe", "Executable Binary"),
    SH(".sh", "Shell Script"),
    NET(".net", "Network Config"),
    TMP(".tmp", "Temporary File");

    companion object {
        fun fromFileName(fileName: String, isDirectory: Boolean): VFSFileType {
            if (isDirectory) return DIRECTORY
            val ext = fileName.substringAfterLast('.', "").lowercase()
            return values().find { it.extension == ".$ext" } ?: TXT
        }
    }
}

enum class VFSSortMode {
    NAME_ASC,
    NAME_DESC,
    SIZE_ASC,
    SIZE_DESC,
    DATE_ASC,
    DATE_DESC,
    TYPE
}

data class VFSMetadata(
    val name: String,
    val path: String,
    val parentPath: String,
    val fileType: VFSFileType,
    val sizeBytes: Long = 0L,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val modifiedTimestamp: Long = System.currentTimeMillis(),
    val owner: String = "root",
    val permissions: String = if (fileType == VFSFileType.DIRECTORY) "drwxr-xr-x" else "-rw-r--r--",
    val isHidden: Boolean = false,
    val isSystemProtected: Boolean = false,
    val inTrash: Boolean = false,
    val originalPath: String? = null
)
