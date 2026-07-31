package com.example.backdoor.network.models

/**
 * Represents a network service running on a specific port of a node.
 *
 * @property name Service identifier (e.g. HTTP, SSH, DNS, Database)
 * @property port Service port number
 * @property isOpen Whether the port is open and accepting socket connections
 * @property banner Service banner response or metadata
 */
data class NetworkService(
    val name: String,
    val port: Int,
    val isOpen: Boolean = true,
    val banner: String = ""
)
