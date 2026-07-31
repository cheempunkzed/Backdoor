package com.example.backdoor.security.modules

import com.example.backdoor.game.AbyssOSManager
import com.example.backdoor.security.framework.SecurityModule
import com.example.backdoor.security.framework.SecurityModuleCategory
import com.example.backdoor.security.framework.SecurityTask
import com.example.backdoor.security.framework.SecurityTaskResult
import com.example.backdoor.security.scanner.ServiceScanner
import kotlinx.coroutines.delay

/**
 * Module for evaluating node or organization Security Score (0-100), Patch Level, Configuration Rating, and Firewall Rating.
 */
class SecurityAssessmentModule(
    private val serviceScanner: ServiceScanner = ServiceScanner()
) : SecurityModule {
    override val id: String = "mod-security-assessment"
    override val name: String = "Vulnerability & Clearance Rating"
    override val category: SecurityModuleCategory = SecurityModuleCategory.SECURITY_ASSESSMENT
    override val description: String = "Evaluates overall security clearance score, patch compliance level, configuration rating, and firewall grade."

    override suspend fun executeTask(
        task: SecurityTask,
        osManager: Any,
        onProgressUpdate: (progress: Float, logLine: String) -> Unit
    ): SecurityTaskResult {
        val manager = osManager as AbyssOSManager
        val target = task.target
        val logs = mutableListOf<String>()

        onProgressUpdate(0.1f, "[ASSESSMENT] Evaluating security posture metrics for $target...")
        delay(350)

        val org = manager.corporateRepository.getOrganizationByDomain(target)
            ?: manager.corporateRepository.getOrganizationById(target)

        val server = org?.servers?.firstOrNull() ?: manager.corporateRepository.organizations.value.firstOrNull()?.servers?.firstOrNull()

        if (server != null) {
            onProgressUpdate(0.5f, "[ASSESSMENT] Auditing server configuration and patch compliance on ${server.name}...")
            delay(400)

            val audit = serviceScanner.auditServer(server)

            onProgressUpdate(0.8f, "[ASSESSMENT] Security Score: ${audit.overallScore}/100 | Grade: ${audit.configRating}")
            delay(300)

            logs.add("Target Server: ${server.name} (${server.ip})")
            logs.add("Security Clearance Tier: ${server.securityLevel}")
            logs.add("Overall Security Score: ${audit.overallScore} / 100")
            logs.add("Patch Compliance: ${audit.patchLevel}")
            logs.add("Configuration Rating: ${audit.configRating}")
            logs.add("Firewall Defense Grade: ${audit.firewallRating}")

            onProgressUpdate(1.0f, "[ASSESSMENT] Security assessment completed.")

            return SecurityTaskResult(
                taskId = task.id,
                success = true,
                summary = "Security Assessment for ${server.ip}: Score ${audit.overallScore}/100 [${audit.configRating}].",
                details = mapOf(
                    "target" to target,
                    "securityScore" to audit.overallScore,
                    "patchLevel" to audit.patchLevel,
                    "configRating" to audit.configRating,
                    "firewallRating" to audit.firewallRating,
                    "hostsFound" to (org?.servers?.size ?: 1),
                    "openPortsFound" to server.services.size
                ),
                logs = logs
            )
        } else {
            onProgressUpdate(1.0f, "[ASSESSMENT] Local Gateway Assessment Complete. Score: 85/100 (Hardened Router).")
            return SecurityTaskResult(
                taskId = task.id,
                success = true,
                summary = "Local Gateway Assessment Complete. Score: 85/100.",
                details = mapOf(
                    "target" to "192.168.1.1",
                    "securityScore" to 85,
                    "patchLevel" to "95% Compliant",
                    "configRating" to "Grade A",
                    "firewallRating" to "HARDENED",
                    "hostsFound" to 1,
                    "openPortsFound" to 3
                ),
                logs = listOf("Score: 85/100", "Patch Level: 95% Compliant", "Firewall: HARDENED")
            )
        }
    }
}
