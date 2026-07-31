package com.example.backdoor.security.modules

import com.example.backdoor.game.AbyssOSManager
import com.example.backdoor.security.framework.SecurityModule
import com.example.backdoor.security.framework.SecurityModuleCategory
import com.example.backdoor.security.framework.SecurityTask
import com.example.backdoor.security.framework.SecurityTaskResult
import com.example.backdoor.security.scanner.PortScannerEngine
import kotlinx.coroutines.delay

/**
 * Module for performing port scanning and service banner diagnostics on target servers.
 */
class ServiceDiscoveryModule(
    private val portScanner: PortScannerEngine = PortScannerEngine()
) : SecurityModule {
    override val id: String = "mod-service-discovery"
    override val name: String = "Port & Service Diagnostics"
    override val category: SecurityModuleCategory = SecurityModuleCategory.PORT_ANALYSIS
    override val description: String = "Scans TCP/UDP ports, detects running daemon services, retrieves banners, and inspects protocol versions."

    override suspend fun executeTask(
        task: SecurityTask,
        osManager: Any,
        onProgressUpdate: (progress: Float, logLine: String) -> Unit
    ): SecurityTaskResult {
        val manager = osManager as AbyssOSManager
        val target = task.target
        val logs = mutableListOf<String>()

        onProgressUpdate(0.1f, "[PORT SCAN] Launching non-destructive TCP SYN port probe against $target...")
        delay(300)

        val org = manager.corporateRepository.getOrganizationByDomain(target)
            ?: manager.corporateRepository.getOrganizationById(target)

        val server = org?.servers?.firstOrNull() ?: manager.corporateRepository.organizations.value.firstOrNull()?.servers?.firstOrNull()

        if (server != null) {
            onProgressUpdate(0.4f, "[PORT SCAN] Probing common service ports (21..27017) on ${server.name} (${server.ip})...")
            delay(500)

            val scanSummary = portScanner.scanCorporateServer(server)

            onProgressUpdate(0.8f, "[PORT SCAN] Detected ${scanSummary.openPortsCount} open service ports with active banners.")
            delay(400)

            scanSummary.portResults.filter { it.state == com.example.backdoor.security.scanner.PortState.OPEN }.forEach { p ->
                logs.add("Port ${p.port}/TCP: OPEN [${p.serviceName}] Banner: \"${p.banner}\" (${p.responseTimeMs}ms)")
            }

            onProgressUpdate(1.0f, "[PORT SCAN] Service diagnostics completed for ${server.ip}.")

            return SecurityTaskResult(
                taskId = task.id,
                success = true,
                summary = "Scanned ${scanSummary.totalScannedPorts} ports on ${server.ip}. Found ${scanSummary.openPortsCount} OPEN services.",
                details = mapOf(
                    "targetIp" to server.ip,
                    "openPortsCount" to scanSummary.openPortsCount,
                    "totalScannedPorts" to scanSummary.totalScannedPorts,
                    "serverClass" to server.type.displayName
                ),
                logs = logs
            )
        } else {
            onProgressUpdate(1.0f, "[PORT SCAN] Default router 192.168.1.1 scanned. Open Ports: 22 (SSH), 80 (HTTP), 53 (DNS).")
            return SecurityTaskResult(
                taskId = task.id,
                success = true,
                summary = "Scanned default gateway router 192.168.1.1. Found 3 OPEN services.",
                details = mapOf("targetIp" to "192.168.1.1", "openPortsCount" to 3),
                logs = listOf("Port 22/TCP OPEN [SSH]", "Port 53/TCP OPEN [DNS]", "Port 80/TCP OPEN [HTTP]")
            )
        }
    }
}
