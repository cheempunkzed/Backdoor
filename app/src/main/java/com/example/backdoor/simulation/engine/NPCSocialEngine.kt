package com.example.backdoor.simulation.engine

import com.example.backdoor.simulation.models.Employee
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

data class SocialRelation(
    val employeeId1: String,
    val employeeId2: String,
    var affinity: Int // -100 to 100
)

class NPCSocialEngine {
    private val _relations = MutableStateFlow<List<SocialRelation>>(emptyList())
    val relations: StateFlow<List<SocialRelation>> = _relations.asStateFlow()

    fun tick(allEmployees: List<Employee>) {
        val current = _relations.value.toMutableList()
        // Randomly adjust relations
        if (allEmployees.size > 1 && Random.nextFloat() < 0.1f) {
            val e1 = allEmployees.random()
            val e2 = allEmployees.random()
            if (e1.id != e2.id) {
                val existing = current.find { (it.employeeId1 == e1.id && it.employeeId2 == e2.id) || (it.employeeId1 == e2.id && it.employeeId2 == e1.id) }
                if (existing != null) {
                    existing.affinity += Random.nextInt(-5, 6)
                    existing.affinity = existing.affinity.coerceIn(-100, 100)
                } else {
                    current.add(SocialRelation(e1.id, e2.id, Random.nextInt(-20, 21)))
                }
            }
        }
        _relations.value = current
    }
}
