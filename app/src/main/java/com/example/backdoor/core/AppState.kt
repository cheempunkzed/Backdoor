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

data class BrowserTab(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "New Tab",
    val url: String = "router.local",
    val urlInput: String = "router.local",
    val history: List<String> = listOf("router.local"),
    val historyIndex: Int = 0,
    val selectedThreadId: String? = null,
    val replyInput: String = ""
)

data class Bookmark(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val category: String = "Personal"
)

data class BrowserSettings(
    val homepage: String = "router.local",
    val defaultSearch: String = "wiki.abyss",
    val animationsEnabled: Boolean = true,
    val compactMode: Boolean = false,
    val fontSizeMultiplier: Float = 1.0f,
    val privacyMode: Boolean = false
)

class BrowserAppState : AppState {
    var currentUrl = androidx.compose.runtime.mutableStateOf("router.local")
    var urlInput = androidx.compose.runtime.mutableStateOf("router.local")
    var selectedThreadId = androidx.compose.runtime.mutableStateOf<String?>(null)
    var replyInput = androidx.compose.runtime.mutableStateOf("")

    // Tab Management
    var activeTabId = androidx.compose.runtime.mutableStateOf("")
    val tabs = androidx.compose.runtime.mutableStateListOf<BrowserTab>()

    // Bookmarks Management
    val bookmarks = androidx.compose.runtime.mutableStateListOf<Bookmark>()

    // History & Downloads
    val historyList = androidx.compose.runtime.mutableStateListOf<String>()
    val downloadsList = androidx.compose.runtime.mutableStateListOf<String>()

    // Settings
    var settings = androidx.compose.runtime.mutableStateOf(BrowserSettings())

    var isLoaded = androidx.compose.runtime.mutableStateOf(false)

    init {
        // Initialize with default tab if empty
        if (tabs.isEmpty()) {
            createNewTab("router.local")
        }
        // Initialize with default bookmarks
        if (bookmarks.isEmpty()) {
            bookmarks.add(Bookmark(title = "Router Admin", url = "router.local", category = "Network"))
            bookmarks.add(Bookmark(title = "Abyss Wiki", url = "wiki.abyss", category = "Research"))
            bookmarks.add(Bookmark(title = "Hidden Services", url = "dir.onion", category = "Darknet"))
            bookmarks.add(Bookmark(title = "Underground Forum", url = "abyss-forum.onion", category = "Darknet"))
            bookmarks.add(Bookmark(title = "Classified Leaks", url = "blackvault.onion", category = "Darknet"))
            bookmarks.add(Bookmark(title = "Shadow Exchange", url = "darkmarket.onion", category = "Darknet"))
        }
    }

    fun addBookmark(title: String, url: String, category: String) {
        bookmarks.add(Bookmark(title = title, url = url, category = category))
    }

    fun removeBookmark(id: String) {
        bookmarks.removeAll { it.id == id }
    }

    fun editBookmark(id: String, title: String, url: String, category: String) {
        val idx = bookmarks.indexOfFirst { it.id == id }
        if (idx >= 0) {
            bookmarks[idx] = Bookmark(id = id, title = title, url = url, category = category)
        }
    }

    fun getFriendlyTitle(url: String): String {
        return when {
            url.equals("router.local", ignoreCase = true) || url.equals("192.168.1.1") -> "Router Admin"
            url.equals("localhost", ignoreCase = true) || url.equals("127.0.0.1") -> "Localhost Node"
            url.equals("about:network", ignoreCase = true) -> "Abyss Network Map"
            url.contains("wiki", ignoreCase = true) || url.contains("docs", ignoreCase = true) -> "Abyss Wiki"
            url.equals("dir.onion", ignoreCase = true) -> "Hidden Services Directory"
            url.equals("abyss-forum.onion", ignoreCase = true) -> "Underground Forum"
            url.equals("blackvault.onion", ignoreCase = true) -> "Classified Leaks"
            url.equals("cipherroom.onion", ignoreCase = true) -> "Cipher Chat"
            url.equals("whistleblower.onion", ignoreCase = true) -> "Whistleblower Portal"
            url.equals("darkmarket.onion", ignoreCase = true) -> "Shadow Exchange"
            url.equals("shadowblog.onion", ignoreCase = true) -> "Shadow Blog"
            url.equals("zero-day.onion", ignoreCase = true) -> "Zero-Day Cell"
            url.endsWith(".onion") -> "Darknet Onion Site"
            url.endsWith(".local") -> "Local Node"
            else -> url
        }
    }

    fun selectTab(tabId: String) {
        val tab = tabs.find { it.id == tabId } ?: return
        activeTabId.value = tabId
        currentUrl.value = tab.url
        urlInput.value = tab.urlInput
        selectedThreadId.value = tab.selectedThreadId
        replyInput.value = tab.replyInput
    }

    fun navigateActiveTabTo(newUrl: String) {
        val activeId = activeTabId.value
        val tabIdx = tabs.indexOfFirst { it.id == activeId }
        if (tabIdx >= 0) {
            val tab = tabs[tabIdx]
            
            val newHistory = tab.history.take(tab.historyIndex + 1) + newUrl
            val newIdx = newHistory.lastIndex
            
            val updated = tab.copy(
                title = getFriendlyTitle(newUrl),
                url = newUrl,
                urlInput = newUrl,
                history = newHistory,
                historyIndex = newIdx,
                selectedThreadId = null,
                replyInput = ""
            )
            tabs[tabIdx] = updated
            
            currentUrl.value = newUrl
            urlInput.value = newUrl
            selectedThreadId.value = null
            replyInput.value = ""

            if (historyList.firstOrNull() != newUrl) {
                historyList.add(0, newUrl)
            }
        }
    }

    fun goBackInActiveTab() {
        val activeId = activeTabId.value
        val tabIdx = tabs.indexOfFirst { it.id == activeId }
        if (tabIdx >= 0) {
            val tab = tabs[tabIdx]
            if (tab.historyIndex > 0) {
                val newIdx = tab.historyIndex - 1
                val newUrl = tab.history[newIdx]
                val updated = tab.copy(
                    url = newUrl,
                    urlInput = newUrl,
                    historyIndex = newIdx,
                    title = getFriendlyTitle(newUrl),
                    selectedThreadId = null,
                    replyInput = ""
                )
                tabs[tabIdx] = updated
                
                currentUrl.value = newUrl
                urlInput.value = newUrl
                selectedThreadId.value = null
                replyInput.value = ""
            }
        }
    }

    fun goForwardInActiveTab() {
        val activeId = activeTabId.value
        val tabIdx = tabs.indexOfFirst { it.id == activeId }
        if (tabIdx >= 0) {
            val tab = tabs[tabIdx]
            if (tab.historyIndex < tab.history.size - 1) {
                val newIdx = tab.historyIndex + 1
                val newUrl = tab.history[newIdx]
                val updated = tab.copy(
                    url = newUrl,
                    urlInput = newUrl,
                    historyIndex = newIdx,
                    title = getFriendlyTitle(newUrl),
                    selectedThreadId = null,
                    replyInput = ""
                )
                tabs[tabIdx] = updated
                
                currentUrl.value = newUrl
                urlInput.value = newUrl
                selectedThreadId.value = null
                replyInput.value = ""
            }
        }
    }

    fun canGoBackInActiveTab(): Boolean {
        val activeId = activeTabId.value
        val tab = tabs.find { it.id == activeId } ?: return false
        return tab.historyIndex > 0
    }

    fun canGoForwardInActiveTab(): Boolean {
        val activeId = activeTabId.value
        val tab = tabs.find { it.id == activeId } ?: return false
        return tab.historyIndex < tab.history.size - 1
    }

    fun createNewTab(url: String = "router.local") {
        val newTab = BrowserTab(
            id = java.util.UUID.randomUUID().toString(),
            title = getFriendlyTitle(url),
            url = url,
            urlInput = url,
            history = listOf(url),
            historyIndex = 0
        )
        tabs.add(newTab)
        selectTab(newTab.id)
    }

    fun closeTab(tabId: String) {
        if (tabs.size <= 1) {
            val idx = tabs.indexOfFirst { it.id == tabId }
            if (idx >= 0) {
                tabs[idx] = BrowserTab(id = tabId)
                selectTab(tabId)
            }
            return
        }
        val idx = tabs.indexOfFirst { it.id == tabId }
        if (idx >= 0) {
            tabs.removeAt(idx)
            if (activeTabId.value == tabId) {
                val nextSelectIdx = if (idx < tabs.size) idx else tabs.size - 1
                selectTab(tabs[nextSelectIdx].id)
            }
        }
    }

    // JSON Serialization/Deserialization
    fun serializeTabs(): String {
        val arr = org.json.JSONArray()
        for (tab in tabs) {
            val obj = org.json.JSONObject()
            obj.put("id", tab.id)
            obj.put("title", tab.title)
            obj.put("url", tab.url)
            obj.put("urlInput", tab.urlInput)
            val histArr = org.json.JSONArray()
            tab.history.forEach { histArr.put(it) }
            obj.put("history", histArr)
            obj.put("historyIndex", tab.historyIndex)
            obj.put("selectedThreadId", tab.selectedThreadId ?: "")
            obj.put("replyInput", tab.replyInput)
            arr.put(obj)
        }
        val root = org.json.JSONObject()
        root.put("tabs", arr)
        root.put("activeTabId", activeTabId.value)
        return root.toString()
    }

    fun deserializeTabs(json: String) {
        try {
            val root = org.json.JSONObject(json)
            val arr = root.getJSONArray("tabs")
            tabs.clear()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.getString("id")
                val title = obj.getString("title")
                val url = obj.getString("url")
                val urlInput = obj.getString("urlInput")
                val histArr = obj.getJSONArray("history")
                val history = mutableListOf<String>()
                for (j in 0 until histArr.length()) {
                    history.add(histArr.getString(j))
                }
                val historyIndex = obj.getInt("historyIndex")
                val selThread = obj.optString("selectedThreadId", "")
                val reply = obj.optString("replyInput", "")
                
                tabs.add(BrowserTab(
                    id = id,
                    title = title,
                    url = url,
                    urlInput = urlInput,
                    history = history,
                    historyIndex = historyIndex,
                    selectedThreadId = if (selThread.isEmpty()) null else selThread,
                    replyInput = reply
                ))
            }
            val activeId = root.optString("activeTabId", "")
            if (activeId.isNotEmpty()) {
                selectTab(activeId)
            } else if (tabs.isNotEmpty()) {
                selectTab(tabs.first().id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (tabs.isEmpty()) {
                createNewTab("router.local")
            }
        }
    }

    fun serializeBookmarks(): String {
        val arr = org.json.JSONArray()
        for (b in bookmarks) {
            val obj = org.json.JSONObject()
            obj.put("id", b.id)
            obj.put("title", b.title)
            obj.put("url", b.url)
            obj.put("category", b.category)
            arr.put(obj)
        }
        return arr.toString()
    }

    fun deserializeBookmarks(json: String) {
        try {
            val arr = org.json.JSONArray(json)
            bookmarks.clear()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                bookmarks.add(Bookmark(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    url = obj.getString("url"),
                    category = obj.getString("category")
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun serializeHistory(): String {
        val arr = org.json.JSONArray()
        historyList.forEach { arr.put(it) }
        return arr.toString()
    }

    fun deserializeHistory(json: String) {
        try {
            val arr = org.json.JSONArray(json)
            historyList.clear()
            for (i in 0 until arr.length()) {
                historyList.add(arr.getString(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun serializeSettings(): String {
        val obj = org.json.JSONObject()
        obj.put("homepage", settings.value.homepage)
        obj.put("defaultSearch", settings.value.defaultSearch)
        obj.put("animationsEnabled", settings.value.animationsEnabled)
        obj.put("compactMode", settings.value.compactMode)
        obj.put("fontSizeMultiplier", settings.value.fontSizeMultiplier.toDouble())
        obj.put("privacyMode", settings.value.privacyMode)
        return obj.toString()
    }

    fun deserializeSettings(json: String) {
        try {
            val obj = org.json.JSONObject(json)
            settings.value = BrowserSettings(
                homepage = obj.optString("homepage", "router.local"),
                defaultSearch = obj.optString("defaultSearch", "wiki.abyss"),
                animationsEnabled = obj.optBoolean("animationsEnabled", true),
                compactMode = obj.optBoolean("compactMode", false),
                fontSizeMultiplier = obj.optDouble("fontSizeMultiplier", 1.0).toFloat(),
                privacyMode = obj.optBoolean("privacyMode", false)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
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

class WalletAppState : AppState {
    var selectedTab = mutableStateOf("OVERVIEW")
}

class ContractsAppState : AppState {
    var selectedTab = mutableStateOf("AVAILABLE")
}

class MarketplaceAppState : AppState {
    var selectedCategory = mutableStateOf("ALL")
}

class MailAppState : AppState {
    var selectedMessageId = mutableStateOf<String?>(null)
}

class NewsAppState : AppState {
    var selectedCategory = mutableStateOf("ALL")
}

class InventoryAppState : AppState {
    var selectedFilter = mutableStateOf("ALL")
}

