package com.example.backdoor.simulation.engine

import com.example.backdoor.core.GameClock
import com.example.backdoor.core.SystemEventBus
import com.example.backdoor.core.SystemEvent
import com.example.backdoor.corporate.CorporateGridRepository
import com.example.backdoor.economy.engine.ShadowEconomyEngine
import com.example.backdoor.simulation.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LivingWorldEngine(
    private val scope: CoroutineScope,
    private val eventBus: SystemEventBus,
    val gameClock: GameClock,
    private val corporateManager: CorporateGridRepository,
    private val economyEngine: ShadowEconomyEngine
) {
    private val _organizationsAI = MutableStateFlow<Map<String, OrganizationAI>>(emptyMap())
    val organizationsAI: StateFlow<Map<String, OrganizationAI>> = _organizationsAI.asStateFlow()

    private val _incidents = MutableStateFlow<List<Incident>>(emptyList())
    val incidents: StateFlow<List<Incident>> = _incidents.asStateFlow()
    
    val socialEngine = NPCSocialEngine()
    val forumSimulation = ForumSimulation()

    private var isRunning = false
    private val simulationTickMs = 5000L

    fun initWorld() {
        val aiMap = mutableMapOf<String, OrganizationAI>()
        for (org in corporateManager.organizations.value) {
            val orgAi = OrganizationAI(org)
            orgAi.setEmployees(generateInitialEmployees(org.id, org.employeeCount))
            aiMap[org.id] = orgAi
        }
        _organizationsAI.value = aiMap
    }

    private fun generateInitialEmployees(orgId: String, count: Int): List<Employee> {
        val list = mutableListOf<Employee>()
        // Always generate a CEO, CTO, CISO
        list.add(Employee(name = "CEO", age = 50, position = JobPosition.CEO, organizationId = orgId, skillLevel = 10))
        list.add(Employee(name = "CTO", age = 45, position = JobPosition.CTO, organizationId = orgId, skillLevel = 9))
        list.add(Employee(name = "CISO", age = 42, position = JobPosition.CISO, organizationId = orgId, skillLevel = 9))

        val types = listOf(JobPosition.DEVELOPER, JobPosition.SYSADMIN, JobPosition.SOC_ANALYST, JobPosition.FINANCE_ANALYST, JobPosition.HR_MANAGER)
        val numToGen = (count.coerceAtMost(8) - 3).coerceAtLeast(0)
        for (i in 0 until numToGen) {
            list.add(Employee(name = "Employee ${i+1}", age = (22..60).random(), position = types.random(), organizationId = orgId, skillLevel = (1..10).random()))
        }
        return list
    }

    fun startSimulation() {
        if (isRunning) return
        isRunning = true
        scope.launch {
            while (isRunning) {
                delay(simulationTickMs)
                simulateTick()
            }
        }
    }

    fun stopSimulation() {
        isRunning = false
    }

    fun serializeToJson(): String {
        val root = org.json.JSONObject()
        
        // Serialize OrganizationsAI
        val aiMapArr = org.json.JSONArray()
        for ((id, orgAi) in _organizationsAI.value) {
            val orgAiObj = org.json.JSONObject()
            orgAiObj.put("orgId", id)
            val empArr = org.json.JSONArray()
            for (emp in orgAi.employees.value) {
                val empObj = org.json.JSONObject()
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
            orgAiObj.put("employees", empArr)
            aiMapArr.put(orgAiObj)
        }
        root.put("organizationsAI", aiMapArr)
        
        // Serialize Incidents
        val incArr = org.json.JSONArray()
        for (inc in _incidents.value) {
            val incObj = org.json.JSONObject()
            incObj.put("id", inc.id)
            incObj.put("type", inc.type.name)
            incObj.put("severity", inc.severity.name)
            incObj.put("organizationId", inc.organizationId)
            inc.targetServerId?.let { incObj.put("targetServerId", it) }
            incObj.put("timestamp", inc.timestamp)
            incObj.put("resolved", inc.resolved)
            incArr.put(incObj)
        }
        root.put("incidents", incArr)
        
        root.put("forumSimulation", forumSimulation.serializeToJson())
        
        // Serialize Game Clock
        val clockObj = org.json.JSONObject()
        val clockTime = gameClock.time.value
        clockObj.put("year", clockTime.year)
        clockObj.put("month", clockTime.month)
        clockObj.put("day", clockTime.day)
        clockObj.put("hour", clockTime.hour)
        clockObj.put("minute", clockTime.minute)
        root.put("gameClock", clockObj)
        
        return root.toString()
    }

    fun deserializeFromJson(json: String) {
        try {
            val root = org.json.JSONObject(json)
            
            // Deserialize OrganizationsAI
            val aiMapArr = root.getJSONArray("organizationsAI")
            val aiMap = mutableMapOf<String, OrganizationAI>()
            for (i in 0 until aiMapArr.length()) {
                val orgAiObj = aiMapArr.getJSONObject(i)
                val orgId = orgAiObj.getString("orgId")
                val org = corporateManager.organizations.value.find { it.id == orgId }
                if (org != null) {
                    val orgAi = OrganizationAI(org)
                    val empArr = orgAiObj.getJSONArray("employees")
                    val employees = mutableListOf<Employee>()
                    for (j in 0 until empArr.length()) {
                        val empObj = empArr.getJSONObject(j)
                        employees.add(Employee(
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
                        ))
                    }
                    orgAi.setEmployees(employees)
                    aiMap[orgId] = orgAi
                }
            }
            _organizationsAI.value = aiMap
            
            // Deserialize Incidents
            val incArr = root.getJSONArray("incidents")
            val incidents = mutableListOf<Incident>()
            for (i in 0 until incArr.length()) {
                val incObj = incArr.getJSONObject(i)
                incidents.add(Incident(
                    id = incObj.getString("id"),
                    type = IncidentType.valueOf(incObj.getString("type")),
                    severity = IncidentSeverity.valueOf(incObj.getString("severity")),
                    organizationId = incObj.getString("organizationId"),
                    targetServerId = if (incObj.has("targetServerId")) incObj.getString("targetServerId") else null,
                    timestamp = incObj.getString("timestamp"),
                    resolved = if (incObj.has("resolved")) incObj.getBoolean("resolved") else false
                ))
            }
            _incidents.value = incidents
            
            if (root.has("forumSimulation")) {
                forumSimulation.deserializeFromJson(root.getString("forumSimulation"))
            }
            
            // Deserialize Game Clock
            if (root.has("gameClock")) {
                val clockObj = root.getJSONObject("gameClock")
                gameClock.setTime(com.example.backdoor.core.WorldTime(
                    year = clockObj.getInt("year"),
                    month = clockObj.getInt("month"),
                    day = clockObj.getInt("day"),
                    hour = clockObj.getInt("hour"),
                    minute = clockObj.getInt("minute")
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            initWorld() // Fallback
        }
    }

    private fun simulateTick() {
        val time = gameClock.time.value
        eventBus.emit(SystemEvent.NotificationTriggered("LIVING_WORLD", "Time ticked to $time", com.example.backdoor.core.NotificationLevel.INFO))
        
        // 1. Employee routines
        simulateEmployees(time)
        // 2. Incident generation
        simulateIncidents(time)
        // 3. Email/News updates
        simulateEmailsAndNews(time)
        
        // 4. Social & Forums
        val allEmployees = _organizationsAI.value.values.flatMap { it.employees.value }
        socialEngine.tick(allEmployees)
        forumSimulation.tick()
    }

    private fun simulateEmployees(time: com.example.backdoor.core.WorldTime) {
        val aiMap = _organizationsAI.value
        for ((_, orgAi) in aiMap) {
            val emps = orgAi.employees.value
            for (emp in emps) {
                // simple routine: work from 9 to 17
                if (time.hour in 9..16 && !emp.isAtWork) {
                    emp.isAtWork = true
                } else if ((time.hour < 9 || time.hour >= 17) && emp.isAtWork) {
                    emp.isAtWork = false
                }
            }
        }
    }

    private fun simulateIncidents(time: com.example.backdoor.core.WorldTime) {
        if ((1..100).random() > 95) { // 5% chance per simulation tick
            val aiMap = _organizationsAI.value
            if (aiMap.isNotEmpty()) {
                val randomOrg = aiMap.values.random().organization
                val type = IncidentType.entries.random()
                val severity = IncidentSeverity.entries.random()
                val incident = Incident(
                    type = type,
                    severity = severity,
                    organizationId = randomOrg.id,
                    targetServerId = randomOrg.servers.randomOrNull()?.id,
                    timestamp = time.toString()
                )
                
                val list = _incidents.value.toMutableList()
                list.add(incident)
                _incidents.value = list

                eventBus.emit(SystemEvent.NotificationTriggered("INCIDENT", "Incident at ${randomOrg.name}: $type", com.example.backdoor.core.NotificationLevel.WARNING))
            }
        }
    }

    private fun simulateEmailsAndNews(time: com.example.backdoor.core.WorldTime) {
        if ((1..100).random() > 98) {
            val aiMap = _organizationsAI.value
            if (aiMap.isNotEmpty()) {
                val org = aiMap.values.random().organization
                val article = com.example.backdoor.economy.models.NewsArticle(
                    id = java.util.UUID.randomUUID().toString(),
                    title = "Market changes for ${org.name}",
                    content = "The global market is reacting to recent events at ${org.name}. Stock values fluctuate.",
                    source = "Global Financial News",
                    timestamp = System.currentTimeMillis(),
                    category = com.example.backdoor.economy.models.NewsCategory.CORPORATE
                )
                economyEngine.newsService.publishArticle(article)
            }
        }
    }
}
