package com.example.backdoor.core

import com.example.backdoor.game.OsApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ApplicationDisplayMode {
    FULLSCREEN,
    WINDOWED,
    MINIMIZED
}

data class WindowState(
    val windowId: String,
    val app: OsApp,
    val title: String,
    val isMinimized: Boolean = false,
    val isFocused: Boolean = true,
    val zIndex: Int = 1,
    val displayMode: ApplicationDisplayMode = ApplicationDisplayMode.FULLSCREEN,
    val positionX: Float = 0f,
    val positionY: Float = 0f
)

class WindowManager(
    private val processManager: ProcessManager
) {
    private val _windows = MutableStateFlow<List<WindowState>>(emptyList())
    val windows: StateFlow<List<WindowState>> = _windows.asStateFlow()

    private var maxZIndex = 1

    fun openOrFocusApp(
        app: OsApp,
        mode: ApplicationDisplayMode = ApplicationDisplayMode.FULLSCREEN
    ): WindowState {
        processManager.spawnProcessForApp(app)

        val existing = _windows.value.find { it.app == app }
        if (existing != null) {
            maxZIndex++
            val targetMode = if (mode == ApplicationDisplayMode.MINIMIZED) {
                if (existing.displayMode == ApplicationDisplayMode.MINIMIZED) ApplicationDisplayMode.FULLSCREEN else existing.displayMode
            } else {
                mode
            }
            val updatedList = _windows.value.map { w ->
                if (w.app == app) {
                    w.copy(
                        isMinimized = false,
                        isFocused = true,
                        displayMode = targetMode,
                        zIndex = maxZIndex
                    )
                } else {
                    w.copy(isFocused = false)
                }
            }
            _windows.value = updatedList
            return updatedList.first { it.app == app }
        }

        maxZIndex++
        val initialMode = if (mode == ApplicationDisplayMode.MINIMIZED) ApplicationDisplayMode.FULLSCREEN else mode
        val newWindow = WindowState(
            windowId = "win_${app.name.lowercase()}_${System.currentTimeMillis()}",
            app = app,
            title = app.appName,
            isMinimized = false,
            isFocused = true,
            displayMode = initialMode,
            zIndex = maxZIndex
        )

        val updatedList = _windows.value.map { it.copy(isFocused = false) } + newWindow
        _windows.value = updatedList
        return newWindow
    }

    fun setDisplayMode(app: OsApp, mode: ApplicationDisplayMode) {
        _windows.value = _windows.value.map { w ->
            if (w.app == app) {
                when (mode) {
                    ApplicationDisplayMode.MINIMIZED -> w.copy(
                        isMinimized = true,
                        isFocused = false
                    )
                    ApplicationDisplayMode.FULLSCREEN -> {
                        maxZIndex++
                        w.copy(
                            isMinimized = false,
                            isFocused = true,
                            displayMode = ApplicationDisplayMode.FULLSCREEN,
                            zIndex = maxZIndex
                        )
                    }
                    ApplicationDisplayMode.WINDOWED -> {
                        maxZIndex++
                        w.copy(
                            isMinimized = false,
                            isFocused = true,
                            displayMode = ApplicationDisplayMode.WINDOWED,
                            zIndex = maxZIndex
                        )
                    }
                }
            } else {
                if (mode != ApplicationDisplayMode.MINIMIZED) w.copy(isFocused = false) else w
            }
        }
    }

    fun toggleDisplayMode(app: OsApp) {
        val existing = _windows.value.find { it.app == app } ?: return
        val newMode = if (existing.displayMode == ApplicationDisplayMode.FULLSCREEN) {
            ApplicationDisplayMode.WINDOWED
        } else {
            ApplicationDisplayMode.FULLSCREEN
        }
        setDisplayMode(app, newMode)
    }

    fun bringToFront(app: OsApp) {
        val existing = _windows.value.find { it.app == app } ?: return
        maxZIndex++
        _windows.value = _windows.value.map { w ->
            if (w.app == app) {
                val mode = if (w.displayMode == ApplicationDisplayMode.MINIMIZED) ApplicationDisplayMode.FULLSCREEN else w.displayMode
                w.copy(isMinimized = false, isFocused = true, displayMode = mode, zIndex = maxZIndex)
            } else {
                w.copy(isFocused = false)
            }
        }
    }

    fun minimizeWindow(app: OsApp) {
        setDisplayMode(app, ApplicationDisplayMode.MINIMIZED)
    }

    fun restoreWindow(app: OsApp, preferredMode: ApplicationDisplayMode = ApplicationDisplayMode.FULLSCREEN) {
        val existing = _windows.value.find { it.app == app }
        val modeToUse = if (existing != null && existing.displayMode != ApplicationDisplayMode.MINIMIZED) {
            existing.displayMode
        } else {
            preferredMode
        }
        setDisplayMode(app, modeToUse)
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

    fun updateWindowPosition(app: OsApp, x: Float, y: Float) {
        _windows.value = _windows.value.map { w ->
            if (w.app == app) w.copy(positionX = x, positionY = y) else w
        }
    }
}
