package com.example.backdoor.web.models

import java.util.UUID

enum class WebEntityType(val displayName: String) {
    CORPORATE("Corporate Portal"),
    PERSONAL_BLOG("Personal Blog"),
    NEWS("News Network"),
    DOCUMENTATION("Tech Documentation"),
    DARKNET_HIDDEN_SERVICE("DarkNet Hidden Service")
}

data class WebPage(
    val id: String = UUID.randomUUID().toString(),
    val slug: String,
    val title: String,
    val sectionName: String, // e.g., HOME, ABOUT, SERVICES, SECURITY STATUS, PRESS, CONTACT
    val content: String,
    val metadata: Map<String, String> = emptyMap()
)

data class WebEntity(
    val domain: String,
    val name: String,
    val ownerOrganizationId: String? = null,
    val serverIp: String? = null,
    val securityLevel: Int = 1,
    val contentType: WebEntityType = WebEntityType.CORPORATE,
    val pages: List<WebPage> = emptyList(),
    val dynamicData: Map<String, String> = emptyMap(),
    val eventHistory: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)
