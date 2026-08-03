package com.example.backdoor.economy.engine

import com.example.backdoor.core.SystemEvent
import com.example.backdoor.core.SystemEventBus
import com.example.backdoor.economy.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ContractManager {
    private val _contracts = MutableStateFlow<List<Contract>>(emptyList())
    val contracts: StateFlow<List<Contract>> = _contracts.asStateFlow()

    private var eventBus: SystemEventBus? = null

    fun setEventBus(bus: SystemEventBus, scope: CoroutineScope? = null) {
        this.eventBus = bus
        scope?.launch {
            bus.events.collect { event ->
                onSystemEvent(event)
            }
        }
    }

    fun addContract(contract: Contract) {
        _contracts.update { current ->
            if (current.none { it.id == contract.id }) {
                current + contract
            } else current
        }
    }

    fun acceptContract(contractId: String): Boolean {
        var accepted = false
        _contracts.update { currentList ->
            currentList.map { contract ->
                if (contract.id == contractId && contract.status == ContractStatus.AVAILABLE) {
                    accepted = true
                    contract.copy(
                        status = ContractStatus.ACCEPTED,
                        acceptedAtTimestamp = System.currentTimeMillis()
                    )
                } else contract
            }
        }
        return accepted
    }

    fun completeContract(contractId: String): Boolean {
        val targetContract = _contracts.value.find { it.id == contractId } ?: return false

        // Completion validation!
        val validation = CompletionValidator.validate(targetContract)
        if (validation !is ValidationResult.Success) {
            return false
        }

        var completed = false
        _contracts.update { currentList ->
            currentList.map { contract ->
                if (contract.id == contractId && contract.status == ContractStatus.ACCEPTED) {
                    completed = true
                    contract.copy(status = ContractStatus.COMPLETED)
                } else contract
            }
        }
        return completed
    }

    fun failContract(contractId: String) {
        _contracts.update { currentList ->
            currentList.map { contract ->
                if (contract.id == contractId && contract.status == ContractStatus.ACCEPTED) {
                    contract.copy(status = ContractStatus.FAILED)
                } else contract
            }
        }
    }

    fun removeContract(contractId: String) {
        _contracts.update { current -> current.filterNot { c -> c.id == contractId } }
    }

    fun onSystemEvent(event: SystemEvent) {
        _contracts.update { currentList ->
            currentList.map { contract ->
                if (contract.status == ContractStatus.ACCEPTED && contract.objectives.isNotEmpty()) {
                    var modified = false
                    val updatedObjectives = contract.objectives.map { obj ->
                        if (!obj.isDone() && matchesEvent(obj, event)) {
                            modified = true
                            val nextProg = (obj.currentProgress + 1).coerceAtMost(obj.requiredProgress)
                            obj.copy(
                                currentProgress = nextProg,
                                isCompleted = nextProg >= obj.requiredProgress
                            )
                        } else {
                            obj
                        }
                    }
                    if (modified) {
                        val updatedContract = contract.copy(objectives = updatedObjectives)
                        if (updatedContract.isCompleted()) {
                            eventBus?.emit(
                                SystemEvent.NotificationTriggered(
                                    title = "CONTRACT OBJECTIVE COMPLETE",
                                    message = "All objectives fulfilled for: ${contract.title}",
                                    level = com.example.backdoor.core.NotificationLevel.SUCCESS
                                )
                            )
                        }
                        updatedContract
                    } else contract
                } else contract
            }
        }
    }

    private fun matchesEvent(objective: Objective, event: SystemEvent): Boolean {
        val target = objective.targetParam?.lowercase()
        val secondary = objective.secondaryParam?.lowercase()

        return when (objective.objectiveType) {
            ObjectiveType.SCAN_TARGET -> {
                when (event) {
                    is SystemEvent.NetworkScanCompleted -> target.isNullOrEmpty() || event.target.lowercase().contains(target)
                    is SystemEvent.CommandExecuted -> (event.commandName.equals("scan", ignoreCase = true) || event.commandName.equals("nmap", ignoreCase = true)) &&
                            (target.isNullOrEmpty() || event.commandLine.lowercase().contains(target))
                    else -> false
                }
            }

            ObjectiveType.DISCOVER_SERVICE -> {
                when (event) {
                    is SystemEvent.ServiceDiscovered -> {
                        val matchesTarget = target.isNullOrEmpty() || event.target.lowercase().contains(target)
                        val matchesService = secondary.isNullOrEmpty() || event.serviceName.lowercase().contains(secondary)
                        matchesTarget && matchesService
                    }
                    else -> false
                }
            }

            ObjectiveType.ANALYZE_SECURITY -> {
                when (event) {
                    is SystemEvent.SecurityScanCompleted -> target.isNullOrEmpty() || event.target.lowercase().contains(target)
                    is SystemEvent.CommandExecuted -> event.commandName.equals("security", ignoreCase = true) &&
                            (target.isNullOrEmpty() || event.commandLine.lowercase().contains(target))
                    else -> false
                }
            }

            ObjectiveType.EXPLOIT_VULNERABILITY -> {
                when (event) {
                    is SystemEvent.VulnerabilityExploited -> target.isNullOrEmpty() || event.target.lowercase().contains(target)
                    else -> false
                }
            }

            ObjectiveType.DOWNLOAD_FILE -> {
                when (event) {
                    is SystemEvent.FileDownloaded -> target.isNullOrEmpty() || event.filePath.lowercase().contains(target)
                    else -> false
                }
            }

            ObjectiveType.DELETE_FILE -> {
                when (event) {
                    is SystemEvent.FileDeleted -> target.isNullOrEmpty() || event.filePath.lowercase().contains(target)
                    else -> false
                }
            }

            ObjectiveType.CREATE_REPORT -> {
                when (event) {
                    is SystemEvent.SecurityReportCreated -> target.isNullOrEmpty() || event.reportPath.lowercase().contains(target)
                    else -> false
                }
            }

            ObjectiveType.EXECUTE_COMMAND -> {
                when (event) {
                    is SystemEvent.CommandExecuted -> {
                        target.isNullOrEmpty() || event.commandName.lowercase() == target || event.commandLine.lowercase().contains(target)
                    }
                    else -> false
                }
            }

            ObjectiveType.VISIT_HIDDEN_SERVICE -> {
                when (event) {
                    is SystemEvent.OnionServiceConnected -> target.isNullOrEmpty() || event.onionUrl.lowercase().contains(target)
                    is SystemEvent.OnionRouteEstablished -> target.isNullOrEmpty() || event.targetOnion.lowercase().contains(target)
                    else -> false
                }
            }

            ObjectiveType.CREATE_FORUM_POST -> {
                when (event) {
                    is SystemEvent.ForumPostCreated -> target.isNullOrEmpty() || event.title.lowercase().contains(target) || event.forumId.lowercase().contains(target)
                    else -> false
                }
            }

            ObjectiveType.CONTACT_NPC -> {
                when (event) {
                    is SystemEvent.NPCInteractionCompleted -> target.isNullOrEmpty() || event.npcId.lowercase().contains(target)
                    else -> false
                }
            }

            ObjectiveType.PAYMENT_RECEIVED -> {
                when (event) {
                    is SystemEvent.TransactionCompleted -> event.isIncoming
                    else -> false
                }
            }

            ObjectiveType.PAYMENT_SENT -> {
                when (event) {
                    is SystemEvent.TransactionCompleted -> !event.isIncoming
                    else -> false
                }
            }

            ObjectiveType.PURCHASE_ITEM -> {
                when (event) {
                    is SystemEvent.ItemPurchased -> target.isNullOrEmpty() || event.itemId.lowercase().contains(target) || event.itemName.lowercase().contains(target)
                    else -> false
                }
            }

            ObjectiveType.SELL_ITEM -> {
                when (event) {
                    is SystemEvent.ItemSold -> target.isNullOrEmpty() || event.itemId.lowercase().contains(target) || event.itemName.lowercase().contains(target)
                    else -> false
                }
            }
        }
    }

    fun restore(savedContracts: List<Contract>) {
        _contracts.value = savedContracts
    }

    fun serializeToJsonArray(): JSONArray {
        val arr = JSONArray()
        _contracts.value.forEach { c ->
            arr.put(c.toJson())
        }
        return arr
    }

    fun deserializeFromJsonArray(arr: JSONArray) {
        val list = mutableListOf<Contract>()
        for (i in 0 until arr.length()) {
            list.add(Contract.fromJson(arr.getJSONObject(i)))
        }
        _contracts.value = list
    }
}
