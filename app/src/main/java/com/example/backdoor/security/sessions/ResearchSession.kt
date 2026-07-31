package com.example.backdoor.security.sessions

import com.example.backdoor.security.framework.SecurityTask

data class ResearchSession(
    val id: String,
    val target: String,
    val startTime: Long = System.currentTimeMillis(),
    var endTime: Long? = null,
    val tasksExecuted: MutableList<SecurityTask> = mutableListOf(),
    var notes: String = "",
    val reportsGenerated: MutableList<String> = mutableListOf()
)

class SessionHistory {
    private val sessions = mutableListOf<ResearchSession>()

    fun startNewSession(target: String): ResearchSession {
        val newSession = ResearchSession(
            id = "sess-${System.currentTimeMillis() % 1000000}",
            target = target
        )
        sessions.add(0, newSession)
        return newSession
    }

    fun getAllSessions(): List<ResearchSession> = sessions

    fun getSessionById(id: String): ResearchSession? = sessions.find { it.id == id }

    fun addSession(session: ResearchSession) {
        sessions.add(0, session)
    }
}
