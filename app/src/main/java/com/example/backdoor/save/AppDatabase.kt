package com.example.backdoor.save

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "save_slots")
data class SaveSlotEntity(
    @PrimaryKey val slotId: Int = 1,
    val saveName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val playTimeSeconds: Long = 0L,
    val playerHandle: String = "operator",
    val osVersion: String = "0.0.1",
    val themeName: String = "matrix_green",
    val vfsDataJson: String = ""
)

@Dao
interface SaveDao {
    @Query("SELECT * FROM save_slots ORDER BY slotId ASC")
    fun getAllSaveSlots(): Flow<List<SaveSlotEntity>>

    @Query("SELECT * FROM save_slots WHERE slotId = :slotId LIMIT 1")
    suspend fun getSaveSlotById(slotId: Int): SaveSlotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaveSlot(saveSlot: SaveSlotEntity)

    @Query("DELETE FROM save_slots WHERE slotId = :slotId")
    suspend fun deleteSaveSlot(slotId: Int)
}

@Database(entities = [SaveSlotEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun saveDao(): SaveDao
}
