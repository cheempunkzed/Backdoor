package com.example.backdoor.simulation.engine

import com.example.backdoor.core.GameClock
import com.example.backdoor.core.SystemEvent
import com.example.backdoor.core.SystemEventBus
import com.example.backdoor.corporate.CorporateGridRepository
import com.example.backdoor.darknet.engine.OnionNetworkEngine
import com.example.backdoor.economy.engine.ContractManager
import com.example.backdoor.economy.engine.ShadowEconomyEngine
import com.example.backdoor.economy.models.NewsArticle
import com.example.backdoor.economy.models.NewsCategory
import com.example.backdoor.web.engine.WebContentEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

class WorldSimulationEngine(
    private val scope: CoroutineScope,
    val eventBus: SystemEventBus,
    val gameClock: GameClock,
    val corporateManager: CorporateGridRepository,
    val economyEngine: ShadowEconomyEngine,
    val webContentEngine: WebContentEngine,
    val darknetEngine: OnionNetworkEngine,
    val contractManager: ContractManager
) {
    val livingWorldEngine = LivingWorldEngine(scope, eventBus, gameClock, corporateManager, economyEngine)
    val vulnerabilityEngine = VulnerabilityEngine(eventBus)
    val lawEnforcementEngine = LawEnforcementEngine(eventBus)
    val missionGenerator = ProceduralMissionGenerator(contractManager, eventBus)

    private val _isSimulationRunning = MutableStateFlow(false)
    val isSimulationRunning: StateFlow<Boolean> = _isSimulationRunning.asStateFlow()

    private val simulationTickDelayMs = 6000L

    init {
        initWorld()
        subscribeToSystemEvents()
    }

    fun initWorld() {
        livingWorldEngine.initWorld()
    }

    private fun subscribeToSystemEvents() {
        scope.launch {
            eventBus.events.collect { event ->
                handleSystemEvent(event)
            }
        }
    }

    private fun handleSystemEvent(event: SystemEvent) {
        when (event) {
            is SystemEvent.NetworkScanCompleted -> {
                lawEnforcementEngine.addHeat(2.5f, "Network Probing on ${event.target}")
                val org = corporateManager.getOrganizationByDomain(event.target)
                    ?: corporateManager.organizations.value.find { it.name.contains(event.target, ignoreCase = true) }
                org?.let {
                    livingWorldEngine.organizationsAI.value[it.id]?.registerProbingDetected(event.target)
                }
            }

            is SystemEvent.SecurityScanCompleted -> {
                lawEnforcementEngine.addHeat(4.0f, "Vulnerability Scan on ${event.target}")
            }

            is SystemEvent.VulnerabilityExploited -> {
                lawEnforcementEngine.addHeat(15.0f, "Exploit Injection on ${event.target}")
                val org = corporateManager.getOrganizationByDomain(event.target)
                org?.let {
                    val orgAi = livingWorldEngine.organizationsAI.value[it.id]
                    orgAi?.registerServerBreached(event.target)
                    missionGenerator.generateMissionForBreach(it.name, event.target)
                }
            }

            is SystemEvent.DataLeakDetected -> {
                lawEnforcementEngine.addHeat(10.0f, "Corporate Data Leak (${event.leakTitle})")
                val article = NewsArticle(
                    id = UUID.randomUUID().toString(),
                    title = "ALERT: ${event.leakTitle}",
                    content = "A critical breach and data leak has been detected. Confidential blueprints and credentials have been published to darknet forums.",
                    source = "Global Cyber News",
                    timestamp = System.currentTimeMillis(),
                    category = NewsCategory.SECURITY
                )
                economyEngine.newsService.publishArticle(article)
            }

            is SystemEvent.ServerCompromised -> {
                lawEnforcementEngine.addHeat(12.0f, "Server Intrusion on ${event.targetHost}")
            }

            is SystemEvent.EmployeeResigned -> {
                val article = NewsArticle(
                    id = UUID.randomUUID().toString(),
                    title = "Executive Resignation at ${event.companyId}",
                    content = "${event.employeeName} has left their position due to ${event.reason}. Market analysts evaluate potential operational impact.",
                    source = "Corporate Insider",
                    timestamp = System.currentTimeMillis(),
                    category = NewsCategory.CORPORATE
                )
                economyEngine.newsService.publishArticle(article)
            }

            is SystemEvent.ItemPurchased -> {
                lawEnforcementEngine.addHeat(3.0f, "Dark Market Purchase (${event.itemName})")
            }

            else -> {}
        }
    }

    fun startSimulation() {
        if (_isSimulationRunning.value) return
        _isSimulationRunning.value = true
        gameClock.start()
        livingWorldEngine.startSimulation()

        scope.launch {
            while (_isSimulationRunning.value) {
                delay(simulationTickDelayMs)
                tickWorldSimulation()
            }
        }
    }

    fun stopSimulation() {
        _isSimulationRunning.value = false
        gameClock.stop()
        livingWorldEngine.stopSimulation()
    }

    private fun tickWorldSimulation() {
        // 1. Vulnerability Lifecycle
        vulnerabilityEngine.tickVulnerabilityLifecycle()

        // 2. Organization AI ticks (HR, Security, Finance, Patching)
        livingWorldEngine.organizationsAI.value.values.forEach { orgAi ->
            orgAi.tick(vulnerabilityEngine, missionGenerator)
        }

        // 3. Law Enforcement Heat decay
        lawEnforcementEngine.decayHeat()

        // 4. Darknet Rumor Engine & Simulation tick
        darknetEngine.tickSimulation()
    }

    fun serializeToJson(): String {
        val root = JSONObject()
        root.put("livingWorld", livingWorldEngine.serializeToJson())
        root.put("vulnerabilities", vulnerabilityEngine.serializeToJson())
        root.put("lawEnforcement", lawEnforcementEngine.serializeToJson())
        return root.toString()
    }

    fun deserializeFromJson(json: String) {
        try {
            val root = JSONObject(json)
            if (root.has("livingWorld")) {
                livingWorldEngine.deserializeFromJson(root.getString("livingWorld"))
            }
            if (root.has("vulnerabilities")) {
                vulnerabilityEngine.deserializeFromJson(root.getString("vulnerabilities"))
            }
            if (root.has("lawEnforcement")) {
                lawEnforcementEngine.deserializeFromJson(root.getString("lawEnforcement"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            initWorld()
        }
    }
}
