package com.example.backdoor.economy.models

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Failed(val reason: String) : ValidationResult()
}

object CompletionValidator {
    fun validate(contract: Contract): ValidationResult {
        if (contract.status != ContractStatus.ACCEPTED) {
            return ValidationResult.Failed("Contract is not in ACCEPTED state.")
        }
        if (contract.expiresAtTimestamp != null && System.currentTimeMillis() > contract.expiresAtTimestamp) {
            return ValidationResult.Failed("Contract has expired.")
        }
        if (!contract.isCompleted()) {
            val incompleteCount = contract.objectives.count { !it.isDone() }
            return ValidationResult.Failed("Incomplete objectives ($incompleteCount remaining).")
        }
        return ValidationResult.Success
    }
}
