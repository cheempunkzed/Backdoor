package com.example.backdoor.game

import com.example.backdoor.core.LogLevel
import com.example.backdoor.core.NotificationLevel
import com.example.backdoor.core.OsNotification
import com.example.backdoor.core.ProcessManager
import com.example.backdoor.core.SystemLog
import com.example.backdoor.core.SystemStatus
import com.example.backdoor.core.WindowManager
import com.example.backdoor.filesystem.InMemoryVirtualFileSystem
import com.example.backdoor.filesystem.VirtualFileSystem
import com.example.backdoor.save.MemorySaveManager
import com.example.backdoor.save.SaveManager
import com.example.backdoor.save.UserProfile
import com.example.backdoor.settings.SettingsRepository
import com.example.backdoor.terminal.CommandContext
import com.example.backdoor.terminal.CommandExecutor
import com.example.backdoor.terminal.CommandRegistry
import com.example.backdoor.terminal.TerminalSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
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
    SYSTEM_MONITOR("System Monitor", "ic_monitor"),
    WALLET("Wallet", "ic_wallet"),
    CONTRACTS("Contracts", "ic_work"),
    MARKETPLACE("Marketplace", "ic_shopping_cart"),
    MAIL("Mail", "ic_mail"),
    NEWS("News", "ic_news"),
    INVENTORY("Inventory", "ic_inventory")
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
    val processManager = ProcessManager(scope)
    val windowManager = WindowManager(processManager)

    val commandRegistry = CommandRegistry()
    val commandExecutor = CommandExecutor(commandRegistry)
    val networkEngine = com.example.backdoor.network.engine.AbyssNetworkEngine()
    val corporateRepository = com.example.backdoor.corporate.CorporateGridRepository()
    val securityFramework = com.example.backdoor.security.core.OffensiveSecurityFramework(scope, vfs)
    val darknetEngine = com.example.backdoor.darknet.engine.OnionNetworkEngine()
    val eventBus = com.example.backdoor.core.SystemEventBus()
    val economyEngine = com.example.backdoor.economy.engine.ShadowEconomyEngine(scope, eventBus)
    val gameClock = com.example.backdoor.core.GameClock(scope)
    val livingWorldEngine = com.example.backdoor.simulation.engine.LivingWorldEngine(
        scope = scope,
        eventBus = eventBus,
        gameClock = gameClock,
        corporateManager = corporateRepository,
        economyEngine = economyEngine
    )

    private val _osState = MutableStateFlow(OsState.BOOTING)
    val osState: StateFlow<OsState> = _osState.asStateFlow()

    private val _bootLogs = MutableStateFlow<List<BootLogEntry>>(emptyList())
    val bootLogs: StateFlow<List<BootLogEntry>> = _bootLogs.asStateFlow()

    private val _systemStatus = MutableStateFlow(SystemStatus())
    val systemStatus: StateFlow<SystemStatus> = _systemStatus.asStateFlow()

    private val _activeApp = MutableStateFlow<OsApp?>(null)
    val activeApp: StateFlow<OsApp?> = _activeApp.asStateFlow()

    private val _pinnedDockApps = MutableStateFlow<List<OsApp>>(emptyList())
    val pinnedDockApps: StateFlow<List<OsApp>> = _pinnedDockApps.asStateFlow()

    private val _desktopPositions = MutableStateFlow<Map<OsApp, Pair<Int, Int>>>(emptyMap())
    val desktopPositions: StateFlow<Map<OsApp, Pair<Int, Int>>> = _desktopPositions.asStateFlow()

    private val _systemLogs = MutableStateFlow<List<SystemLog>>(emptyList())
    val systemLogs: StateFlow<List<SystemLog>> = _systemLogs.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _activeNotification = MutableStateFlow<OsNotification?>(null)
    val activeNotification: StateFlow<OsNotification?> = _activeNotification.asStateFlow()

    private val _brightnessAlpha = MutableStateFlow(0f)
    val brightnessAlpha: StateFlow<Float> = _brightnessAlpha.asStateFlow()

    private var notificationJob: Job? = null
    private var tickerJob: Job? = null

    val terminalSession: TerminalSession by lazy {
        TerminalSession(
            initialUser = settingsRepository.settings.value.userHandle,
            initialHostname = settingsRepository.settings.value.hostname,
            vfs = vfs
        )
    }

    init {
        corporateRepository.registerWithNetworkEngine(networkEngine)
        securityFramework.updateVfs(vfs)
        securityFramework.darkNetHook = darknetEngine
        addSystemLog("KERNEL", "AbyssOS 1.1.0 Hybrid System initialized.", LogLevel.INFO)
        startStatusTicker()

        scope.launch {
            windowManager.windows.collect { wins ->
                val focused = wins.find { it.isFocused && !it.isMinimized }?.app
                _activeApp.value = focused
            }
        }

        scope.launch {
            val profile = saveManager.getUserProfile()
            _userProfile.value = profile

            val vfsJson = saveManager.getVfsDataJson()
            if (!vfsJson.isNullOrEmpty()) {
                vfs.deserializeFromJson(vfsJson, profile?.username ?: "operator")
            }

            val networkJson = saveManager.getNetworkTopologyJson()
            if (!networkJson.isNullOrEmpty()) {
                networkEngine.repository.deserializeFromJson(networkJson)
            }
            
            val livingWorldJson = saveManager.getLivingWorldJson()
            if (!livingWorldJson.isNullOrEmpty()) {
                livingWorldEngine.deserializeFromJson(livingWorldJson)
            } else {
                livingWorldEngine.initWorld()
            }
            livingWorldEngine.startSimulation()

            val economyJson = saveManager.getEconomyJson()
            if (!economyJson.isNullOrEmpty()) {
                economyEngine.deserializeFromJson(economyJson)
            }

            // Restore Dock Pinned Apps
            val dockJson = saveManager.getDockPinnedAppsJson()
            if (!dockJson.isNullOrEmpty()) {
                try {
                    val jsonArr = JSONArray(dockJson)
                    val restoredList = mutableListOf<OsApp>()
                    for (i in 0 until jsonArr.length()) {
                        val name = jsonArr.getString(i)
                        OsApp.entries.find { it.name == name }?.let { restoredList.add(it) }
                    }
                    _pinnedDockApps.value = restoredList
                } catch (e: Exception) {
                    _pinnedDockApps.value = emptyList()
                }
            } else {
                _pinnedDockApps.value = emptyList() // Dock empty by default as requested
            }

            // Restore Desktop Positions
            val desktopJson = saveManager.getDesktopPositionsJson()
            if (!desktopJson.isNullOrEmpty()) {
                try {
                    val jsonObj = JSONObject(desktopJson)
                    val restoredMap = mutableMapOf<OsApp, Pair<Int, Int>>()
                    jsonObj.keys().forEach { key ->
                        val app = OsApp.entries.find { it.name == key }
                        if (app != null) {
                            val posObj = jsonObj.getJSONObject(key)
                            restoredMap[app] = Pair(posObj.getInt("row"), posObj.getInt("col"))
                        }
                    }
                    _desktopPositions.value = restoredMap
                } catch (e: Exception) {
                    _desktopPositions.value = defaultDesktopPositions()
                }
            } else {
                _desktopPositions.value = defaultDesktopPositions()
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
        
        // Background auto-save for engine states
        scope.launch {
            while(true) {
                delay(10000)
                saveManager.saveLivingWorldJson(livingWorldEngine.serializeToJson())
                saveManager.saveEconomyJson(economyEngine.serializeToJson())
            }
        }
        
        // Listen to SystemEventBus
        scope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is com.example.backdoor.core.SystemEvent.AppRequested -> {
                        openApp(event.targetApp)
                        // payload would go to the App Instance if it supported it.
                    }
                    is com.example.backdoor.core.SystemEvent.NotificationTriggered -> {
                        val logLevel = when (event.level) {
                            com.example.backdoor.core.NotificationLevel.INFO -> com.example.backdoor.core.LogLevel.INFO
                            com.example.backdoor.core.NotificationLevel.SUCCESS -> com.example.backdoor.core.LogLevel.INFO
                            com.example.backdoor.core.NotificationLevel.WARNING -> com.example.backdoor.core.LogLevel.WARN
                            com.example.backdoor.core.NotificationLevel.ERROR -> com.example.backdoor.core.LogLevel.ERROR
                        }
                        addSystemLog(event.title, event.message, logLevel)
                        
                        if (event.priority != com.example.backdoor.core.NotificationPriority.BACKGROUND) {
                            showNotification(event.title, event.message, event.level)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun defaultDesktopPositions(): Map<OsApp, Pair<Int, Int>> {
        val map = mutableMapOf<OsApp, Pair<Int, Int>>()
        OsApp.entries.forEachIndexed { index, app ->
            val row = index / 4
            val col = index % 4
            map[app] = Pair(row, col)
        }
        return map
    }

    fun pinAppToDock(app: OsApp) {
        if (!_pinnedDockApps.value.contains(app)) {
            val newList = _pinnedDockApps.value + app
            _pinnedDockApps.value = newList
            scope.launch {
                val jsonArr = JSONArray()
                newList.forEach { jsonArr.put(it.name) }
                saveManager.saveDockPinnedAppsJson(jsonArr.toString())
            }
            showNotification("DOCK", "${app.appName} pinned to dock.", NotificationLevel.INFO)
        }
    }

    fun unpinAppFromDock(app: OsApp) {
        val newList = _pinnedDockApps.value.filterNot { it == app }
        _pinnedDockApps.value = newList
        scope.launch {
            val jsonArr = JSONArray()
            newList.forEach { jsonArr.put(it.name) }
            saveManager.saveDockPinnedAppsJson(jsonArr.toString())
        }
        showNotification("DOCK", "${app.appName} unpinned from dock.", NotificationLevel.INFO)
    }

    fun updateDesktopPosition(app: OsApp, row: Int, col: Int) {
        val newMap = _desktopPositions.value.toMutableMap()
        newMap[app] = Pair(row, col)
        _desktopPositions.value = newMap
        scope.launch {
            val jsonObj = JSONObject()
            newMap.forEach { (a, pos) ->
                val pObj = JSONObject()
                pObj.put("row", pos.first)
                pObj.put("col", pos.second)
                jsonObj.put(a.name, pObj)
            }
            saveManager.saveDesktopPositionsJson(jsonObj.toString())
        }
    }

    fun openApp(
        app: OsApp,
        mode: com.example.backdoor.core.ApplicationDisplayMode = com.example.backdoor.core.ApplicationDisplayMode.FULLSCREEN
    ) {
        windowManager.openOrFocusApp(app, mode)
        _activeApp.value = app
        addSystemLog("UI", "Opened application: ${app.appName} [${mode.name}]", LogLevel.INFO)
    }

    fun closeActiveApp() {
        val app = _activeApp.value
        if (app != null) {
            windowManager.closeWindow(app)
            _activeApp.value = windowManager.getFocusedApp()
            addSystemLog("UI", "Closed application: ${app.appName}", LogLevel.INFO)
        }
    }

    fun closeApp(app: OsApp) {
        windowManager.closeWindow(app)
        _activeApp.value = windowManager.getFocusedApp()
        addSystemLog("UI", "Closed application: ${app.appName}", LogLevel.INFO)
    }

    fun startBootSequence() {
        _osState.value = OsState.BOOTING
        _bootLogs.value = emptyList()

        scope.launch {
            val lines = listOf(
                "AbyssOS Boot Sequence",
                "Version 1.1.0 (Codename: ABYSSOS HYBRID INTERFACE)",
                "",
                "Initializing AbyssNet Hybrid Subsystem Kernel...",
                "Mounting Virtual File System (AbyssFS)...",
                "Populating Corporate Organizations & Server Matrix...",
                "Configuring Domain Name Resolver (DNS)...",
                "Starting Virtual Routing Engine & Latency Simulator...",
                "Probing Corporate Data Centers & Subnet Racks...",
                "Loading Command Registry & Security Tools...",
                "Initializing Shell Engine...",
                "Verifying Corporate Infrastructure Integrity...",
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

    fun getCommandContext(): CommandContext {
        val currentStatus = _systemStatus.value
        return CommandContext(
            vfs = vfs,
            systemStatus = currentStatus,
            commandRegistry = commandRegistry,
            session = terminalSession,
            networkEngine = networkEngine,
            eventBus = eventBus,
            onExitRequested = { closeActiveApp() },
            onOpenAppRequested = { appName ->
                val targetApp = OsApp.entries.find { 
                    it.appName.equals(appName, ignoreCase = true) || it.name.equals(appName, ignoreCase = true)
                }
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

