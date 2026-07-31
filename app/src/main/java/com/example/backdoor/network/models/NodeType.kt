package com.example.backdoor.network.models

/**
 * Represents the architectural category of a network node within AbyssNet.
 */
enum class NodeType(val displayName: String) {
    PERSONAL_DEVICE("Personal Device"),
    ROUTER("Router"),
    SWITCH("Switch"),
    SERVER("Server"),
    DATABASE("Database"),
    FIREWALL("Firewall"),
    IOT_DEVICE("IoT Device"),
    UNKNOWN_DEVICE("Unknown Device")
}
