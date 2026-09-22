package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 AND timestamp >= :startEpoch AND timestamp <= :endEpoch ORDER BY timestamp DESC")
    fun getTransactionsBetween(startEpoch: Long, endEpoch: Long): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE isDeleted = 0")
    suspend fun getTransactionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 AND cardId = :cardId ORDER BY timestamp DESC")
    fun getTransactionsByCard(cardId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 AND installmentGroupId = :groupId ORDER BY installmentNumber ASC")
    fun getInstallmentsByGroup(groupId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 AND installmentGroupId = :groupId ORDER BY installmentNumber ASC")
    suspend fun getInstallmentsByGroupSync(groupId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 AND isInstallment = 1 ORDER BY timestamp DESC")
    fun getAllInstallmentTransactions(): Flow<List<TransactionEntity>>

    @Query("DELETE FROM transactions WHERE installmentGroupId = :groupId")
    suspend fun deleteInstallmentGroup(groupId: String)

    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :updatedAt WHERE installmentGroupId = :groupId")
    suspend fun markInstallmentGroupDeleted(groupId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM transactions WHERE syncUuid = :syncUuid LIMIT 1")
    suspend fun getBySyncUuid(syncUuid: String): TransactionEntity?

    @Query("SELECT * FROM transactions")
    suspend fun getAllRawTransactions(): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("UPDATE transactions SET category = :newCategory WHERE category = :oldCategory AND type = :type AND isDeleted = 0")
    suspend fun updateCategoryName(oldCategory: String, newCategory: String, type: String)
}

