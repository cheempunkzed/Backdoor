package com.example.backdoor.simulation.engine

import com.example.backdoor.core.SystemEvent
import com.example.backdoor.core.SystemEventBus
import com.example.backdoor.economy.engine.ContractManager
import com.example.backdoor.economy.models.*
import com.example.backdoor.simulation.models.Vulnerability
import java.util.UUID

class ProceduralMissionGenerator(
    private val contractManager: ContractManager,
    private val eventBus: SystemEventBus
) {
    fun generateMissionForResignation(orgName: String, empName: String) {
        val contractId = "proc-resign-" + UUID.randomUUID().toString().take(8)
        val contract = Contract(
            id = contractId,
            title = "Forensic Audit: $empName ($orgName)",
            description = "Former employee $empName abruptly resigned from $orgName. The CISO suspects proprietary source code or access credentials were leaked prior to departure.",
            type = ContractType.PUBLIC,
            difficulty = (2..5).random(),
            rewardAmount = (1500..4500).random().toLong(),
            issuer = orgName,
            status = ContractStatus.AVAILABLE,
            category = "INVESTIGATION",
            objectives = listOf(
                Objective(
                    objectiveType = ObjectiveType.ANALYZE_SECURITY,
                    description = "Perform log analysis or network scan on $orgName primary gateway.",
                    targetParam = "$orgName-gateway"
                )
            )
        )
        contractManager.addContract(contract)
        eventBus.emit(SystemEvent.MissionCreated(contractId, contract.title, orgName))
    }

    fun generateMissionForBreach(orgName: String, serverName: String) {
        val contractId = "proc-breach-" + UUID.randomUUID().toString().take(8)
        val contract = Contract(
            id = contractId,
            title = "Emergency Hardening: $serverName",
            description = "Unidentified intrusion detected on $serverName ($orgName). Perform immediate incident response, patch exposed services, and restore integrity.",
            type = ContractType.PUBLIC,
            difficulty = (3..7).random(),
            rewardAmount = (2500..8000).random().toLong(),
            issuer = orgName,
            status = ContractStatus.AVAILABLE,
            category = "DEFENSIVE",
            objectives = listOf(
                Objective(
                    objectiveType = ObjectiveType.ANALYZE_SECURITY,
                    description = "Run security scan and verify non-vulnerable status.",
                    targetParam = serverName
                )
            )
        )
        contractManager.addContract(contract)
        eventBus.emit(SystemEvent.MissionCreated(contractId, contract.title, orgName))
    }

    fun generateMissionForZeroDay(vulnerability: Vulnerability, orgName: String) {
        val contractId = "proc-vuln-" + UUID.randomUUID().toString().take(8)
        val contract = Contract(
            id = contractId,
            title = "Zero-Day Research: ${vulnerability.cveId}",
            description = "Newly published vulnerability ${vulnerability.cveId} affects ${vulnerability.affectedSoftware}. $orgName requires an urgent proof-of-concept audit or patch validation.",
            type = ContractType.DARKNET,
            difficulty = (4..8).random(),
            rewardAmount = (3000..10000).random().toLong(),
            issuer = orgName,
            status = ContractStatus.AVAILABLE,
            category = "RESEARCH",
            objectives = listOf(
                Objective(
                    objectiveType = ObjectiveType.ANALYZE_SECURITY,
                    description = "Locate server running ${vulnerability.affectedSoftware} and execute scan.",
                    targetParam = vulnerability.affectedSoftware
                )
            )
        )
        contractManager.addContract(contract)
        eventBus.emit(SystemEvent.MissionCreated(contractId, contract.title, orgName))
    }

    fun generateMissionForPenTest(orgName: String) {
        val contractId = "proc-pentest-" + UUID.randomUUID().toString().take(8)
        val contract = Contract(
            id = contractId,
            title = "Red Team Penetration Test: $orgName",
            description = "With recent budget expansion, $orgName has commissioned a third-party red team assessment to test internal security responsiveness.",
            type = ContractType.CORPORATE,
            difficulty = (3..6).random(),
            rewardAmount = (4000..9000).random().toLong(),
            issuer = orgName,
            status = ContractStatus.AVAILABLE,
            category = "PENETRATION_TEST",
            objectives = listOf(
                Objective(
                    objectiveType = ObjectiveType.SCAN_TARGET,
                    description = "Discover active services and report security level.",
                    targetParam = orgName
                )
            )
        )
        contractManager.addContract(contract)
        eventBus.emit(SystemEvent.MissionCreated(contractId, contract.title, orgName))
    }
}
