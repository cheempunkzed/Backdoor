package com.example.backdoor.economy.models

import org.json.JSONObject
import java.util.UUID

enum class ObjectiveType {
    // NETWORK
    SCAN_TARGET,
    DISCOVER_SERVICE,

    // SECURITY
    ANALYZE_SECURITY,
    EXPLOIT_VULNERABILITY,

    // FILESYSTEM
    DOWNLOAD_FILE,
    DELETE_FILE,
    CREATE_REPORT,

    // TERMINAL
    EXECUTE_COMMAND,

    // DARKNET
    VISIT_HIDDEN_SERVICE,
    CREATE_FORUM_POST,
    CONTACT_NPC,

    // ECONOMY
    PAYMENT_RECEIVED,
    PAYMENT_SENT,
    PURCHASE_ITEM,
    SELL_ITEM
}

data class Objective(
    val objectiveId: String = UUID.randomUUID().toString(),
    val objectiveType: ObjectiveType,
    val description: String,
    val currentProgress: Int = 0,
    val requiredProgress: Int = 1,
    val targetParam: String? = null,
    val secondaryParam: String? = null,
    val isCompleted: Boolean = false
) {
    fun isDone(): Boolean = isCompleted || currentProgress >= requiredProgress

    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("objectiveId", objectiveId)
        obj.put("objectiveType", objectiveType.name)
        obj.put("description", description)
        obj.put("currentProgress", currentProgress)
        obj.put("requiredProgress", requiredProgress)
        obj.put("targetParam", targetParam ?: "")
        obj.put("secondaryParam", secondaryParam ?: "")
        obj.put("isCompleted", isDone())
        return obj
    }

    companion object {
        fun fromJson(json: JSONObject): Objective {
            val typeStr = json.optString("objectiveType", "EXECUTE_COMMAND")
            val type = runCatching { ObjectiveType.valueOf(typeStr) }.getOrDefault(ObjectiveType.EXECUTE_COMMAND)
            val curr = json.optInt("currentProgress", 0)
            val req = json.optInt("requiredProgress", 1)
            val comp = json.optBoolean("isCompleted", false)
            return Objective(
                objectiveId = json.optString("objectiveId", UUID.randomUUID().toString()),
                objectiveType = type,
                description = json.optString("description", ""),
                currentProgress = curr,
                requiredProgress = req,
                targetParam = json.optString("targetParam", "").ifEmpty { null },
                secondaryParam = json.optString("secondaryParam", "").ifEmpty { null },
                isCompleted = comp || curr >= req
            )
        }
    }
}
