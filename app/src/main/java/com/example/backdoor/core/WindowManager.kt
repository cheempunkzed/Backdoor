package com.example.backdoor.core

import com.example.backdoor.game.OsApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WindowState(
    val windowId: String,
    val app: OsApp,
    val title: String,
    val isMinimized: Boolean = false,
    val isFocused: Boolean = true,
    val zIndex: Int = 1
)

class WindowManager(
    private val processManager: ProcessManager
) {
    private val _windows = MutableStateFlow<List<WindowState>>(emptyList())
    val windows: StateFlow<List<WindowState>> = _windows.asStateFlow()

    private var maxZIndex = 1

    fun openOrFocusApp(app: OsApp): WindowState {
        processManager.spawnProcessForApp(app)

        val existing = _windows.value.find { it.app == app }
        if (existing != null) {
            maxZIndex++
            val updatedList = _windows.value.map { w ->
                if (w.app == app) {
                    w.copy(isMinimized = false, isFocused = true, zIndex = maxZIndex)
                } else {
                    w.copy(isFocused = false)
                }
            }
            _windows.value = updatedList
            return updatedList.first { it.app == app }
        }

        maxZIndex++
        val newWindow = WindowState(
            windowId = "win_${app.name.lowercase()}_${System.currentTimeMillis()}",
            app = app,
            title = app.appName,
            isMinimized = false,
            isFocused = true,
            zIndex = maxZIndex
        )

        val updatedList = _windows.value.map { it.copy(isFocused = false) } + newWindow
        _windows.value = updatedList
        return newWindow
    }

    fun bringToFront(app: OsApp) {
        val existing = _windows.value.find { it.app == app } ?: return
        maxZIndex++
        _windows.value = _windows.value.map { w ->
            if (w.app == app) {
                w.copy(isMinimized = false, isFocused = true, zIndex = maxZIndex)
            } else {
                w.copy(isFocused = false)
            }
        }
    }

    fun minimizeWindow(app: OsApp) {
        _windows.value = _windows.value.map { w ->
            if (w.app == app) {
                w.copy(isMinimized = true, isFocused = false)
            } else w
        }
    }

    fun restoreWindow(app: OsApp) {
        bringToFront(app)
    }

    fun closeWindow(app: OsApp) {
        _windows.value = _windows.value.filterNot { it.app == app }
        processManager.terminateProcessByApp(app)

        // Focus top remaining non-minimized window if any
        val remaining = _windows.value.filterNot { it.isMinimized }.maxByOrNull { it.zIndex }
        if (remaining != null) {
            bringToFront(remaining.app)
        }
    }

    fun closeFocusedWindow() {
        val focused = _windows.value.find { it.isFocused }
        if (focused != null) {
            closeWindow(focused.app)
        }
    }

    fun isAppOpen(app: OsApp): Boolean {
        return _windows.value.any { it.app == app }
    }

    fun getFocusedApp(): OsApp? {
        return _windows.value.find { it.isFocused && !it.isMinimized }?.app
    }
}
