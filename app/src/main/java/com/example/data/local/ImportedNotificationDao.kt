package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportedNotificationDao {
    @Query("SELECT * FROM imported_bank_notifications ORDER BY timestamp DESC")
    fun getAllImportedNotifications(): Flow<List<ImportedNotificationEntity>>

    @Query("SELECT * FROM imported_bank_notifications WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingNotifications(): Flow<List<ImportedNotificationEntity>>

    @Query("SELECT COUNT(*) FROM imported_bank_notifications WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT * FROM imported_bank_notifications WHERE id = :id")
    suspend fun getById(id: Long): ImportedNotificationEntity?

    @Query("SELECT * FROM imported_bank_notifications WHERE amount = :amount AND merchant = :merchant AND timestamp >= :minTimestamp LIMIT 1")
    suspend fun findRecentDuplicate(amount: Double, merchant: String, minTimestamp: Long): ImportedNotificationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ImportedNotificationEntity): Long

    @Update
    suspend fun update(entity: ImportedNotificationEntity)

    @Query("UPDATE imported_bank_notifications SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Delete
    suspend fun delete(entity: ImportedNotificationEntity)

    @Query("DELETE FROM imported_bank_notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM imported_bank_notifications")
    suspend fun clearAll()
}
