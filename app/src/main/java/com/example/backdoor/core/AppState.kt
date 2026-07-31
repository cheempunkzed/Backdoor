package com.example.backdoor.core

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

interface AppState

class TerminalAppState : AppState {
    var inputCommand = mutableStateOf("")
    var historyIndex = mutableStateOf(-1)
    val history: SnapshotStateList<Any> = mutableStateListOf() // holds TerminalHistoryItem
}

class BrowserAppState : AppState {
    var currentUrl = mutableStateOf("router.local")
    var urlInput = mutableStateOf("router.local")
    var selectedThreadId = mutableStateOf<String?>(null)
    var replyInput = mutableStateOf("")
}

class FilesAppState : AppState {
    var currentPath = mutableStateOf("/home/operator")
    var showHidden = mutableStateOf(false)
}

class SettingsAppState : AppState {
    var selectedCategory = mutableStateOf("SYSTEM")
}

class DarkNetAppState : AppState {
    var selectedTab = mutableStateOf("OVERVIEW")
}

class NetworkAppState : AppState {
    var selectedTab = mutableStateOf("TOPOLOGY")
}

class LogsAppState : AppState {
    var selectedFilter = mutableStateOf("ALL")
}

class SystemMonitorAppState : AppState {
    var selectedTab = mutableStateOf("PROCESSES")
}
