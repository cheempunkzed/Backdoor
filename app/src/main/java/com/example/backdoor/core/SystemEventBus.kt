package com.example.backdoor.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

sealed class SystemEvent {
    data class NetworkStatusChanged(val isConnected: Boolean) : SystemEvent()
    data class OnionRouteEstablished(val targetOnion: String) : SystemEvent()
    data class FileDownloaded(val filePath: String) : SystemEvent()
    data class AppRequested(val targetApp: com.example.backdoor.game.OsApp, val payload: String? = null) : SystemEvent()
    data class NotificationTriggered(val title: String, val message: String, val level: NotificationLevel = NotificationLevel.INFO) : SystemEvent()
}

class SystemEventBus {
    private val _events = MutableSharedFlow<SystemEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<SystemEvent> = _events.asSharedFlow()

    fun emit(event: SystemEvent) {
        _events.tryEmit(event)
    }
}
