package com.example.backdoor.game

import com.example.backdoor.core.LogLevel
import com.example.backdoor.core.NotificationLevel
import com.example.backdoor.core.OsNotification
import com.example.backdoor.core.SystemLog
import com.example.backdoor.core.SystemStatus
import com.example.backdoor.filesystem.InMemoryVirtualFileSystem
import com.example.backdoor.filesystem.VirtualFileSystem
import com.example.backdoor.save.MemorySaveManager
import com.example.backdoor.save.SaveManager
import com.example.backdoor.save.UserProfile
import com.example.backdoor.settings.SettingsRepository
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandExecutor
import com.example.backdoor.terminal.CommandRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class OsState {
    BOOTING,
    LOGIN,
    DESKTOP
}

enum class OsApp(val appName: String, val iconName: String) {
    TERMINAL("Terminal", "ic_terminal"),
    FILES("Files", "ic_folder"),
    BROWSER("Browser", "ic_globe"),
    NETWORK("Network", "ic_wifi"),
    DARKNET("DarkNet", "ic_security"),
    SETTINGS("Settings", "ic_settings"),
    LOGS("Logs", "ic_list"),
    SYSTEM_MONITOR("System Monitor", "ic_monitor")
}

data class BootLogEntry(
    val text: String,
    val isDone: Boolean = false
)

class AbyssOSManager(
    val scope: CoroutineScope,
    val vfs: VirtualFileSystem = InMemoryVirtualFileSystem(),
    val settingsRepository: SettingsRepository = SettingsRepository(),
    val saveManager: SaveManager = MemorySaveManager()
) {
    val commandRegistry = CommandRegistry()
    val commandExecutor = CommandExecutor(commandRegistry)

    private val _osState = MutableStateFlow(OsState.BOOTING)
    val osState: StateFlow<OsState> = _osState.asStateFlow()

    private val _bootLogs = MutableStateFlow<List<BootLogEntry>>(emptyList())
    val bootLogs: StateFlow<List<BootLogEntry>> = _bootLogs.asStateFlow()

    private val _systemStatus = MutableStateFlow(SystemStatus())
    val systemStatus: StateFlow<SystemStatus> = _systemStatus.asStateFlow()

    private val _activeApp = MutableStateFlow<OsApp?>(null)
    val activeApp: StateFlow<OsApp?> = _activeApp.asStateFlow()

    private val _systemLogs = MutableStateFlow<List<SystemLog>>(emptyList())
    val systemLogs: StateFlow<List<SystemLog>> = _systemLogs.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _activeNotification = MutableStateFlow<OsNotification?>(null)
    val activeNotification: StateFlow<OsNotification?> = _activeNotification.asStateFlow()

    private val _brightnessAlpha = MutableStateFlow(0f) // 0f is full brightness, up to 0.7f dimming
    val brightnessAlpha: StateFlow<Float> = _brightnessAlpha.asStateFlow()

    private var notificationJob: Job? = null
    private var tickerJob: Job? = null

    init {
        addSystemLog("KERNEL", "AbyssOS 0.2.0 Manager initialized.", LogLevel.INFO)
        startStatusTicker()

        scope.launch {
            val profile = saveManager.getUserProfile()
            _userProfile.value = profile

            // Restore saved VFS JSON state
            val vfsJson = saveManager.getVfsDataJson()
            if (!vfsJson.isNullOrEmpty()) {
                vfs.deserializeFromJson(vfsJson, profile?.username ?: "operator")
            }

            if (profile != null) {
                settingsRepository.updateUserHandle(profile.username)
                setupUserVfsDirectory(profile.username)
            }
        }

        // Auto-save VFS state on changes
        scope.launch {
            vfs.updateEvent.collect {
                val json = vfs.serializeToJson()
                saveManager.saveVfsDataJson(json)
            }
        }
    }

    fun startBootSequence() {
        _osState.value = OsState.BOOTING
        _bootLogs.value = emptyList()

        scope.launch {
            val lines = listOf(
                "AbyssOS Boot Sequence",
                "Version 0.2.0",
                "",
                "Initializing Kernel...",
                "Loading Virtual Memory...",
                "Mounting AbyssFS Virtual File System...",
                "Starting Network Stack...",
                "Loading Security Modules...",
                "Initializing Terminal & Commands...",
                "Verifying File System Integrity...",
                "Launching Desktop Environment...",
                "",
                "Boot completed successfully."
            )

            val currentList = mutableListOf<BootLogEntry>()

            for (line in lines) {
                currentList.add(
                    BootLogEntry(
                        text = line,
                        isDone = line == "Boot completed successfully."
                    )
                )
                _bootLogs.value = currentList.toList()
                if (line.isNotEmpty()) {
                    addSystemLog("BOOT", line, LogLevel.DEBUG)
                }
                // Realistic variable operational delays totaling ~4.5 - 5.5s
                val itemDelay = if (line.isEmpty()) 150L else Random.nextLong(280L, 520L)
                delay(itemDelay)
            }

            delay(600L)
            _osState.value = OsState.LOGIN
            addSystemLog("KERNEL", "Boot sequence complete. Redirected to Authentication.", LogLevel.INFO)
        }
    }

    fun skipBootSequence() {
        _osState.value = OsState.LOGIN
        addSystemLog("KERNEL", "Boot sequence skipped by user.", LogLevel.WARN)
    }

    fun registerUser(username: String, pass: String): Boolean {
        val cleanUser = username.lowercase().trim()
        val cleanPass = pass.trim()

        if (cleanUser.isEmpty() || cleanPass.isEmpty()) {
            showNotification(
                title = "REGISTRATION ERROR",
                message = "Username and password cannot be empty.",
                level = NotificationLevel.ERROR
            )
            return false
        }

        val newProfile = UserProfile(
            username = cleanUser,
            passwordHash = cleanPass,
            registeredAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )

        scope.launch {
            saveManager.saveUserProfile(newProfile)
            _userProfile.value = newProfile
            settingsRepository.updateUserHandle(cleanUser)
            setupUserVfsDirectory(cleanUser)

            addSystemLog("AUTH", "Registered new AbyssOS user: $cleanUser", LogLevel.INFO)
            showNotification(
                title = "PROFILE CREATED",
                message = "Welcome to AbyssOS, $cleanUser.",
                level = NotificationLevel.SUCCESS
            )

            _osState.value = OsState.DESKTOP
        }
        return true
    }

    fun loginUser(username: String, pass: String): Boolean {
        val cleanUser = username.lowercase().trim()
        val cleanPass = pass.trim()
        val profile = _userProfile.value

        if (profile == null || profile.username != cleanUser || profile.passwordHash != cleanPass) {
            addSystemLog("AUTH", "Authentication failed for user: $cleanUser", LogLevel.WARN)
            showNotification(
                title = "AUTHENTICATION FAILED",
                message = "Authentication failed.",
                level = NotificationLevel.ERROR
            )
            return false
        }

        val updatedProfile = profile.copy(lastLoginAt = System.currentTimeMillis())
        scope.launch {
            saveManager.saveUserProfile(updatedProfile)
            _userProfile.value = updatedProfile
            settingsRepository.updateUserHandle(cleanUser)
            setupUserVfsDirectory(cleanUser)

            addSystemLog("AUTH", "User $cleanUser authenticated successfully.", LogLevel.INFO)
            showNotification(
                title = "SESSION ESTABLISHED",
                message = "Welcome back, $cleanUser.",
                level = NotificationLevel.SUCCESS
            )

            _osState.value = OsState.DESKTOP
        }
        return true
    }

    fun logoutUser() {
        _osState.value = OsState.LOGIN
        closeActiveApp()
        addSystemLog("AUTH", "User session logged out.", LogLevel.INFO)
        showNotification(
            title = "LOGGED OUT",
            message = "AbyssOS session terminated.",
            level = NotificationLevel.INFO
        )
    }

    fun showNotification(title: String, message: String, level: NotificationLevel = NotificationLevel.INFO) {
        notificationJob?.cancel()
        _activeNotification.value = OsNotification(title = title, message = message, level = level)
        notificationJob = scope.launch {
            delay(3500L)
            _activeNotification.value = null
        }
    }

    fun setVirtualBrightnessAlpha(dimAlpha: Float) {
        _brightnessAlpha.value = dimAlpha.coerceIn(0f, 0.75f)
    }

    private fun setupUserVfsDirectory(username: String) {
        vfs.setupUserHome(username)
    }

    fun openApp(app: OsApp) {
        _activeApp.value = app
        addSystemLog("UI", "Opened application: ${app.appName}", LogLevel.INFO)
    }

    fun closeActiveApp() {
        val app = _activeApp.value
        _activeApp.value = null
        if (app != null) {
            addSystemLog("UI", "Closed application: ${app.appName}", LogLevel.INFO)
        }
    }

    fun getCommandContext(): CommandContext {
        val currentStatus = _systemStatus.value
        return CommandContext(
            vfs = vfs,
            systemStatus = currentStatus,
            commandRegistry = commandRegistry,
            onExitRequested = { closeActiveApp() },
            onOpenAppRequested = { appName ->
                val targetApp = OsApp.entries.find { it.appName.equals(appName, ignoreCase = true) }
                if (targetApp != null) {
                    openApp(targetApp)
                }
            }
        )
    }

    fun addSystemLog(tag: String, message: String, level: LogLevel = LogLevel.INFO) {
        val log = SystemLog(tag = tag, message = message, level = level)
        _systemLogs.value = (listOf(log) + _systemLogs.value).take(100)
    }

    private fun startStatusTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch(Dispatchers.Default) {
            var seconds = 3600L
            while (true) {
                delay(2000L)
                seconds += 2
                val cpu = Random.nextInt(8, 28)
                val ram = Random.nextInt(30, 42)
                _systemStatus.value = _systemStatus.value.copy(
                    cpuUsagePercent = cpu,
                    ramUsagePercent = ram,
                    uptimeSeconds = seconds,
                    userHandle = settingsRepository.settings.value.userHandle,
                    hostname = settingsRepository.settings.value.hostname
                )
            }
        }
    }

    fun getCurrentTimeString(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    fun getCurrentDateString(): String {
        return SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date())
    }
}

