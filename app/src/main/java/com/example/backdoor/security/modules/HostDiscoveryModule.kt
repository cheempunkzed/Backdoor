package com.example.backdoor.security.modules

import com.example.backdoor.game.AbyssOSManager
import com.example.backdoor.security.framework.SecurityModule
import com.example.backdoor.security.framework.SecurityModuleCategory
import com.example.backdoor.security.framework.SecurityTask
import com.example.backdoor.security.framework.SecurityTaskResult
import kotlinx.coroutines.delay

/**
 * Module for discovering active IP hosts, server nodes, and data center routers in simulated subnets.
 */
class HostDiscoveryModule : SecurityModule {
    override val id: String = "mod-host-discovery"
    override val name: String = "Host & Network Discovery"
    override val category: SecurityModuleCategory = SecurityModuleCategory.RECONNAISSANCE
    override val description: String = "Probes subnets and organizations to identify live IP nodes, MAC addresses, and hostnames."

    override suspend fun executeTask(
        task: SecurityTask,
        osManager: Any,
        onProgressUpdate: (progress: Float, logLine: String) -> Unit
    ): SecurityTaskResult {
        val manager = osManager as AbyssOSManager
        val target = task.target
        val logs = mutableListOf<String>()

        onProgressUpdate(0.1f, "[DISCOVERY] Initializing ARP & ICMP probe sweep against $target...")
        delay(300)

        // Find organization or search nodes in corporate repository
        val org = manager.corporateRepository.getOrganizationByDomain(target)
            ?: manager.corporateRepository.getOrganizationById(target)

        val foundHosts = if (org != null) {
            onProgressUpdate(0.4f, "[DISCOVERY] Target identified as Corporate Grid Entity: ${org.name} (${org.subnet})")
            delay(400)
            org.servers
        } else {
            onProgressUpdate(0.4f, "[DISCOVERY] Scanning generic network subnet for active IP addresses...")
            delay(400)
            emptyList()
        }

        val nodeCount = if (foundHosts.isNotEmpty()) foundHosts.size else manager.networkEngine.nodes.value.size
        onProgressUpdate(0.7f, "[DISCOVERY] Discovered $nodeCount active IP response signals on subnet.")
        delay(400)

        logs.add("Target: $target")
        logs.add("Active Hosts Found: $nodeCount")
        if (org != null) {
            logs.add("Organization: ${org.name} [${org.code}]")
            logs.add("Data Centers Monitored: ${org.dataCenters.size}")
        }

        onProgressUpdate(1.0f, "[DISCOVERY] Host sweep complete. $nodeCount responsive nodes indexed.")

        val details = mutableMapOf<String, Any>(
            "target" to target,
            "hostsCount" to nodeCount,
            "organization" to (org?.name ?: "Local Subnet"),
            "subnet" to (org?.subnet ?: "192.168.1.0/24")
        )

        return SecurityTaskResult(
            taskId = task.id,
            success = true,
            summary = "Discovered $nodeCount active hosts on target $target.",
            details = details,
            logs = logs
        )
    }
}
