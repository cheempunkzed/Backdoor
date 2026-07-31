package com.example.backdoor.core

import com.example.backdoor.game.OsApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class ProcessStatus {
    RUNNING,
    SLEEPING,
    STOPPED,
    ZOMBIE
}

data class OSProcess(
    val pid: Int,
    val name: String,
    val app: OsApp? = null,
    val status: ProcessStatus = ProcessStatus.RUNNING,
    val cpuUsagePercent: Float = 0.5f,
    val ramUsageMb: Float = 14.5f,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val priority: Int = 20, // Nice value 0-39
    val isDaemon: Boolean = false,
    val appState: Any? = null
) {
    val uptimeSeconds: Long
        get() = (System.currentTimeMillis() - startTimeMillis) / 1000L
}

class ProcessManager(
    private val scope: CoroutineScope
) {
    private val _processes = MutableStateFlow<List<OSProcess>>(emptyList())
    val processes: StateFlow<List<OSProcess>> = _processes.asStateFlow()

    private var nextPid = 101
    private var tickerJob: Job? = null

    init {
        // Initialize Core Virtual System Daemons
        val systemDaemons = listOf(
            OSProcess(pid = 1, name = "systemd (init)", status = ProcessStatus.RUNNING, cpuUsagePercent = 0.2f, ramUsageMb = 8.5f, priority = 10, isDaemon = true),
            OSProcess(pid = 2, name = "abyss-vfsd", status = ProcessStatus.RUNNING, cpuUsagePercent = 0.4f, ramUsageMb = 12.0f, priority = 15, isDaemon = true),
            OSProcess(pid = 3, name = "win-manager", status = ProcessStatus.RUNNING, cpuUsagePercent = 1.1f, ramUsageMb = 18.2f, priority = 12, isDaemon = true),
            OSProcess(pid = 4, name = "net-daemon", status = ProcessStatus.RUNNING, cpuUsagePercent = 0.3f, ramUsageMb = 9.8f, priority = 18, isDaemon = true),
            OSProcess(pid = 5, name = "desktop-shell", status = ProcessStatus.RUNNING, cpuUsagePercent = 2.4f, ramUsageMb = 28.0f, priority = 15, isDaemon = true)
        )
        _processes.value = systemDaemons

        startProcessTicker()
    }

    fun spawnProcessForApp(app: OsApp): OSProcess {
        val existing = _processes.value.find { it.app == app && it.status != ProcessStatus.STOPPED }
        if (existing != null) {
            // Re-activate if sleeping or stopped
            val updated = existing.copy(status = ProcessStatus.RUNNING)
            _processes.value = _processes.value.map { if (it.pid == existing.pid) updated else it }
            return updated
        }

        val pid = nextPid++
        val (initialCpu, initialRam) = when (app) {
            OsApp.TERMINAL -> 1.5f to 16.0f
            OsApp.FILES -> 2.1f to 24.5f
            OsApp.BROWSER -> 4.8f to 45.0f
            OsApp.NETWORK -> 1.2f to 15.0f
            OsApp.DARKNET -> 3.5f to 32.0f
            OsApp.SETTINGS -> 0.8f to 12.0f
            OsApp.LOGS -> 0.6f to 10.0f
            OsApp.SYSTEM_MONITOR -> 1.8f to 18.0f
            OsApp.WALLET -> 0.5f to 15.0f
            OsApp.CONTRACTS -> 0.7f to 18.0f
            OsApp.MARKETPLACE -> 1.5f to 28.0f
            OsApp.MAIL -> 0.6f to 12.0f
            OsApp.NEWS -> 0.4f to 14.0f
            OsApp.INVENTORY -> 0.6f to 16.0f
        }

        val appState: AppState = when (app) {
            OsApp.TERMINAL -> TerminalAppState()
            OsApp.BROWSER -> BrowserAppState()
            OsApp.FILES -> FilesAppState()
            OsApp.SETTINGS -> SettingsAppState()
            OsApp.DARKNET -> DarkNetAppState()
            OsApp.NETWORK -> NetworkAppState()
            OsApp.LOGS -> LogsAppState()
            OsApp.SYSTEM_MONITOR -> SystemMonitorAppState()
            OsApp.WALLET -> WalletAppState()
            OsApp.CONTRACTS -> ContractsAppState()
            OsApp.MARKETPLACE -> MarketplaceAppState()
            OsApp.MAIL -> MailAppState()
            OsApp.NEWS -> NewsAppState()
            OsApp.INVENTORY -> InventoryAppState()
        }

        val newProc = OSProcess(
            pid = pid,
            name = app.appName,
            app = app,
            status = ProcessStatus.RUNNING,
            cpuUsagePercent = initialCpu,
            ramUsageMb = initialRam,
            priority = 20,
            isDaemon = false,
            appState = appState
        )

        _processes.value = _processes.value + newProc
        return newProc
    }

    fun terminateProcessByApp(app: OsApp) {
        val target = _processes.value.find { it.app == app }
        if (target != null && !target.isDaemon) {
            killProcess(target.pid)
        }
    }

    fun killProcess(pid: Int): Boolean {
        val proc = _processes.value.find { it.pid == pid } ?: return false
        if (proc.isDaemon) {
            // Protected daemon process
            return false
        }
        _processes.value = _processes.value.filterNot { it.pid == pid }
        return true
    }

    fun updatePriority(pid: Int, newPriority: Int) {
        val clampedPriority = newPriority.coerceIn(1, 39)
        _processes.value = _processes.value.map {
            if (it.pid == pid) it.copy(priority = clampedPriority) else it
        }
    }

    fun getProcessForApp(app: OsApp): OSProcess? {
        return _processes.value.find { it.app == app && it.status == ProcessStatus.RUNNING }
    }

    private fun startProcessTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch(Dispatchers.Default) {
            while (true) {
                delay(1800L)
                _processes.value = _processes.value.map { proc ->
                    if (proc.status == ProcessStatus.RUNNING) {
                        val cpuDelta = (Random.nextFloat() * 1.2f) - 0.6f
                        val ramDelta = (Random.nextFloat() * 0.8f) - 0.4f
                        val newCpu = (proc.cpuUsagePercent + cpuDelta).coerceIn(0.1f, 25.0f)
                        val newRam = (proc.ramUsageMb + ramDelta).coerceIn(5.0f, 150.0f)
                        proc.copy(
                            cpuUsagePercent = (newCpu * 10).toInt() / 10f,
                            ramUsageMb = (newRam * 10).toInt() / 10f
                        )
                    } else proc
                }
            }
        }
    }
}
