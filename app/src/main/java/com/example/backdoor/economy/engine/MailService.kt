package com.example.backdoor.economy.engine

import com.example.backdoor.economy.models.EmailMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MailService {
    private val _inbox = MutableStateFlow<List<EmailMessage>>(emptyList())
    val inbox: StateFlow<List<EmailMessage>> = _inbox.asStateFlow()

    fun receiveEmail(email: EmailMessage) {
        _inbox.update { listOf(email) + it }
    }

    fun markAsRead(emailId: String) {
        _inbox.update { current ->
            current.map { if (it.id == emailId) it.copy(isRead = true) else it }
        }
    }

    fun deleteEmail(emailId: String) {
        _inbox.update { it.filterNot { e -> e.id == emailId } }
    }

    fun restore(savedEmails: List<EmailMessage>) {
        _inbox.value = savedEmails
    }
}
