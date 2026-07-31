package com.example.backdoor.simulation.models

import java.util.UUID

enum class Department {
    EXECUTIVE, HR, FINANCE, IT, SOC, DEVELOPMENT, RESEARCH, LEGAL
}

enum class JobPosition(val department: Department, val baseSalary: Int) {
    CEO(Department.EXECUTIVE, 10000),
    CTO(Department.EXECUTIVE, 9000),
    CISO(Department.EXECUTIVE, 9000),
    HR_MANAGER(Department.HR, 4000),
    FINANCE_ANALYST(Department.FINANCE, 5000),
    SYSADMIN(Department.IT, 4500),
    SOC_ANALYST(Department.SOC, 5500),
    DEVELOPER(Department.DEVELOPMENT, 6000),
    RESEARCHER(Department.RESEARCH, 6500),
    LEGAL_ADVISOR(Department.LEGAL, 7000)
}

data class Employee(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    var age: Int,
    var position: JobPosition,
    var organizationId: String,
    var salary: Int = position.baseSalary,
    var skillLevel: Int = 1, // 1 to 10
    var stress: Int = 0, // 0 to 100
    var fatigue: Int = 0, // 0 to 100
    var loyalty: Int = 50, // 0 to 100
    var reputation: Int = 0,
    var productivity: Int = 100, // %
    var isAtWork: Boolean = false
)
