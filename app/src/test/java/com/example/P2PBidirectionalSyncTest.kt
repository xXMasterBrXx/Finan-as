package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.CreditCardEntity
import com.example.data.local.CustomCategoryEntity
import com.example.data.local.TransactionEntity
import com.example.data.p2p.P2PJsonCodec
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class P2PBidirectionalSyncTest {

    private lateinit var dbHost: AppDatabase
    private lateinit var dbClient: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dbHost = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dbClient = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        dbHost.close()
        dbClient.close()
    }

    @Test
    fun testP2PJsonCodecSyncResponseWithInvoicesAndRequiresResponse() {
        val tx = TransactionEntity(
            title = "Supermercado",
            amount = 120.50,
            type = "EXPENSE",
            category = "Alimentação",
            timestamp = 1000L,
            syncUuid = "tx-uuid-1",
            updatedAt = 2000L
        )
        val card = CreditCardEntity(
            name = "Nubank",
            syncUuid = "card-uuid-1",
            updatedAt = 2000L
        )
        val cat = CustomCategoryEntity(
            name = "Assinaturas",
            type = "EXPENSE",
            syncUuid = "cat-uuid-1",
            updatedAt = 2000L
        )

        val json = P2PJsonCodec.createSyncResponse(
            senderId = "dev-1",
            transactions = listOf(tx),
            cards = listOf(card),
            categories = listOf(cat),
            cardSyncUuidMap = mapOf(1L to "card-uuid-1"),
            paidInvoices = setOf("card-uuid-1_2026_9"),
            unpaidInvoices = setOf("card-uuid-1_2026_10"),
            requiresResponse = true
        )

        assertEquals("SYNC_RESP", json.getString("type"))
        assertTrue(json.getBoolean("requiresResponse"))
        val paid = P2PJsonCodec.extractInvoices(json, "paidInvoices")
        val unpaid = P2PJsonCodec.extractInvoices(json, "unpaidInvoices")
        assertTrue(paid.contains("card-uuid-1_2026_9"))
        assertTrue(unpaid.contains("card-uuid-1_2026_10"))
    }

    @Test
    fun testBidirectionalSyncPreservesOfflineChangesOnBothSides() = runBlocking {
        // 1. Initial State: Host has an initial transaction T1
        val initialTxUuid = UUID.randomUUID().toString()
        val t1Host = TransactionEntity(
            title = "Jantar",
            amount = 100.0,
            type = "EXPENSE",
            category = "Restaurante",
            timestamp = 10000L,
            syncUuid = initialTxUuid,
            updatedAt = 10000L
        )
        val t1Id = dbHost.transactionDao().insert(t1Host)

        // Initial pairing sync: Client receives T1
        dbClient.transactionDao().insert(t1Host.copy(id = 0))

        // 2. Both disconnect. Both make adjustments outside the connection!
        // Client modifies T1 offline (e.g. adjusts amount to 150.0, with newer updatedAt)
        val clientT1 = dbClient.transactionDao().getBySyncUuid(initialTxUuid)!!
        val updatedClientT1 = clientT1.copy(
            amount = 150.0,
            note = "Adicionado gorjeta",
            updatedAt = 20000L // newer than host's 10000L
        )
        dbClient.transactionDao().update(updatedClientT1)

        // Client adds a brand new transaction T_Client offline
        val clientTxUuid = UUID.randomUUID().toString()
        val txClientOffline = TransactionEntity(
            title = "Café da tarde",
            amount = 18.0,
            type = "EXPENSE",
            category = "Lanche",
            timestamp = 21000L,
            syncUuid = clientTxUuid,
            updatedAt = 21000L
        )
        dbClient.transactionDao().insert(txClientOffline)

        // Client adds a new Credit Card offline
        val cardSyncUuid = UUID.randomUUID().toString()
        val cardClientOffline = CreditCardEntity(
            name = "Inter Black",
            syncUuid = cardSyncUuid,
            updatedAt = 21000L
        )
        dbClient.creditCardDao().insertCard(cardClientOffline)

        // Host adds a brand new transaction T_Host offline
        val hostTxUuid = UUID.randomUUID().toString()
        val txHostOffline = TransactionEntity(
            title = "Salário Extra",
            amount = 500.0,
            type = "INCOME",
            category = "Trabalho",
            timestamp = 22000L,
            syncUuid = hostTxUuid,
            updatedAt = 22000L
        )
        dbHost.transactionDao().insert(txHostOffline)

        // 3. RECONNECTION: Bidirectional sync occurs between Host and Client!
        // Step A: Host sends its raw data to Client. Client reconciles Host's items.
        val hostRawCards = dbHost.creditCardDao().getAllRawCards()
        val hostRawTx = dbHost.transactionDao().getAllRawTransactions()
        reconcileCards(dbClient, hostRawCards)
        reconcileTransactions(dbClient, hostRawTx)

        // Step B: Client sends its reconciled state to Host. Host reconciles Client's items.
        val clientRawCards = dbClient.creditCardDao().getAllRawCards()
        val clientRawTx = dbClient.transactionDao().getAllRawTransactions()
        reconcileCards(dbHost, clientRawCards)
        reconcileTransactions(dbHost, clientRawTx)

        // 4. VERIFY: Both Host and Client have preserved and merged all changes!
        val hostTransactions = dbHost.transactionDao().getAllRawTransactions().filter { !it.isDeleted }
        val clientTransactions = dbClient.transactionDao().getAllRawTransactions().filter { !it.isDeleted }

        assertEquals(3, hostTransactions.size)
        assertEquals(3, clientTransactions.size)

        // Verify T1 has the Client's offline adjustment (150.0) on BOTH devices
        val finalT1Host = dbHost.transactionDao().getBySyncUuid(initialTxUuid)
        val finalT1Client = dbClient.transactionDao().getBySyncUuid(initialTxUuid)
        assertNotNull(finalT1Host)
        assertNotNull(finalT1Client)
        assertEquals(150.0, finalT1Host!!.amount, 0.001)
        assertEquals(150.0, finalT1Client!!.amount, 0.001)
        assertEquals("Adicionado gorjeta", finalT1Host.note)

        // Verify T_Client is on BOTH devices
        assertNotNull(dbHost.transactionDao().getBySyncUuid(clientTxUuid))
        assertNotNull(dbClient.transactionDao().getBySyncUuid(clientTxUuid))

        // Verify T_Host is on BOTH devices
        assertNotNull(dbHost.transactionDao().getBySyncUuid(hostTxUuid))
        assertNotNull(dbClient.transactionDao().getBySyncUuid(hostTxUuid))

        // Verify Card created on Client is on BOTH devices
        assertNotNull(dbHost.creditCardDao().getBySyncUuid(cardSyncUuid))
        assertNotNull(dbClient.creditCardDao().getBySyncUuid(cardSyncUuid))
    }

    private suspend fun reconcileCards(db: AppDatabase, incomingList: List<CreditCardEntity>) {
        incomingList.forEach { incoming ->
            val existing = db.creditCardDao().getBySyncUuid(incoming.syncUuid)
            if (existing != null) {
                if (incoming.updatedAt >= existing.updatedAt) {
                    db.creditCardDao().updateCard(incoming.copy(id = existing.id))
                }
            } else {
                db.creditCardDao().insertCard(incoming.copy(id = 0))
            }
        }
    }

    private suspend fun reconcileTransactions(db: AppDatabase, incomingList: List<TransactionEntity>) {
        incomingList.forEach { incoming ->
            val existing = db.transactionDao().getBySyncUuid(incoming.syncUuid)
            if (existing != null) {
                if (incoming.updatedAt >= existing.updatedAt) {
                    db.transactionDao().update(incoming.copy(id = existing.id))
                }
            } else {
                db.transactionDao().insert(incoming.copy(id = 0))
            }
        }
    }
}
