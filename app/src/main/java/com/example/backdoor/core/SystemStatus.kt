package com.example.backdoor.core

data class SystemStatus(
    val batteryPercent: Int = 88,
    val isCharging: Boolean = false,
    val signalBars: Int = 4,
    val isWifiConnected: Boolean = true,
    val networkStatus: String = "127.0.0.1 [ENCRYPTED]",
    val ipAddress: String = "192.168.1.104",
    val cpuUsagePercent: Int = 12,
    val ramUsagePercent: Int = 34,
    val totalRamMb: Int = 8192,
    val usedRamMb: Int = 2785,
    val activeProcessCount: Int = 14,
    val uptimeSeconds: Long = 3600L,
    val hostname: String = "abyss-node-01",
    val userHandle: String = "operator"
)

enum class LogLevel {
    INFO, DEBUG, WARN, ERROR, CRITICAL
}

data class SystemLog(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel = LogLevel.INFO,
    val tag: String = "KERNEL",
    val message: String
)
