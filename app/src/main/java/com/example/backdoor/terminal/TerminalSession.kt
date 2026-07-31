package com.example.backdoor.terminal

import com.example.backdoor.filesystem.VirtualFileSystem

enum class PromptStyle(val label: String) {
    DEFAULT("user@host:path$"),
    SHORT("path$"),
    MINIMAL(">$"),
    CYBER("[USER@HOST ~]#")
}

data class TerminalSettings(
    val cursorBlink: Boolean = true,
    val fontSize: Int = 13,
    val terminalOpacity: Float = 1.0f,
    val promptStyle: PromptStyle = PromptStyle.DEFAULT,
    val textColorHex: String = "#00FF66",
    val animationSpeed: Float = 1.0f
)

class TerminalSession(
    initialUser: String = "operator",
    initialHostname: String = "abyss-node-01",
    val vfs: VirtualFileSystem
) {
    val envVars = mutableMapOf<String, String>()
    val aliases = mutableMapOf<String, String>()

    init {
        envVars["USER"] = initialUser
        envVars["HOSTNAME"] = initialHostname
        envVars["HOME"] = "/home/$initialUser"
        envVars["PWD"] = vfs.getCwd()
        envVars["PATH"] = "/bin:/usr/bin:/sbin"
        envVars["SHELL"] = "/bin/abyss-sh"
    }

    fun getEnv(key: String): String = envVars[key.uppercase()] ?: ""

    fun setEnv(key: String, value: String) {
        envVars[key.uppercase()] = value
    }

    fun expandVariables(input: String): String {
        var result = input
        envVars.forEach { (k, v) ->
            result = result.replace("$$k", v)
        }
        return result
    }

    fun formatPrompt(style: PromptStyle): String {
        val user = getEnv("USER").ifEmpty { "operator" }
        val host = getEnv("HOSTNAME").ifEmpty { "abyss" }
        val cwd = vfs.getCwd()

        return when (style) {
            PromptStyle.DEFAULT -> "$user@$host:$cwd$ "
            PromptStyle.SHORT -> "$cwd$ "
            PromptStyle.MINIMAL -> "> "
            PromptStyle.CYBER -> "[$user@$host $cwd]# "
        }
    }
}
