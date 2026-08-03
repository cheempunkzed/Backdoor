package com.example.backdoor.darknet.engine

import com.example.backdoor.darknet.model.Rumor
import com.example.backdoor.darknet.model.UndergroundEvent
import com.example.backdoor.darknet.model.UndergroundEventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class RumorEngine {

    private val _rumors = MutableStateFlow<List<Rumor>>(emptyList())
    val rumors: StateFlow<List<Rumor>> = _rumors.asStateFlow()

    private val _undergroundEvents = MutableStateFlow<List<UndergroundEvent>>(emptyList())
    val undergroundEvents: StateFlow<List<UndergroundEvent>> = _undergroundEvents.asStateFlow()

    init {
        initializeInitialRumors()
        initializeInitialEvents()
    }

    private fun initializeInitialRumors() {
        _rumors.value = listOf(
            Rumor(
                title = "Aegis Perimeter Router Vulnerability",
                description = "Rumor circulating on CipherRoom: Aegis Financial left port 27017 exposed on core gateway 192.168.1.10.",
                sourceHandle = "v0id_walker",
                credibility = 0.95f,
                targetHostOrCorp = "Aegis Financial",
                isTrue = true
            ),
            Rumor(
                title = "Apex Crypto Cold Wallet Breach",
                description = "Unverified report claiming Apex Corp internal ledger was copied during a midnight maintenance window.",
                sourceHandle = "cypher_ghost",
                credibility = 0.60f,
                targetHostOrCorp = "Apex Financial",
                isTrue = true
            ),
            Rumor(
                title = "Federal Honeypot Exit Node",
                description = "Exit node 'ExitNode-Echo' in France is allegedly logging unencrypted TCP streams.",
                sourceHandle = "shadow_weaver",
                credibility = 0.40f,
                targetHostOrCorp = "Onion Relay Network",
                isTrue = false
            ),
            Rumor(
                title = "Zero-Day Hardware Backdoor in Router Firmware",
                description = "BlackVault leak claims FPGA router chipsets contain hardcoded master credentials 'root:syndicate2026'.",
                sourceHandle = "k3rnel_panic",
                credibility = 0.88f,
                targetHostOrCorp = "Hardware Syndicate",
                isTrue = true
            )
        )
    }

    private fun initializeInitialEvents() {
        _undergroundEvents.value = listOf(
            UndergroundEvent(
                type = UndergroundEventType.MASSIVE_DATA_LEAK,
                headline = "BlackVault Discloses Aegis Financial Credential Dump",
                description = "Over 15,000 corporate employee hashes and gateway addresses leaked to blackvault.onion.",
                affectedAddress = "blackvault.onion",
                heatImpact = 15
            ),
            UndergroundEvent(
                type = UndergroundEventType.EXIT_NODE_COMPROMISED,
                headline = "Onion Relay Node Bravo Isolated Following Anomalous Traffic",
                description = "Relay operator cycled keys after detecting unauthorized trace attempts.",
                affectedAddress = "199.249.230.8",
                heatImpact = 5
            )
        )
    }

    fun generateRandomRumor(): Rumor {
        val targets = listOf("Aegis Financial", "Apex Corp", "Nexus Systems", "Global Cyber Grid", "Shadow Exchange")
        val subjects = listOf(
            "unpatched zero-day vulnerability in SSH service",
            "unencrypted database backup left on public storage bucket",
            "insider trading scheme orchestrated by high-level executives",
            "covert federal surveillance tap on exit node",
            "experimental Quantum Encryption key leak"
        )
        val target = targets.random()
        val subject = subjects.random()
        val handles = listOf("v0id_walker", "cypher_ghost", "k3rnel_panic", "shadow_weaver", "root_daemon", "hex_ghost")
        val isTrue = (0..100).random() > 30
        val cred = if (isTrue) (0.7f + (0..25).random() / 100f) else (0.1f + (0..40).random() / 100f)

        val rumor = Rumor(
            title = "$target $subject",
            description = "Informant @${handles.random()} posted evidence suggesting $target has $subject.",
            sourceHandle = handles.random(),
            credibility = cred,
            targetHostOrCorp = target,
            isTrue = isTrue
        )
        _rumors.value = listOf(rumor) + _rumors.value.take(19)
        return rumor
    }

    fun triggerUndergroundEvent(type: UndergroundEventType, headline: String, desc: String, address: String? = null): UndergroundEvent {
        val event = UndergroundEvent(
            type = type,
            headline = headline,
            description = desc,
            affectedAddress = address,
            heatImpact = (10..30).random()
        )
        _undergroundEvents.value = listOf(event) + _undergroundEvents.value.take(14)
        return event
    }

    fun toJson(): String {
        val root = JSONObject()

        val rArray = JSONArray()
        _rumors.value.forEach { r ->
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("title", r.title)
            obj.put("description", r.description)
            obj.put("sourceHandle", r.sourceHandle)
            obj.put("credibility", r.credibility)
            obj.put("timestamp", r.timestamp)
            obj.put("propagatedCount", r.propagatedCount)
            obj.put("targetHostOrCorp", r.targetHostOrCorp)
            obj.put("isTrue", r.isTrue)
            rArray.put(obj)
        }
        root.put("rumors", rArray)

        val eArray = JSONArray()
        _undergroundEvents.value.forEach { e ->
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("type", e.type.name)
            obj.put("headline", e.headline)
            obj.put("description", e.description)
            obj.put("affectedAddress", e.affectedAddress)
            obj.put("timestamp", e.timestamp)
            obj.put("heatImpact", e.heatImpact)
            eArray.put(obj)
        }
        root.put("events", eArray)

        return root.toString()
    }

    fun loadFromJson(json: String) {
        try {
            val root = JSONObject(json)
            if (root.has("rumors")) {
                val arr = root.getJSONArray("rumors")
                val list = mutableListOf<Rumor>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        Rumor(
                            id = obj.optString("id"),
                            title = obj.optString("title"),
                            description = obj.optString("description"),
                            sourceHandle = obj.optString("sourceHandle"),
                            credibility = obj.optDouble("credibility", 0.8).toFloat(),
                            timestamp = obj.optLong("timestamp"),
                            propagatedCount = obj.optInt("propagatedCount"),
                            targetHostOrCorp = obj.optString("targetHostOrCorp"),
                            isTrue = obj.optBoolean("isTrue", true)
                        )
                    )
                }
                if (list.isNotEmpty()) _rumors.value = list
            }

            if (root.has("events")) {
                val arr = root.getJSONArray("events")
                val list = mutableListOf<UndergroundEvent>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val type = try { UndergroundEventType.valueOf(obj.getString("type")) } catch (_: Exception) { UndergroundEventType.MASSIVE_DATA_LEAK }
                    list.add(
                        UndergroundEvent(
                            id = obj.optString("id"),
                            type = type,
                            headline = obj.optString("headline"),
                            description = obj.optString("description"),
                            affectedAddress = obj.optString("affectedAddress"),
                            timestamp = obj.optLong("timestamp"),
                            heatImpact = obj.optInt("heatImpact", 10)
                        )
                    )
                }
                if (list.isNotEmpty()) _undergroundEvents.value = list
            }
        } catch (_: Exception) {}
    }
}
