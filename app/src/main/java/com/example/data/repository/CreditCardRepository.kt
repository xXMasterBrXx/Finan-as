package com.example.data.repository

import com.example.data.local.CreditCardDao
import com.example.data.local.CreditCardEntity
import com.example.data.p2p.P2PSyncManager
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CreditCardRepository(
    private val creditCardDao: CreditCardDao,
    private val userPreferences: UserPreferences,
    var p2pSyncManager: P2PSyncManager? = null
) {

    val allCards: Flow<List<CreditCardEntity>> = creditCardDao.getAllCards()

    suspend fun getCardById(id: Long): CreditCardEntity? = withContext(Dispatchers.IO) {
        creditCardDao.getCardById(id)
    }

    suspend fun insert(card: CreditCardEntity): Long = withContext(Dispatchers.IO) {
        val id = creditCardDao.insertCard(card)
        val inserted = card.copy(id = id)
        p2pSyncManager?.broadcastCardUpsert(inserted)
        id
    }

    suspend fun update(card: CreditCardEntity) = withContext(Dispatchers.IO) {
        val updated = card.copy(updatedAt = System.currentTimeMillis())
        creditCardDao.updateCard(updated)
        p2pSyncManager?.broadcastCardUpsert(updated)
    }

    suspend fun delete(card: CreditCardEntity) = withContext(Dispatchers.IO) {
        val tombstone = card.copy(isDeleted = true, updatedAt = System.currentTimeMillis())
        creditCardDao.updateCard(tombstone)
        p2pSyncManager?.broadcastCardDelete(card.syncUuid)
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        creditCardDao.deleteAllCards()
    }

    suspend fun seedInitialCardsIfEmpty(): List<Long> = withContext(Dispatchers.IO) {
        if (!userPreferences.isInitialDataSeeded()) {
            val sampleCards = listOf(
                CreditCardEntity(
                    name = "Nubank Roxo",
                    lastFourDigits = "4321",
                    colorHex = "#8A05BE",
                    limitAmount = 4500.0,
                    closingDay = 5,
                    dueDay = 12
                ),
                CreditCardEntity(
                    name = "Inter Black",
                    lastFourDigits = "9876",
                    colorHex = "#FF7A00",
                    limitAmount = 7000.0,
                    closingDay = 20,
                    dueDay = 27
                )
            )
            sampleCards.map {
                val id = creditCardDao.insertCard(it)
                p2pSyncManager?.broadcastCardUpsert(it.copy(id = id))
                id
            }
        } else {
            emptyList()
        }
    }
}

