package com.example.backdoor.economy.engine

import com.example.backdoor.economy.models.Contract
import com.example.backdoor.economy.models.ContractStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ContractManager {
    private val _contracts = MutableStateFlow<List<Contract>>(emptyList())
    val contracts: StateFlow<List<Contract>> = _contracts.asStateFlow()

    fun addContract(contract: Contract) {
        _contracts.update { it + contract }
    }

    fun acceptContract(contractId: String): Boolean {
        var accepted = false
        _contracts.update { currentList ->
            currentList.map { 
                if (it.id == contractId && it.status == ContractStatus.AVAILABLE) {
                    accepted = true
                    it.copy(status = ContractStatus.ACCEPTED)
                } else it
            }
        }
        return accepted
    }

    fun completeContract(contractId: String): Boolean {
        var completed = false
        _contracts.update { currentList ->
            currentList.map {
                if (it.id == contractId && it.status == ContractStatus.ACCEPTED) {
                    completed = true
                    it.copy(status = ContractStatus.COMPLETED)
                } else it
            }
        }
        return completed
    }

    fun failContract(contractId: String) {
        _contracts.update { currentList ->
            currentList.map {
                if (it.id == contractId && it.status == ContractStatus.ACCEPTED) {
                    it.copy(status = ContractStatus.FAILED)
                } else it
            }
        }
    }
    
    fun removeContract(contractId: String) {
        _contracts.update { it.filterNot { c -> c.id == contractId } }
    }

    fun restore(savedContracts: List<Contract>) {
        _contracts.value = savedContracts
    }
}
