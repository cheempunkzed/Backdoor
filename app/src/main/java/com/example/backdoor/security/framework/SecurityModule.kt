package com.example.backdoor.security.framework

/**
 * Execution status for an Offensive Security Task.
 */
enum class TaskStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Categories of security analysis modules in Backdoor.
 */
enum class SecurityModuleCategory(val displayName: String) {
    RECONNAISSANCE("Reconnaissance & Asset Discovery"),
    PORT_ANALYSIS("Port & Service Diagnostics"),
    SECURITY_ASSESSMENT("Vulnerability & Clearance Rating"),
    TOPOLOGY_ANALYSIS("Network Topology Mapping")
}

/**
 * Data model representing a scheduled or running security task.
 */
data class SecurityTask(
    val id: String,
    val moduleId: String,
    val moduleName: String,
    val target: String,
    val status: TaskStatus = TaskStatus.PENDING,
    val progress: Float = 0f,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val logOutput: List<String> = emptyList(),
    val resultMap: Map<String, Any> = emptyMap()
)

/**
 * Result object returned upon completion of a security module task.
 */
data class SecurityTaskResult(
    val taskId: String,
    val success: Boolean,
    val summary: String,
    val details: Map<String, Any> = emptyMap(),
    val logs: List<String> = emptyList()
)

/**
 * Extensible interface for all security analysis modules in AbyssOS.
 */
interface SecurityModule {
    val id: String
    val name: String
    val category: SecurityModuleCategory
    val description: String

    /**
     * Executes the security task asynchronously against the simulated virtual network.
     */
    suspend fun executeTask(
        task: SecurityTask,
        osManager: Any, // Type cast to AbyssOSManager inside implementation
        onProgressUpdate: (progress: Float, logLine: String) -> Unit
    ): SecurityTaskResult
}
