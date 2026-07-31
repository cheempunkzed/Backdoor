package com.example.backdoor.security.core

import com.example.backdoor.filesystem.VirtualFileSystem
import com.example.backdoor.security.database.KnowledgeDatabase
import com.example.backdoor.security.framework.SecurityModule
import com.example.backdoor.security.framework.SecurityTask
import com.example.backdoor.security.framework.SecurityTaskResult
import com.example.backdoor.security.framework.TaskStatus
import com.example.backdoor.security.modules.HostDiscoveryModule
import com.example.backdoor.security.modules.SecurityAssessmentModule
import com.example.backdoor.security.modules.ServiceDiscoveryModule
import com.example.backdoor.security.modules.TopologyAnalysisModule
import com.example.backdoor.security.reports.SecurityReport
import com.example.backdoor.security.reports.SecurityReportGenerator
import com.example.backdoor.security.sessions.ResearchSession
import com.example.backdoor.security.sessions.SessionHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Interface hook prepared for Milestone 8 (DarkNet / Onion Routing Integration).
 */
interface DarkNetRoutingHook {
    fun isEncryptedOnionRouteActive(): Boolean = false
    fun getHiddenServiceDescriptors(): List<String> = emptyList()
}

/**
 * Central Offensive Security Engine in AbyssOS 0.7.0.
 * Coordinates module registration, task scheduling, research session logging,
 * report generation, and knowledge base integration.
 */
class OffensiveSecurityFramework(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    vfs: VirtualFileSystem? = null
) {
    val knowledgeDatabase = KnowledgeDatabase()
    val sessionHistory = SessionHistory()
    private var reportGenerator: SecurityReportGenerator? = vfs?.let { SecurityReportGenerator(it) }

    private val registeredModules = mutableMapOf<String, SecurityModule>()

    private val _activeTasks = MutableStateFlow<List<SecurityTask>>(emptyList())
    val activeTasks: StateFlow<List<SecurityTask>> = _activeTasks.asStateFlow()

    private val _completedReports = MutableStateFlow<List<SecurityReport>>(emptyList())
    val completedReports: StateFlow<List<SecurityReport>> = _completedReports.asStateFlow()

    private val _sessions = MutableStateFlow<List<ResearchSession>>(emptyList())
    val sessions: StateFlow<List<ResearchSession>> = _sessions.asStateFlow()

    var darkNetHook: DarkNetRoutingHook? = null

    init {
        registerDefaultModules()
    }

    fun updateVfs(vfs: VirtualFileSystem) {
        reportGenerator = SecurityReportGenerator(vfs)
    }

    private fun registerDefaultModules() {
        registerModule(HostDiscoveryModule())
        registerModule(ServiceDiscoveryModule())
        registerModule(TopologyAnalysisModule())
        registerModule(SecurityAssessmentModule())
    }

    fun registerModule(module: SecurityModule) {
        registeredModules[module.id] = module
    }

    fun getRegisteredModules(): List<SecurityModule> = registeredModules.values.toList()

    fun getModuleById(id: String): SecurityModule? = registeredModules[id]

    /**
     * Executes a security module task asynchronously.
     */
    fun runTask(
        moduleId: String,
        target: String,
        osManager: Any,
        onComplete: ((SecurityTaskResult) -> Unit)? = null
    ): SecurityTask? {
        val module = registeredModules[moduleId] ?: return null
        val taskId = "task-${System.currentTimeMillis() % 1000000}"

        val initialTask = SecurityTask(
            id = taskId,
            moduleId = module.id,
            moduleName = module.name,
            target = target,
            status = TaskStatus.RUNNING,
            progress = 0.05f,
            logOutput = listOf("[INIT] Starting task ${module.name} against $target...")
        )

        _activeTasks.value = listOf(initialTask) + _activeTasks.value

        // Ensure active research session
        val session = sessionHistory.startNewSession(target)
        _sessions.value = sessionHistory.getAllSessions()

        scope.launch {
            val taskResult = module.executeTask(
                task = initialTask,
                osManager = osManager,
                onProgressUpdate = { prog, line ->
                    _activeTasks.value = _activeTasks.value.map { t ->
                        if (t.id == taskId) {
                            t.copy(
                                progress = prog,
                                logOutput = t.logOutput + line
                            )
                        } else t
                    }
                }
            )

            val updatedTask = initialTask.copy(
                status = if (taskResult.success) TaskStatus.COMPLETED else TaskStatus.FAILED,
                progress = 1.0f,
                endTime = System.currentTimeMillis(),
                logOutput = initialTask.logOutput + taskResult.logs,
                resultMap = taskResult.details
            )

            _activeTasks.value = _activeTasks.value.map { t ->
                if (t.id == taskId) updatedTask else t
            }

            session.tasksExecuted.add(updatedTask)

            // Auto generate report if it's security assessment or host discovery
            generateReportForTask(target, taskResult)

            onComplete?.invoke(taskResult)
        }

        return initialTask
    }

    fun generateReportForTask(targetName: String, taskResult: SecurityTaskResult): SecurityReport? {
        val gen = reportGenerator ?: return null
        val details = taskResult.details

        val score = (details["securityScore"] as? Int) ?: 80
        val hosts = (details["hostsCount"] as? Int) ?: (details["hostsFound"] as? Int) ?: 1
        val ports = (details["openPortsCount"] as? Int) ?: (details["openPortsFound"] as? Int) ?: 2
        val patch = (details["patchLevel"] as? String) ?: "92.0% Compliant"
        val firewall = (details["firewallRating"] as? String) ?: "STANDARD"

        val report = gen.generateAndSaveReport(
            targetName = targetName,
            targetIpOrSubnet = targetName,
            securityScore = score,
            hostsFound = hosts,
            openPortsFound = ports,
            patchLevel = patch,
            firewallRating = firewall,
            detailsMap = details
        )

        _completedReports.value = listOf(report) + _completedReports.value
        return report
    }

    /**
     * Serializes framework history and reports into a JSON string for save system.
     */
    fun toJson(): String {
        val root = JSONObject()

        val reportsArray = JSONArray()
        _completedReports.value.forEach { r ->
            val reportObj = JSONObject()
            reportObj.put("id", r.id)
            reportObj.put("title", r.title)
            reportObj.put("target", r.target)
            reportObj.put("timestamp", r.timestamp)
            reportObj.put("securityScore", r.securityScore)
            reportObj.put("filePath", r.filePath)
            reportsArray.put(reportObj)
        }
        root.put("reports", reportsArray)

        val tasksArray = JSONArray()
        _activeTasks.value.take(20).forEach { t ->
            val taskObj = JSONObject()
            taskObj.put("id", t.id)
            taskObj.put("moduleId", t.moduleId)
            taskObj.put("target", t.target)
            taskObj.put("status", t.status.name)
            tasksArray.put(taskObj)
        }
        root.put("recentTasks", tasksArray)

        return root.toString()
    }

    /**
     * Deserializes framework saved state.
     */
    fun loadFromJson(json: String) {
        try {
            val root = JSONObject(json)
            // Restore basic metadata if available
        } catch (_: Exception) {}
    }
}
