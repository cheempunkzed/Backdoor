package com.example.backdoor.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class SystemEvent {
    data class NetworkStatusChanged(val isConnected: Boolean) : SystemEvent()
    data class OnionRouteEstablished(val targetOnion: String) : SystemEvent()
    data class FileDownloaded(val filePath: String) : SystemEvent()
    data class AppRequested(val targetApp: com.example.backdoor.game.OsApp, val payload: String? = null) : SystemEvent()
    data class NotificationTriggered(
        val title: String,
        val message: String,
        val level: NotificationLevel = NotificationLevel.INFO,
        val priority: NotificationPriority = NotificationPriority.INFO
    ) : SystemEvent()

    // MISSION OBJECTIVE VALIDATION GAMEPLAY EVENTS
    data class NetworkScanCompleted(val target: String, val portsScanned: List<Int> = emptyList()) : SystemEvent()
    data class ServiceDiscovered(val target: String, val serviceName: String, val port: Int) : SystemEvent()
    data class SecurityScanCompleted(val target: String, val vulnerabilities: List<String> = emptyList()) : SystemEvent()
    data class VulnerabilityExploited(val target: String, val exploitName: String) : SystemEvent()
    data class FileDeleted(val filePath: String) : SystemEvent()
    data class SecurityReportCreated(val reportPath: String) : SystemEvent()
    data class CommandExecuted(val commandLine: String, val commandName: String, val args: List<String> = emptyList()) : SystemEvent()
    data class OnionServiceConnected(val onionUrl: String) : SystemEvent()
    data class ForumPostCreated(val forumId: String, val title: String) : SystemEvent()
    data class NPCInteractionCompleted(val npcId: String, val action: String) : SystemEvent()
    data class TransactionCompleted(val amount: Long, val currency: String, val isIncoming: Boolean, val type: String = "TRANSFER") : SystemEvent()
    data class ItemPurchased(val itemId: String, val itemName: String) : SystemEvent()
    data class ItemSold(val itemId: String, val itemName: String) : SystemEvent()
}

class SystemEventBus {
    private val _events = MutableSharedFlow<SystemEvent>(extraBufferCapacity = 128)
    val events: SharedFlow<SystemEvent> = _events.asSharedFlow()

    fun emit(event: SystemEvent) {
        _events.tryEmit(event)
    }
}
