package com.example.backdoor.simulation.engine

import com.example.backdoor.core.SystemEvent
import com.example.backdoor.core.SystemEventBus
import com.example.backdoor.corporate.Organization
import com.example.backdoor.simulation.models.Department
import com.example.backdoor.simulation.models.Employee
import com.example.backdoor.simulation.models.JobPosition
import com.example.backdoor.simulation.models.PatchStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class SecurityDepartmentAI(
    var firewallSensitivity: Int = 3, // 1 to 10
    var idsEnabled: Boolean = false,
    var probingCount: Int = 0,
    val pendingPatches: MutableList<String> = mutableListOf() // List of CVE IDs
)

class FinanceDepartmentAI(
    var currentBudget: Double = 1_000_000.0,
    var securityAllocation: Double = 150_000.0
)

class ITDepartmentAI(
    var activeServerReboots: MutableList<String> = mutableListOf(),
    var maintenanceScheduled: Boolean = false
)

class OrganizationAI(
    val organization: Organization,
    private val eventBus: SystemEventBus? = null
) {
    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    val securityDept = SecurityDepartmentAI(firewallSensitivity = organization.securityLevel.coerceIn(1, 10))
    val financeDept = FinanceDepartmentAI(currentBudget = organization.budget.toDouble())
    val itDept = ITDepartmentAI()

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

    /**
     * Responds to network scans / probing on company domain or subnet.
     */
    fun registerProbingDetected(targetHost: String) {
        securityDept.probingCount++
        if (securityDept.probingCount >= 2 && !securityDept.idsEnabled) {
            securityDept.idsEnabled = true
            securityDept.firewallSensitivity = (securityDept.firewallSensitivity + 2).coerceAtMost(10)

            eventBus?.emit(
                SystemEvent.CompanySecurityChanged(
                    companyId = organization.id,
                    companyName = organization.name,
                    newSecurityLevel = securityDept.firewallSensitivity
                )
            )

            eventBus?.emit(
                SystemEvent.NotificationTriggered(
                    title = "IDS ACTIVATED",
                    message = "${organization.name} Security Operations Center enabled Intrusion Detection System following network probing.",
                    level = com.example.backdoor.core.NotificationLevel.WARNING
                )
            )
        }
    }

    /**
     * Responds to a server breach or exploit.
     */
    fun registerServerBreached(targetHost: String, cveId: String? = null) {
        securityDept.firewallSensitivity = 10
        securityDept.idsEnabled = true
        if (cveId != null && !securityDept.pendingPatches.contains(cveId)) {
            securityDept.pendingPatches.add(cveId)
        }

        // Trigger law enforcement escalation via event
        eventBus?.emit(
            SystemEvent.ServerCompromised(
                targetHost = targetHost,
                companyId = organization.id
            )
        )
    }

    /**
     * Executes hourly simulation tick for the organization.
     */
    fun tick(
        vulnerabilityEngine: VulnerabilityEngine?,
        missionGenerator: ProceduralMissionGenerator?
    ) {
        val currentEmps = _employees.value.toMutableList()

        // 1. HR & Employee management (stress, resignations, leaks)
        val iterator = currentEmps.iterator()
        while (iterator.hasNext()) {
            val emp = iterator.next()
            // Random stress fluctuations
            if ((1..100).random() <= 15) {
                emp.stress = (emp.stress + (-5..10).random()).coerceIn(0, 100)
            }
            if ((1..100).random() <= 10) {
                emp.loyalty = (emp.loyalty + (-5..5).random()).coerceIn(0, 100)
            }

            // High stress / low loyalty employee resignation
            if ((emp.stress > 85 || emp.loyalty < 15) && (1..100).random() <= 8) {
                iterator.remove()
                eventBus?.emit(
                    SystemEvent.EmployeeResigned(
                        employeeId = emp.id,
                        employeeName = emp.name,
                        companyId = organization.id,
                        reason = if (emp.stress > 85) "Extreme Workplace Stress" else "Low Organizational Loyalty"
                    )
                )

                // 20% chance disloyal employee leaks corporate data
                if (emp.loyalty < 15 && (1..100).random() <= 20) {
                    eventBus?.emit(
                        SystemEvent.DataLeakDetected(
                            companyId = organization.id,
                            leakTitle = "Internal Leak: ${organization.name} (${emp.position.name})"
                        )
                    )
                }

                // Generate procedural audit mission
                missionGenerator?.generateMissionForResignation(organization.name, emp.name)
            }
        }

        // Replenish employees if team is understaffed
        if (currentEmps.size < 4) {
            val newEmp = Employee(
                name = "Specialist " + (100..999).random(),
                age = (23..55).random(),
                position = JobPosition.entries.random(),
                organizationId = organization.id,
                skillLevel = (3..9).random(),
                stress = 10,
                loyalty = 75
            )
            currentEmps.add(newEmp)
        }
        _employees.value = currentEmps

        // 2. Security & Patching Management
        if (securityDept.pendingPatches.isNotEmpty()) {
            val cveToPatch = securityDept.pendingPatches.removeAt(0)
            vulnerabilityEngine?.markPatchStatus(cveToPatch, PatchStatus.PATCHED)
            val serverId = organization.servers.firstOrNull()?.id ?: "srv-primary"
            eventBus?.emit(
                SystemEvent.ServerPatched(
                    serverId = serverId,
                    cveId = cveToPatch,
                    companyId = organization.id
                )
            )
        }

        // 3. Finance budget adjustments
        if ((1..100).random() <= 5) { // 5% chance per tick
            val delta = (-50000..150000).random().toDouble()
            financeDept.currentBudget = (financeDept.currentBudget + delta).coerceAtLeast(100000.0)
            eventBus?.emit(
                SystemEvent.BudgetChanged(
                    companyId = organization.id,
                    newBudget = financeDept.currentBudget
                )
            )
            if (delta > 100000.0) {
                missionGenerator?.generateMissionForPenTest(organization.name)
            }
        }
    }

    fun serializeToJson(): JSONObject {
        val obj = JSONObject()
        obj.put("orgId", organization.id)
        obj.put("firewallSensitivity", securityDept.firewallSensitivity)
        obj.put("idsEnabled", securityDept.idsEnabled)
        obj.put("probingCount", securityDept.probingCount)
        obj.put("currentBudget", financeDept.currentBudget)

        val empArr = JSONArray()
        _employees.value.forEach { emp ->
            val empObj = JSONObject()
            empObj.put("id", emp.id)
            empObj.put("name", emp.name)
            empObj.put("age", emp.age)
            empObj.put("position", emp.position.name)
            empObj.put("organizationId", emp.organizationId)
            empObj.put("salary", emp.salary)
            empObj.put("skillLevel", emp.skillLevel)
            empObj.put("stress", emp.stress)
            empObj.put("fatigue", emp.fatigue)
            empObj.put("loyalty", emp.loyalty)
            empObj.put("reputation", emp.reputation)
            empObj.put("productivity", emp.productivity)
            empObj.put("isAtWork", emp.isAtWork)
            empArr.put(empObj)
        }
        obj.put("employees", empArr)
        return obj
    }

    fun deserializeFromJson(obj: JSONObject) {
        if (obj.has("firewallSensitivity")) {
            securityDept.firewallSensitivity = obj.getInt("firewallSensitivity")
        }
        if (obj.has("idsEnabled")) {
            securityDept.idsEnabled = obj.getBoolean("idsEnabled")
        }
        if (obj.has("probingCount")) {
            securityDept.probingCount = obj.getInt("probingCount")
        }
        if (obj.has("currentBudget")) {
            financeDept.currentBudget = obj.getDouble("currentBudget")
        }

        if (obj.has("employees")) {
            val empArr = obj.getJSONArray("employees")
            val list = mutableListOf<Employee>()
            for (i in 0 until empArr.length()) {
                val empObj = empArr.getJSONObject(i)
                list.add(
                    Employee(
                        id = empObj.getString("id"),
                        name = empObj.getString("name"),
                        age = empObj.getInt("age"),
                        position = JobPosition.valueOf(empObj.getString("position")),
                        organizationId = empObj.getString("organizationId"),
                        salary = empObj.getInt("salary"),
                        skillLevel = empObj.getInt("skillLevel"),
                        stress = empObj.getInt("stress"),
                        fatigue = empObj.getInt("fatigue"),
                        loyalty = empObj.getInt("loyalty"),
                        reputation = if (empObj.has("reputation")) empObj.getInt("reputation") else 0,
                        productivity = empObj.getInt("productivity"),
                        isAtWork = empObj.getBoolean("isAtWork")
                    )
                )
            }
            _employees.value = list
        }
    }
}
