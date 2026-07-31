package com.example.backdoor.network.models

import java.util.UUID

/**
 * Represents a discrete virtual data packet routed across AbyssNet.
 *
 * @property id Unique packet ID
 * @property sourceIp Source IPv4 address
 * @property destinationIp Destination IPv4 address
 * @property protocol Transport protocol (ICMP, TCP, UDP, HTTP, DNS)
 * @property payload Packet message content
 * @property ttl Time-To-Live hop counter
 * @property timestamp Transmission time
 */
data class Packet(
    val id: String = UUID.randomUUID().toString(),
    val sourceIp: String,
    val destinationIp: String,
    val protocol: String = "ICMP",
    val payload: String = "",
    val ttl: Int = 64,
    val timestamp: Long = System.currentTimeMillis()
)
