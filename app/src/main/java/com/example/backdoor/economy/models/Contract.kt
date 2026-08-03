package com.example.backdoor.economy.models

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Contract(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val type: ContractType = ContractType.PUBLIC,
    val difficulty: Int = 1, // 1 to 10
    val requiredReputation: Int = 0,
    val rewardAmount: Long = 500,
    val rewardCurrency: CurrencyType = CurrencyType.CREDITS,
    val timeLimitSeconds: Long? = null,
    val issuer: String = "Unknown Client", // NPC or Corp Name
    val status: ContractStatus = ContractStatus.AVAILABLE,
    val risks: List<String> = emptyList(),
    val requiredHardware: List<String> = emptyList(),
    val completionCriteria: String = "",
    val objectives: List<Objective> = emptyList(),
    val category: String = "GENERAL",
    val acceptedAtTimestamp: Long? = null,
    val expiresAtTimestamp: Long? = null
) {
    fun isCompleted(): Boolean {
        if (objectives.isEmpty()) {
            return status == ContractStatus.COMPLETED
        }
        return objectives.all { it.isDone() }
    }

    val completedObjectivesCount: Int
        get() = objectives.count { it.isDone() }

    val totalObjectivesCount: Int
        get() = objectives.size

    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("title", title)
        obj.put("description", description)
        obj.put("type", type.name)
        obj.put("difficulty", difficulty)
        obj.put("requiredReputation", requiredReputation)
        obj.put("rewardAmount", rewardAmount)
        obj.put("rewardCurrency", rewardCurrency.name)
        if (timeLimitSeconds != null) obj.put("timeLimitSeconds", timeLimitSeconds)
        obj.put("issuer", issuer)
        obj.put("status", status.name)
        
        val risksArr = JSONArray()
        risks.forEach { risksArr.put(it) }
        obj.put("risks", risksArr)

        val hwArr = JSONArray()
        requiredHardware.forEach { hwArr.put(it) }
        obj.put("requiredHardware", hwArr)

        obj.put("completionCriteria", completionCriteria)
        obj.put("category", category)
        if (acceptedAtTimestamp != null) obj.put("acceptedAtTimestamp", acceptedAtTimestamp)
        if (expiresAtTimestamp != null) obj.put("expiresAtTimestamp", expiresAtTimestamp)

        val objsArr = JSONArray()
        objectives.forEach { objsArr.put(it.toJson()) }
        obj.put("objectives", objsArr)

        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): Contract {
            val typeStr = obj.optString("type", "PUBLIC")
            val cType = runCatching { ContractType.valueOf(typeStr) }.getOrDefault(ContractType.PUBLIC)

            val currStr = obj.optString("rewardCurrency", "CREDITS")
            val cCurr = runCatching { CurrencyType.valueOf(currStr) }.getOrDefault(CurrencyType.CREDITS)

            val statusStr = obj.optString("status", "AVAILABLE")
            val cStatus = runCatching { ContractStatus.valueOf(statusStr) }.getOrDefault(ContractStatus.AVAILABLE)

            val risksList = mutableListOf<String>()
            if (obj.has("risks")) {
                val rArr = obj.getJSONArray("risks")
                for (i in 0 until rArr.length()) risksList.add(rArr.getString(i))
            }

            val hwList = mutableListOf<String>()
            if (obj.has("requiredHardware")) {
                val hArr = obj.getJSONArray("requiredHardware")
                for (i in 0 until hArr.length()) hwList.add(hArr.getString(i))
            }

            val objsList = mutableListOf<Objective>()
            if (obj.has("objectives")) {
                val oArr = obj.getJSONArray("objectives")
                for (i in 0 until oArr.length()) {
                    objsList.add(Objective.fromJson(oArr.getJSONObject(i)))
                }
            }

            return Contract(
                id = obj.optString("id", UUID.randomUUID().toString()),
                title = obj.optString("title", "Unknown Mission"),
                description = obj.optString("description", ""),
                type = cType,
                difficulty = obj.optInt("difficulty", 1),
                requiredReputation = obj.optInt("requiredReputation", 0),
                rewardAmount = obj.optLong("rewardAmount", 500L),
                rewardCurrency = cCurr,
                timeLimitSeconds = if (obj.has("timeLimitSeconds")) obj.getLong("timeLimitSeconds") else null,
                issuer = obj.optString("issuer", "Unknown Client"),
                status = cStatus,
                risks = risksList,
                requiredHardware = hwList,
                completionCriteria = obj.optString("completionCriteria", ""),
                objectives = objsList,
                category = obj.optString("category", "GENERAL"),
                acceptedAtTimestamp = if (obj.has("acceptedAtTimestamp")) obj.getLong("acceptedAtTimestamp") else null,
                expiresAtTimestamp = if (obj.has("expiresAtTimestamp")) obj.getLong("expiresAtTimestamp") else null
            )
        }
    }
}

enum class ContractType {
    PUBLIC, CORPORATE, GOVERNMENT, RESEARCH, DARKNET, FREELANCE, NPC
}

enum class ContractStatus {
    AVAILABLE,
    ACCEPTED,
    COMPLETED,
    FAILED,
    EXPIRED
}
