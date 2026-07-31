package com.example.backdoor.terminal

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.i18n.StringManager

sealed class TerminalException(override val message: String) : Exception(message) {
    class CommandNotFound(val command: String) :
        TerminalException(StringManager.get(StringKey.CMD_NOT_FOUND, command))

    class PermissionDenied(val path: String? = null) :
        TerminalException(StringManager.get(StringKey.PERM_DENIED, path ?: "restricted area"))

    class FileNotFound(val path: String) :
        TerminalException(StringManager.get(StringKey.FILE_NOT_FOUND, path))

    class DirectoryNotFound(val path: String) :
        TerminalException(StringManager.get(StringKey.DIR_NOT_FOUND, path))

    class AccessDenied(val resource: String) :
        TerminalException(StringManager.get(StringKey.ACCESS_DENIED, resource))

    class InvalidArgument(val arg: String, val reason: String = "") :
        TerminalException(StringManager.get(StringKey.INVALID_ARG, arg, reason))

    class PathAlreadyExists(val path: String) :
        TerminalException(StringManager.get(StringKey.PATH_ALREADY_EXISTS, path))

    class NotADirectory(val path: String) :
        TerminalException(StringManager.get(StringKey.NOT_A_DIRECTORY, path))

    class NotAFile(val path: String) :
        TerminalException(StringManager.get(StringKey.NOT_A_FILE, path))

    class CommandExecution(val commandName: String, val causeMessage: String) :
        TerminalException(StringManager.get(StringKey.CMD_EXEC_ERROR, commandName, causeMessage))
}
