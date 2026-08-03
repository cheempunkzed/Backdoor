package com.example.backdoor.terminal.commands

import com.example.backdoor.i18n.StringKey
import com.example.backdoor.terminal.Command
import com.example.backdoor.terminal.CommandCategory
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandResult
import com.example.backdoor.terminal.ManPage
import com.example.backdoor.terminal.ParsedCommand

/**
 * Terminal command `onion` (aliases `darknet`, `relays`) for inspecting and interacting with the Dark Layer.
 */
class OnionCommand : Command {
    override val name: String = "onion"
    override val category: CommandCategory = CommandCategory.NETWORK
    override val descriptionKey: StringKey = StringKey.NETSTAT_DESC
    override val aliases: List<String> = listOf("darknet", "relays")
    override val usage: String = "onion <status|circuits|services|search|peers|identity|forum|pm|connect> [args...]"
    override val manPage: ManPage = ManPage(
        name = "onion",
        synopsis = "onion <status|circuits|services|search|peers|identity|forum|pm|connect> [options]",
        description = "Interacts with the AbyssNet Dark Layer multi-hop onion routing network, digital identities, encrypted PMs, underground forums, and factions.",
        options = listOf(
            "status" to "Check circuit status, active digital identity, heat level, and faction standings",
            "circuits" to "List active multi-hop relay nodes and bandwidth",
            "services" to "Display all indexed .onion hidden services",
            "search <query>" to "Search hidden services, forums, and market listings",
            "peers" to "List online darknet operators and NPC identities",
            "identity <list|create|switch> [name]" to "Manage alias identities and active PGP keys",
            "forum <list|read|post> [args...]" to "Interact with underground forum threads",
            "pm <inbox|read|send> [args...]" to "Read or send encrypted private messages",
            "connect <address>" to "Establish multi-hop encrypted connection to hidden service"
        ),
        examples = listOf(
            "onion status",
            "onion search zero-day",
            "onion identity switch cipher_ghost",
            "onion forum list",
            "onion pm inbox",
            "onion connect blackvault.onion"
        )
    )

    override suspend fun execute(parsed: ParsedCommand, context: CommandContext): CommandResult {
        val subCmd = parsed.positionalArgs.firstOrNull()?.lowercase() ?: "status"
        val engine = context.onionEngine

        return when (subCmd) {
            "status" -> {
                val sb = StringBuilder()
                val identity = engine?.activeIdentity?.value
                val rep = engine?.playerReputation?.value
                sb.appendLine("=== ABYSSNET ONION NETWORK SYSTEM STATUS ===")
                sb.appendLine("Circuit State    : ACTIVE (3-Hop Tunnel RSA-4096 / AES-256-GCM)")
                sb.appendLine("Active Identity  : ${identity?.nickname ?: "operator"} [PGP: ${identity?.pgpFingerprint ?: "N/A"}]")
                sb.appendLine("Criminal Heat    : ${identity?.criminalHeat ?: 0}% | Suspicion Risk: ${rep?.suspicion ?: 0}")
                sb.appendLine("Trust Score      : ${identity?.trustScore ?: 150} | Fame: ${rep?.fame ?: 60} | Rank: ${rep?.rank?.title ?: "Novice"}")
                if (engine != null) {
                    sb.appendLine("\n--- UNDERGROUND FACTION STANDINGS ---")
                    engine.factions.value.forEach { f ->
                        val standing = engine.getFactionStanding(f.id)
                        sb.appendLine("  • ${f.tag.padEnd(8)} ${f.name.padEnd(24)} Tier: ${standing.trustTier.padEnd(10)} Score: ${standing.reputation}")
                    }
                }
                CommandResult(output = sb.toString().trimEnd())
            }

            "circuits", "nodes" -> {
                val sb = StringBuilder()
                sb.appendLine("=== ABYSSNET MULTI-HOP RELAY CIRCUITS ===")
                val nodes = engine?.relayNodes?.value ?: emptyList()
                if (nodes.isEmpty()) {
                    sb.appendLine("  [1] Entry:  OnionRelay-Alpha   [185.220.101.5] (Iceland - 1000 Mbps)")
                    sb.appendLine("  [2] Middle: OnionRelay-Bravo   [199.249.230.8] (Switzerland - 750 Mbps)")
                    sb.appendLine("  [3] Exit:   ExitNode-Delta     [109.70.100.15] (Netherlands - 500 Mbps)")
                } else {
                    nodes.forEachIndexed { i, node ->
                        val role = if (node.isExitNode) "Exit" else if (i == 0) "Entry" else "Middle"
                        sb.appendLine("  [${i + 1}] ${role.padEnd(6)}: ${node.alias.padEnd(18)} [${node.ip}] (${node.country} - ${node.bandwidthMbps} Mbps)")
                    }
                }
                CommandResult(output = sb.toString().trimEnd())
            }

            "services" -> {
                val services = engine?.hiddenServices?.value ?: emptyList()
                val sb = StringBuilder()
                sb.appendLine("=== ABYSSNET INDEXED HIDDEN SERVICES (${services.size}) ===")
                services.forEach { s ->
                    sb.appendLine("  • ${s.address.padEnd(22)} [${s.type.displayName.padEnd(16)}] Clearance: Tier ${s.accessLevel}")
                }
                sb.appendLine("\nAccess in Abyss Browser by searching the .onion domain.")
                CommandResult(output = sb.toString().trimEnd())
            }

            "search" -> {
                val query = parsed.positionalArgs.drop(1).joinToString(" ").lowercase()
                if (query.isEmpty()) return CommandResult(output = "Usage: onion search <keyword>")
                val sb = StringBuilder()
                sb.appendLine("=== ONION SEARCH RESULTS FOR: '$query' ===")

                val matchedServices = engine?.hiddenServices?.value?.filter {
                    it.name.lowercase().contains(query) || it.description.lowercase().contains(query) || it.address.lowercase().contains(query)
                } ?: emptyList()

                val matchedThreads = engine?.forumThreads?.value?.filter {
                    it.title.lowercase().contains(query) || it.category.lowercase().contains(query)
                } ?: emptyList()

                sb.appendLine("Hidden Services (${matchedServices.size}):")
                matchedServices.forEach { sb.appendLine("  • ${it.address} - ${it.name}") }

                sb.appendLine("\nForum Threads (${matchedThreads.size}):")
                matchedThreads.forEach { sb.appendLine("  • [ID: ${it.id}] ${it.title} (by @${it.authorHandle})") }

                CommandResult(output = sb.toString().trimEnd())
            }

            "peers" -> {
                val npcs = engine?.npcIdentities?.value ?: emptyList()
                val sb = StringBuilder()
                sb.appendLine("=== ONLINE DARKNET OPERATORS & PEERS (${npcs.size}) ===")
                npcs.forEach { npc ->
                    sb.appendLine("  • @${npc.handle.padEnd(16)} Role: ${npc.personality.padEnd(26)} Rank: ${npc.reputation.rank.title}")
                }
                CommandResult(output = sb.toString().trimEnd())
            }

            "identity", "id" -> {
                val action = parsed.positionalArgs.getOrNull(1)?.lowercase() ?: "current"
                when (action) {
                    "current" -> {
                        val active = engine?.activeIdentity?.value
                        CommandResult(output = "Current Active Identity: @${active?.nickname ?: "operator"}\nPGP Fingerprint: ${active?.pgpFingerprint}\nProfile: ${active?.hiddenProfile}")
                    }
                    "list" -> {
                        val sb = StringBuilder()
                        sb.appendLine("=== REGISTERED DIGITAL IDENTITIES ===")
                        engine?.identities?.value?.forEach { id ->
                            val activeTag = if (id.id == engine.activeIdentity.value.id) " [ACTIVE]" else ""
                            sb.appendLine("  • @${id.nickname.padEnd(16)} PGP: ${id.pgpFingerprint}$activeTag")
                        }
                        CommandResult(output = sb.toString().trimEnd())
                    }
                    "create" -> {
                        val name = parsed.positionalArgs.getOrNull(2) ?: return CommandResult(output = "Usage: onion identity create <nickname>")
                        val created = engine?.createIdentity(name, "Custom created cipher identity")
                        CommandResult(output = "Created new digital identity @${created?.nickname} [PGP: ${created?.pgpFingerprint}]")
                    }
                    "switch" -> {
                        val target = parsed.positionalArgs.getOrNull(2) ?: return CommandResult(output = "Usage: onion identity switch <nickname>")
                        val success = engine?.switchIdentity(target) ?: false
                        if (success) {
                            CommandResult(output = "Switched active identity to @$target.")
                        } else {
                            CommandResult(output = "Identity '$target' not found. Use 'onion identity list' to see available aliases.")
                        }
                    }
                    else -> CommandResult(output = "Usage: onion identity <current|list|create|switch> [name]")
                }
            }

            "forum" -> {
                val action = parsed.positionalArgs.getOrNull(1)?.lowercase() ?: "list"
                when (action) {
                    "list" -> {
                        val threads = engine?.forumThreads?.value ?: emptyList()
                        val sb = StringBuilder()
                        sb.appendLine("=== ABYSS UNDERGROUND FORUM THREADS (${threads.size}) ===")
                        threads.forEach { t ->
                            sb.appendLine("  • [ID: ${t.id}] ${t.title} (@${t.authorHandle}) - ${t.posts.size} posts")
                        }
                        CommandResult(output = sb.toString().trimEnd())
                    }
                    "read" -> {
                        val threadId = parsed.positionalArgs.getOrNull(2) ?: return CommandResult(output = "Usage: onion forum read <thread_id>")
                        val thread = engine?.forumThreads?.value?.find { it.id == threadId }
                            ?: return CommandResult(output = "Thread ID '$threadId' not found.")
                        val sb = StringBuilder()
                        sb.appendLine("=== THREAD: ${thread.title} ===")
                        sb.appendLine("Category: ${thread.category} | Author: @${thread.authorHandle}\n")
                        thread.posts.forEach { p ->
                            sb.appendLine("[@${p.authorHandle}]: ${p.content}\n---")
                        }
                        CommandResult(output = sb.toString().trimEnd())
                    }
                    "post" -> {
                        val title = parsed.positionalArgs.getOrNull(2) ?: return CommandResult(output = "Usage: onion forum post <title> <category> <content>")
                        val cat = parsed.positionalArgs.getOrNull(3) ?: "General"
                        val content = parsed.positionalArgs.drop(4).joinToString(" ")
                        if (content.isEmpty()) return CommandResult(output = "Content cannot be empty.")
                        val created = engine?.createNewThread(title, cat, content)
                        CommandResult(output = "Created forum thread #${created?.id}: '${created?.title}'")
                    }
                    else -> CommandResult(output = "Usage: onion forum <list|read|post> [args...]")
                }
            }

            "pm" -> {
                val action = parsed.positionalArgs.getOrNull(1)?.lowercase() ?: "inbox"
                when (action) {
                    "inbox" -> {
                        val msgs = engine?.encryptedMessages?.value ?: emptyList()
                        val sb = StringBuilder()
                        sb.appendLine("=== ENCRYPTED PRIVATE MESSAGES INBOX (${msgs.size}) ===")
                        msgs.forEach { m ->
                            val status = if (m.isRead) "[READ]" else "[UNREAD]"
                            sb.appendLine("  • [ID: ${m.id.take(8)}] $status From: @${m.senderHandle.padEnd(14)} Subject: ${m.subject}")
                        }
                        CommandResult(output = sb.toString().trimEnd())
                    }
                    "read" -> {
                        val msgId = parsed.positionalArgs.getOrNull(2) ?: return CommandResult(output = "Usage: onion pm read <msg_id>")
                        val msg = engine?.encryptedMessages?.value?.find { it.id.startsWith(msgId) }
                            ?: return CommandResult(output = "Message ID starting with '$msgId' not found.")
                        engine.markMessageAsRead(msg.id)
                        val sb = StringBuilder()
                        sb.appendLine("=== ENCRYPTED MESSAGE ===")
                        sb.appendLine("From: @${msg.senderHandle}")
                        sb.appendLine("To  : @${msg.recipientHandle}")
                        sb.appendLine("Subject: ${msg.subject}")
                        sb.appendLine("PGP Sig: ${msg.pgpSignature}")
                        sb.appendLine("\n${msg.body}")
                        if (msg.attachmentItemOrKey != null) {
                            sb.appendLine("\nAttachment Key: ${msg.attachmentItemOrKey}")
                        }
                        CommandResult(output = sb.toString().trimEnd())
                    }
                    "send" -> {
                        val to = parsed.positionalArgs.getOrNull(2) ?: return CommandResult(output = "Usage: onion pm send <recipient> <subject> <body>")
                        val subject = parsed.positionalArgs.getOrNull(3) ?: "Encrypted Communication"
                        val body = parsed.positionalArgs.drop(4).joinToString(" ")
                        if (body.isEmpty()) return CommandResult(output = "Message body cannot be empty.")
                        val sent = engine?.sendPrivateMessage(to, subject, body)
                        CommandResult(output = "Encrypted message sent to @$to [PGP Signed: ${sent?.pgpSignature}]")
                    }
                    else -> CommandResult(output = "Usage: onion pm <inbox|read|send> [args...]")
                }
            }

            "connect" -> {
                val target = parsed.positionalArgs.getOrNull(1) ?: "dir.onion"
                context.eventBus?.emit(com.example.backdoor.core.SystemEvent.OnionRouteEstablished(target))
                context.eventBus?.emit(com.example.backdoor.core.SystemEvent.AppRequested(com.example.backdoor.game.OsApp.BROWSER))
                CommandResult(output = "[ONION ROUTER] Multi-hop route built for $target. Launching session in Abyss Browser.")
            }

            else -> {
                CommandResult(output = "Unknown subcommand '$subCmd'. Usage: onion <status|circuits|services|search|peers|identity|forum|pm|connect>")
            }
        }
    }
}
