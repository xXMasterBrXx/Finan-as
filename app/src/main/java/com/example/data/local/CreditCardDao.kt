package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {

    @Query("SELECT * FROM credit_cards WHERE isDeleted = 0 ORDER BY id ASC")
    fun getAllCards(): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getCardById(id: Long): CreditCardEntity?

    @Query("SELECT * FROM credit_cards WHERE syncUuid = :syncUuid LIMIT 1")
    suspend fun getBySyncUuid(syncUuid: String): CreditCardEntity?

    @Query("SELECT * FROM credit_cards")
    suspend fun getAllRawCards(): List<CreditCardEntity>

    @Query("SELECT COUNT(*) FROM credit_cards WHERE isDeleted = 0")
    suspend fun getCardCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CreditCardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<CreditCardEntity>): List<Long>

    @Update
    suspend fun updateCard(card: CreditCardEntity)

    @Delete
    suspend fun deleteCard(card: CreditCardEntity)

    @Query("DELETE FROM credit_cards")
    suspend fun deleteAllCards()
}

