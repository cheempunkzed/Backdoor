package com.example.backdoor.simulation.models

import java.util.UUID

enum class IncidentType {
    POWER_FAILURE,
    STORAGE_FAILURE,
    DATABASE_CORRUPTION,
    FIREWALL_FAILURE,
    VPN_OUTAGE,
    CREDENTIAL_LEAK,
    EXPIRED_CERTIFICATE,
    DDOS,
    MISCONFIGURATION,
    BACKUP_FAILURE,
    SERVICE_CRASH,
    NETWORK_PARTITION
}

enum class IncidentSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class Incident(
    val id: String = UUID.randomUUID().toString(),
    val type: IncidentType,
    val severity: IncidentSeverity,
    val organizationId: String,
    val targetServerId: String?,
    val timestamp: String,
    var resolved: Boolean = false
)
