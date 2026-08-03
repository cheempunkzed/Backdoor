package com.example.backdoor.simulation.engine

import com.example.backdoor.core.SystemEvent
import com.example.backdoor.core.SystemEventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

enum class HeatLevel(val title: String, val threshold: Float) {
    NORMAL("Clear / Normal Activity", 0.0f),
    SUSPICION("Elevated Suspicion", 25.0f),
    ACTIVE_INVESTIGATION("Active Law Enforcement Investigation", 50.0f),
    CRITICAL_RAID_RISK("HIGH ALERT - Grid Raid Risk", 75.0f)
}

data class LawEnforcementState(
    val globalHeat: Float = 0.0f, // 0.0 to 100.0
    val heatLevel: HeatLevel = HeatLevel.NORMAL,
    val activeAgency: String = "Global Cyber Command",
    val investigationSeverity: Int = 1,
    val monitoredSubnets: List<String> = emptyList(),
    val flaggedIdentities: List<String> = emptyList(),
    val isProxyRouteBlocked: Boolean = false
)

class LawEnforcementEngine(
    private val eventBus: SystemEventBus? = null
) {
    private val _state = MutableStateFlow(LawEnforcementState())
    val state: StateFlow<LawEnforcementState> = _state.asStateFlow()

    fun addHeat(amount: Float, reason: String) {
        val current = _state.value
        val newHeat = (current.globalHeat + amount).coerceIn(0.0f, 100.0f)
        val newLevel = calculateHeatLevel(newHeat)

        var newAgency = current.activeAgency
        var newSeverity = current.investigationSeverity
        var newProxyBlocked = current.isProxyRouteBlocked

        if (newHeat >= 75.0f) {
            newAgency = "INTERPOL Cyber Taskforce"
            newSeverity = 4
            newProxyBlocked = true
        } else if (newHeat >= 50.0f) {
            newAgency = "Global Cyber Command"
            newSeverity = 3
        } else if (newHeat >= 25.0f) {
            newAgency = "Aegis Counter-Intel"
            newSeverity = 2
        } else {
            newAgency = "Grid Security"
            newSeverity = 1
            newProxyBlocked = false
        }

        val updated = current.copy(
            globalHeat = newHeat,
            heatLevel = newLevel,
            activeAgency = newAgency,
            investigationSeverity = newSeverity,
            isProxyRouteBlocked = newProxyBlocked
        )
        _state.value = updated

        eventBus?.emit(SystemEvent.HeatChanged(newHeat.toInt(), newLevel.title))

        if (newLevel != current.heatLevel && newHeat >= 50.0f) {
            eventBus?.emit(
                SystemEvent.InvestigationStarted(
                    targetIdentity = "operator",
                    agency = newAgency,
                    severity = newSeverity
                )
            )
            eventBus?.emit(
                SystemEvent.NotificationTriggered(
                    title = "LAW ENFORCEMENT ALERT",
                    message = "${newLevel.title} ($reason) - Investigating Agency: $newAgency",
                    level = com.example.backdoor.core.NotificationLevel.WARNING
                )
            )
        }
    }

    fun decayHeat() {
        val current = _state.value
        if (current.globalHeat > 0.0f) {
            val newHeat = (current.globalHeat - 1.5f).coerceAtLeast(0.0f)
            val newLevel = calculateHeatLevel(newHeat)
            _state.value = current.copy(
                globalHeat = newHeat,
                heatLevel = newLevel,
                isProxyRouteBlocked = if (newHeat < 70.0f) false else current.isProxyRouteBlocked
            )
            eventBus?.emit(SystemEvent.HeatChanged(newHeat.toInt(), newLevel.title))
        }
    }

    private fun calculateHeatLevel(heat: Float): HeatLevel {
        return when {
            heat >= 75.0f -> HeatLevel.CRITICAL_RAID_RISK
            heat >= 50.0f -> HeatLevel.ACTIVE_INVESTIGATION
            heat >= 25.0f -> HeatLevel.SUSPICION
            else -> HeatLevel.NORMAL
        }
    }

    fun serializeToJson(): String {
        val current = _state.value
        val obj = JSONObject()
        obj.put("globalHeat", current.globalHeat.toDouble())
        obj.put("heatLevel", current.heatLevel.name)
        obj.put("activeAgency", current.activeAgency)
        obj.put("investigationSeverity", current.investigationSeverity)
        obj.put("isProxyRouteBlocked", current.isProxyRouteBlocked)
        return obj.toString()
    }

    fun deserializeFromJson(json: String) {
        try {
            val obj = JSONObject(json)
            val heat = obj.getDouble("globalHeat").toFloat()
            val level = HeatLevel.valueOf(obj.getString("heatLevel"))
            val agency = obj.getString("activeAgency")
            val severity = obj.getInt("investigationSeverity")
            val blocked = if (obj.has("isProxyRouteBlocked")) obj.getBoolean("isProxyRouteBlocked") else false

            _state.value = LawEnforcementState(
                globalHeat = heat,
                heatLevel = level,
                activeAgency = agency,
                investigationSeverity = severity,
                isProxyRouteBlocked = blocked
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
