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
}

class MemorySaveManager(
    private val saveDao: SaveDao? = null
) : SaveManager {

    private var storedProfile: UserProfile? = null
    private val storedTerminalHistory = mutableListOf<String>()

    private val _currentSave = MutableStateFlow<SaveSlotEntity?>(
        SaveSlotEntity(
            slotId = 1,
            saveName = "AbyssOS Default Profile",
            timestamp = System.currentTimeMillis(),
            playerHandle = "operator",
            osVersion = "0.1 Alpha"
        )
    )
    override val currentSaveState: Flow<SaveSlotEntity?> = _currentSave.asStateFlow()

    override suspend fun saveGame(slotId: Int, saveName: String): Boolean {
        val newSave = SaveSlotEntity(
            slotId = slotId,
            saveName = saveName,
            timestamp = System.currentTimeMillis(),
            playTimeSeconds = (_currentSave.value?.playTimeSeconds ?: 0L) + 60L,
            playerHandle = storedProfile?.username ?: "operator",
            osVersion = "0.1 Alpha"
        )
        _currentSave.value = newSave
        saveDao?.insertSaveSlot(newSave)
        return true
    }

    override suspend fun loadGame(slotId: Int): SaveSlotEntity? {
        val loaded = saveDao?.getSaveSlotById(slotId) ?: _currentSave.value
        if (loaded != null) {
            _currentSave.value = loaded
        }
        return loaded
    }

    override suspend fun resetGameData(): Boolean {
        storedProfile = null
        storedTerminalHistory.clear()
        val resetSave = SaveSlotEntity(
            slotId = 1,
            saveName = "New Operator Session",
            timestamp = System.currentTimeMillis(),
            playTimeSeconds = 0L,
            playerHandle = "operator",
            osVersion = "0.1 Alpha"
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
}

