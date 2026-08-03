package com.example.backdoor.save

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

interface SaveManager {
    val currentSaveState: Flow<SaveSlotEntity?>
    fun attachContext(context: Context)
    suspend fun saveGame(slotId: Int = 1, saveName: String = "AutoSave"): Boolean
    suspend fun loadGame(slotId: Int = 1): SaveSlotEntity?
    suspend fun resetGameData(): Boolean

    suspend fun getUserProfile(): UserProfile?
    suspend fun saveUserProfile(profile: UserProfile): Boolean

    suspend fun getTerminalHistory(): List<String>
    suspend fun saveTerminalHistory(history: List<String>): Boolean

    suspend fun getVfsDataJson(): String?
    suspend fun saveVfsDataJson(json: String): Boolean

    suspend fun getDockPinnedAppsJson(): String?
    suspend fun saveDockPinnedAppsJson(json: String): Boolean

    suspend fun getDesktopPositionsJson(): String?
    suspend fun saveDesktopPositionsJson(json: String): Boolean

    suspend fun getNetworkTopologyJson(): String?
    suspend fun saveNetworkTopologyJson(json: String): Boolean

    suspend fun getCorporateGridJson(): String?
    suspend fun saveCorporateGridJson(json: String): Boolean

    suspend fun getSecurityFrameworkJson(): String?
    suspend fun saveSecurityFrameworkJson(json: String): Boolean

    suspend fun getDarknetJson(): String?
    suspend fun saveDarknetJson(json: String): Boolean

    suspend fun getEconomyJson(): String?
    suspend fun saveEconomyJson(json: String): Boolean

    suspend fun getLivingWorldJson(): String?
    suspend fun saveLivingWorldJson(json: String): Boolean

    suspend fun getBrowserBookmarksJson(): String?
    suspend fun saveBrowserBookmarksJson(json: String): Boolean

    suspend fun getBrowserTabsJson(): String?
    suspend fun saveBrowserTabsJson(json: String): Boolean

    suspend fun getBrowserHistoryJson(): String?
    suspend fun saveBrowserHistoryJson(json: String): Boolean

    suspend fun getBrowserSettingsJson(): String?
    suspend fun saveBrowserSettingsJson(json: String): Boolean

    suspend fun getWebContentJson(): String?
    suspend fun saveWebContentJson(json: String): Boolean
}

class MemorySaveManager(
    private var appContext: Context? = null
) : SaveManager {

    private var prefs = appContext?.getSharedPreferences("abyss_os_save_prefs", Context.MODE_PRIVATE)

    override fun attachContext(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
            prefs = appContext?.getSharedPreferences("abyss_os_save_prefs", Context.MODE_PRIVATE)
            loadFromDisk()
        }
    }

    private fun loadFromDisk() {
        prefs?.let { p ->
            val uName = p.getString("profile_username", null)
            val pHash = p.getString("profile_hash", null)
            if (uName != null && pHash != null) {
                storedProfile = UserProfile(
                    username = uName,
                    passwordHash = pHash,
                    registeredAt = p.getLong("profile_created", System.currentTimeMillis()),
                    lastLoginAt = p.getLong("profile_last_login", System.currentTimeMillis())
                )
            }
            storedVfsJson = p.getString("vfs_data", null)
            storedDockJson = p.getString("dock_data", null)
            storedDesktopJson = p.getString("desktop_data", null)
            storedNetworkJson = p.getString("network_data", null)
            storedCorporateJson = p.getString("corporate_data", null)
            storedSecurityJson = p.getString("security_data", null)
            storedDarknetJson = p.getString("darknet_data", null)
            storedEconomyJson = p.getString("economy_data", null)
            storedLivingWorldJson = p.getString("living_world_data", null)
            storedBrowserBookmarksJson = p.getString("bookmarks_data", null)
            storedBrowserTabsJson = p.getString("tabs_data", null)
            storedBrowserHistoryJson = p.getString("history_data", null)
            storedBrowserSettingsJson = p.getString("settings_data", null)
            storedWebContentJson = p.getString("web_content_data", null)
        }
    }

    private var storedProfile: UserProfile? = null
    private val storedTerminalHistory = mutableListOf<String>()
    private var storedVfsJson: String? = null
    private var storedDockJson: String? = null
    private var storedDesktopJson: String? = null
    private var storedNetworkJson: String? = null
    private var storedCorporateJson: String? = null
    private var storedSecurityJson: String? = null
    private var storedDarknetJson: String? = null
    private var storedEconomyJson: String? = null
    private var storedLivingWorldJson: String? = null
    private var storedBrowserBookmarksJson: String? = null
    private var storedBrowserTabsJson: String? = null
    private var storedBrowserHistoryJson: String? = null
    private var storedBrowserSettingsJson: String? = null
    private var storedWebContentJson: String? = null

    private val _currentSave = MutableStateFlow<SaveSlotEntity?>(
        SaveSlotEntity(
            slotId = 1,
            saveName = "AbyssOS Default Profile",
            timestamp = System.currentTimeMillis(),
            playerHandle = "operator",
            osVersion = "0.9.0"
        )
    )
    override val currentSaveState: Flow<SaveSlotEntity?> = _currentSave.asStateFlow()

    init {
        loadFromDisk()
    }

    override suspend fun saveGame(slotId: Int, saveName: String): Boolean {
        val currentSlot = _currentSave.value
        val newSave = SaveSlotEntity(
            slotId = slotId,
            saveName = saveName,
            timestamp = System.currentTimeMillis(),
            playTimeSeconds = (currentSlot?.playTimeSeconds ?: 0L) + 60L,
            playerHandle = storedProfile?.username ?: currentSlot?.playerHandle ?: "operator",
            osVersion = "0.3.0",
            vfsDataJson = storedVfsJson ?: currentSlot?.vfsDataJson ?: ""
        )
        _currentSave.value = newSave
        prefs?.edit()?.putLong("last_save_time", System.currentTimeMillis())?.apply()
        return true
    }

    override suspend fun loadGame(slotId: Int): SaveSlotEntity? {
        loadFromDisk()
        return _currentSave.value
    }

    override suspend fun resetGameData(): Boolean {
        storedProfile = null
        storedTerminalHistory.clear()
        storedVfsJson = null
        storedDockJson = null
        storedDesktopJson = null
        storedNetworkJson = null
        storedCorporateJson = null
        storedSecurityJson = null
        storedDarknetJson = null
        storedEconomyJson = null
        storedLivingWorldJson = null
        storedBrowserBookmarksJson = null
        storedBrowserTabsJson = null
        storedBrowserHistoryJson = null
        storedBrowserSettingsJson = null
        storedWebContentJson = null

        prefs?.edit()?.clear()?.apply()

        val resetSave = SaveSlotEntity(
            slotId = 1,
            saveName = "New Operator Session",
            timestamp = System.currentTimeMillis(),
            playTimeSeconds = 0L,
            playerHandle = "operator",
            osVersion = "0.3.0"
        )
        _currentSave.value = resetSave
        return true
    }

    override suspend fun getUserProfile(): UserProfile? {
        if (storedProfile == null) loadFromDisk()
        return storedProfile
    }

    override suspend fun saveUserProfile(profile: UserProfile): Boolean {
        storedProfile = profile
        _currentSave.value = _currentSave.value?.copy(playerHandle = profile.username)
        prefs?.edit()
            ?.putString("profile_username", profile.username)
            ?.putString("profile_hash", profile.passwordHash)
            ?.putLong("profile_created", profile.registeredAt)
            ?.putLong("profile_last_login", profile.lastLoginAt)
            ?.apply()
        return true
    }

    override suspend fun getTerminalHistory(): List<String> {
        return storedTerminalHistory.toList()
    }

    override suspend fun saveTerminalHistory(history: List<String>): Boolean {
        storedTerminalHistory.clear()
        storedTerminalHistory.addAll(history.takeLast(100))
        return true
    }

    override suspend fun getVfsDataJson(): String? {
        if (storedVfsJson == null) loadFromDisk()
        return storedVfsJson ?: _currentSave.value?.vfsDataJson
    }

    override suspend fun saveVfsDataJson(json: String): Boolean {
        storedVfsJson = json
        _currentSave.value = _currentSave.value?.copy(vfsDataJson = json)
        prefs?.edit()?.putString("vfs_data", json)?.apply()
        return true
    }

    override suspend fun getDockPinnedAppsJson(): String? {
        if (storedDockJson == null) loadFromDisk()
        return storedDockJson
    }

    override suspend fun saveDockPinnedAppsJson(json: String): Boolean {
        storedDockJson = json
        prefs?.edit()?.putString("dock_data", json)?.apply()
        return true
    }

    override suspend fun getDesktopPositionsJson(): String? {
        if (storedDesktopJson == null) loadFromDisk()
        return storedDesktopJson
    }

    override suspend fun saveDesktopPositionsJson(json: String): Boolean {
        storedDesktopJson = json
        prefs?.edit()?.putString("desktop_data", json)?.apply()
        return true
    }

    override suspend fun getNetworkTopologyJson(): String? {
        if (storedNetworkJson == null) loadFromDisk()
        return storedNetworkJson
    }

    override suspend fun saveNetworkTopologyJson(json: String): Boolean {
        storedNetworkJson = json
        prefs?.edit()?.putString("network_data", json)?.apply()
        return true
    }

    override suspend fun getCorporateGridJson(): String? {
        if (storedCorporateJson == null) loadFromDisk()
        return storedCorporateJson
    }

    override suspend fun saveCorporateGridJson(json: String): Boolean {
        storedCorporateJson = json
        prefs?.edit()?.putString("corporate_data", json)?.apply()
        return true
    }

    override suspend fun getSecurityFrameworkJson(): String? {
        if (storedSecurityJson == null) loadFromDisk()
        return storedSecurityJson
    }

    override suspend fun saveSecurityFrameworkJson(json: String): Boolean {
        storedSecurityJson = json
        prefs?.edit()?.putString("security_data", json)?.apply()
        return true
    }

    override suspend fun getDarknetJson(): String? {
        if (storedDarknetJson == null) loadFromDisk()
        return storedDarknetJson
    }

    override suspend fun saveDarknetJson(json: String): Boolean {
        storedDarknetJson = json
        prefs?.edit()?.putString("darknet_data", json)?.apply()
        return true
    }

    override suspend fun getEconomyJson(): String? {
        if (storedEconomyJson == null) loadFromDisk()
        return storedEconomyJson
    }

    override suspend fun saveEconomyJson(json: String): Boolean {
        storedEconomyJson = json
        prefs?.edit()?.putString("economy_data", json)?.apply()
        return true
    }

    override suspend fun getLivingWorldJson(): String? {
        if (storedLivingWorldJson == null) loadFromDisk()
        return storedLivingWorldJson
    }

    override suspend fun saveLivingWorldJson(json: String): Boolean {
        storedLivingWorldJson = json
        prefs?.edit()?.putString("living_world_data", json)?.apply()
        return true
    }

    override suspend fun getBrowserBookmarksJson(): String? {
        if (storedBrowserBookmarksJson == null) loadFromDisk()
        return storedBrowserBookmarksJson
    }

    override suspend fun saveBrowserBookmarksJson(json: String): Boolean {
        storedBrowserBookmarksJson = json
        prefs?.edit()?.putString("bookmarks_data", json)?.apply()
        return true
    }

    override suspend fun getBrowserTabsJson(): String? {
        if (storedBrowserTabsJson == null) loadFromDisk()
        return storedBrowserTabsJson
    }

    override suspend fun saveBrowserTabsJson(json: String): Boolean {
        storedBrowserTabsJson = json
        prefs?.edit()?.putString("tabs_data", json)?.apply()
        return true
    }

    override suspend fun getBrowserHistoryJson(): String? {
        if (storedBrowserHistoryJson == null) loadFromDisk()
        return storedBrowserHistoryJson
    }

    override suspend fun saveBrowserHistoryJson(json: String): Boolean {
        storedBrowserHistoryJson = json
        prefs?.edit()?.putString("history_data", json)?.apply()
        return true
    }

    override suspend fun getBrowserSettingsJson(): String? {
        if (storedBrowserSettingsJson == null) loadFromDisk()
        return storedBrowserSettingsJson
    }

    override suspend fun saveBrowserSettingsJson(json: String): Boolean {
        storedBrowserSettingsJson = json
        prefs?.edit()?.putString("settings_data", json)?.apply()
        return true
    }

    override suspend fun getWebContentJson(): String? {
        if (storedWebContentJson == null) loadFromDisk()
        return storedWebContentJson
    }

    override suspend fun saveWebContentJson(json: String): Boolean {
        storedWebContentJson = json
        prefs?.edit()?.putString("web_content_data", json)?.apply()
        return true
    }
}

