package com.example.backdoor.terminal

data class ParsedCommand(
    val rawInput: String,
    val commandName: String,
    val positionalArgs: List<String>,
    val flags: Set<Char>,
    val longFlags: Set<String>,
    val options: Map<String, String>
) {
    fun hasFlag(flag: Char): Boolean = flags.contains(flag)
    fun hasLongFlag(flag: String): Boolean = longFlags.contains(flag.lowercase())
    
    fun getOption(key: String, default: String? = null): String? {
        return options[key.lowercase()] ?: default
    }
}

data class CommandPipeline(
    val commands: List<ParsedCommand>,
    val pipeType: PipelineType = PipelineType.SINGLE,
    val redirectTarget: String? = null,
    val isAppendRedirect: Boolean = false
)

enum class PipelineType {
    SINGLE,
    PIPE,          // |
    AND,           // &&
    OR             // ||
}

object CommandParser {

    fun parse(rawLine: String): ParsedCommand {
        val trimmed = rawLine.trim()
        if (trimmed.isEmpty()) {
            return ParsedCommand(
                rawInput = rawLine,
                commandName = "",
                positionalArgs = emptyList(),
                flags = emptySet(),
                longFlags = emptySet(),
                options = emptyMap()
            )
        }

        val tokens = tokenize(trimmed)
        if (tokens.isEmpty()) {
            return ParsedCommand(
                rawInput = rawLine,
                commandName = "",
                positionalArgs = emptyList(),
                flags = emptySet(),
                longFlags = emptySet(),
                options = emptyMap()
            )
        }

        val cmdName = tokens.first().lowercase()
        val argTokens = tokens.drop(1)

        val positional = mutableListOf<String>()
        val shortFlags = mutableSetOf<Char>()
        val longFlags = mutableSetOf<String>()
        val options = mutableMapOf<String, String>()

        var i = 0
        while (i < argTokens.size) {
            val token = argTokens[i]
            when {
                token.startsWith("--") -> {
                    val flagBody = token.removePrefix("--")
                    if (flagBody.contains("=")) {
                        val parts = flagBody.split("=", limit = 2)
                        options[parts[0].lowercase()] = parts[1]
                    } else {
                        longFlags.add(flagBody.lowercase())
                    }
                }
                token.startsWith("-") && token.length > 1 && !token.startsWith("--") -> {
                    val chars = token.removePrefix("-")
                    chars.forEach { char -> shortFlags.add(char) }
                }
                else -> {
                    positional.add(token)
                }
            }
            i++
        }

        return ParsedCommand(
            rawInput = rawLine,
            commandName = cmdName,
            positionalArgs = positional,
            flags = shortFlags,
            longFlags = longFlags,
            options = options
        )
    }

    /**
     * Splits a raw line into tokens considering single and double quotes.
     */
    fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        val currentToken = StringBuilder()
        var inDoubleQuotes = false
        var inSingleQuotes = false
        var isEscaped = false

        for (ch in input) {
            if (isEscaped) {
                currentToken.append(ch)
                isEscaped = false
                continue
            }

            if (ch == '\\' && !inSingleQuotes) {
                isEscaped = true
                continue
            }

            if (ch == '"' && !inSingleQuotes) {
                inDoubleQuotes = !inDoubleQuotes
                continue
            }

            if (ch == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes
                continue
            }

            if (ch.isWhitespace() && !inDoubleQuotes && !inSingleQuotes) {
                if (currentToken.isNotEmpty()) {
                    tokens.add(currentToken.toString())
                    currentToken.clear()
                }
            } else {
                currentToken.append(ch)
            }
        }

        if (currentToken.isNotEmpty()) {
            tokens.add(currentToken.toString())
        }

        return tokens
    }

    /**
     * Parse pipeline representation (prepared for future execution).
     */
    fun parsePipeline(rawLine: String): CommandPipeline {
        // Basic pipeline parser dividing by '|' or '&&' or '||'
        val parsedCmd = parse(rawLine)
        return CommandPipeline(
            commands = listOf(parsedCmd),
            pipeType = PipelineType.SINGLE,
            redirectTarget = null,
            isAppendRedirect = false
        )
    }
}
