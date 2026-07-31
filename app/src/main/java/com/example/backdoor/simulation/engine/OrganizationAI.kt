package com.example.backdoor.simulation.engine

import com.example.backdoor.corporate.Organization
import com.example.backdoor.simulation.models.Employee
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class OrganizationAI(val organization: Organization) {
    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    fun setEmployees(list: List<Employee>) {
        _employees.value = list
    }

    fun hireEmployee(employee: Employee) {
        val current = _employees.value.toMutableList()
        current.add(employee)
        _employees.value = current
    }

    fun fireEmployee(employeeId: String) {
        val current = _employees.value.toMutableList()
        current.removeAll { it.id == employeeId }
        _employees.value = current
    }

    fun getEmployeeById(id: String): Employee? {
        return _employees.value.find { it.id == id }
    }
}
