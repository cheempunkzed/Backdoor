package com.example.backdoor.terminal

import kotlinx.coroutines.flow.StateFlow

interface JobProcess {
    val id: Int
    val pid: Int
    val name: String
    val isBackground: Boolean
    val status: String
    fun cancel()
}

interface JobManager {
    val runningJobs: StateFlow<List<JobProcess>>
    fun spawnJob(name: String, isBackground: Boolean, action: suspend () -> Unit): JobProcess
    fun killJob(pid: Int): Boolean
    fun bringToForeground(jobId: Int): Boolean
}

interface PackageManager {
    fun installPackage(packageName: String): Boolean
    fun removePackage(packageName: String): Boolean
    fun listInstalledPackages(): List<String>
}

interface NetworkService {
    suspend fun ping(host: String): String
    suspend fun sshConnect(host: String, user: String): Boolean
    suspend fun fetchUrl(url: String): String
}

interface ScriptInterpreter {
    suspend fun executeScript(scriptContent: String, context: CommandContext): CommandResult
}

interface CronScheduler {
    fun scheduleTask(cronExpr: String, command: String): String
    fun cancelTask(taskId: String): Boolean
    fun listScheduledTasks(): List<Pair<String, String>>
}
