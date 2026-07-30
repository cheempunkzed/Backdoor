package com.example.backdoor.save

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SaveManager {
    val currentSaveState: Flow<SaveSlotEntity?>
    suspend fun saveGame(slotId: Int = 1, saveName: String = "AutoSave"): Boolean
    suspend fun loadGame(slotId: Int = 1): SaveSlotEntity?
    suspend fun resetGameData(): Boolean

    suspend fun getUserProfile(): UserProfile?
    suspend fun saveUserProfile(profile: UserProfile): Boolean

    suspend fun getTerminalHistory(): List<String>
    suspend fun saveTerminalHistory(history: List<String>): Boolean

    suspend fun getVfsDataJson(): String?
    suspend fun saveVfsDataJson(json: String): Boolean
}

class MemorySaveManager(
    private val saveDao: SaveDao? = null
) : SaveManager {

    private var storedProfile: UserProfile? = null
    private val storedTerminalHistory = mutableListOf<String>()
    private var storedVfsJson: String? = null

    private val _currentSave = MutableStateFlow<SaveSlotEntity?>(
        SaveSlotEntity(
            slotId = 1,
            saveName = "AbyssOS Default Profile",
            timestamp = System.currentTimeMillis(),
            playerHandle = "operator",
            osVersion = "0.2.0"
        )
    )
    override val currentSaveState: Flow<SaveSlotEntity?> = _currentSave.asStateFlow()

    override suspend fun saveGame(slotId: Int, saveName: String): Boolean {
        val currentSlot = _currentSave.value
        val newSave = SaveSlotEntity(
            slotId = slotId,
            saveName = saveName,
            timestamp = System.currentTimeMillis(),
            playTimeSeconds = (currentSlot?.playTimeSeconds ?: 0L) + 60L,
            playerHandle = storedProfile?.username ?: currentSlot?.playerHandle ?: "operator",
            osVersion = "0.2.0",
            vfsDataJson = storedVfsJson ?: currentSlot?.vfsDataJson ?: ""
        )
        _currentSave.value = newSave
        saveDao?.insertSaveSlot(newSave)
        return true
    }

    override suspend fun loadGame(slotId: Int): SaveSlotEntity? {
        val loaded = saveDao?.getSaveSlotById(slotId) ?: _currentSave.value
        if (loaded != null) {
            _currentSave.value = loaded
            if (loaded.vfsDataJson.isNotEmpty()) {
                storedVfsJson = loaded.vfsDataJson
            }
        }
        return loaded
    }

    override suspend fun resetGameData(): Boolean {
        storedProfile = null
        storedTerminalHistory.clear()
        storedVfsJson = null
        val resetSave = SaveSlotEntity(
            slotId = 1,
            saveName = "New Operator Session",
            timestamp = System.currentTimeMillis(),
            playTimeSeconds = 0L,
            playerHandle = "operator",
            osVersion = "0.2.0"
        )
        _currentSave.value = resetSave
        saveDao?.deleteSaveSlot(1)
        saveDao?.insertSaveSlot(resetSave)
        return true
    }

    override suspend fun getUserProfile(): UserProfile? {
        return storedProfile
    }

    override suspend fun saveUserProfile(profile: UserProfile): Boolean {
        storedProfile = profile
        _currentSave.value = _currentSave.value?.copy(playerHandle = profile.username)
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
        return storedVfsJson ?: _currentSave.value?.vfsDataJson
    }

    override suspend fun saveVfsDataJson(json: String): Boolean {
        storedVfsJson = json
        _currentSave.value = _currentSave.value?.copy(vfsDataJson = json)
        return true
    }
}

