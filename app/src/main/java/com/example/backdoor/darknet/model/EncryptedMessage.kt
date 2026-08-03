package com.example.backdoor.darknet.model

import java.util.UUID

data class EncryptedMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderHandle: String,
    val recipientHandle: String,
    val subject: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val attachmentItemOrKey: String? = null,
    val pgpSignature: String? = DigitalIdentity.generatePgpFingerprint(),
    val isEncrypted: Boolean = true
)
