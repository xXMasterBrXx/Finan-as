package com.example.data.p2p

import com.example.data.local.CreditCardEntity
import com.example.data.local.CustomCategoryEntity
import com.example.data.local.TransactionEntity
import org.json.JSONArray
import org.json.JSONObject

object P2PJsonCodec {

    fun transactionToJson(tx: TransactionEntity): JSONObject {
        return JSONObject().apply {
            put("syncUuid", tx.syncUuid)
            put("title", tx.title)
            put("amount", tx.amount)
            put("type", tx.type)
            put("category", tx.category)
            put("timestamp", tx.timestamp)
            put("note", tx.note)
            put("cardId", tx.cardId ?: JSONObject.NULL)
            put("isInstallment", tx.isInstallment)
            put("installmentNumber", tx.installmentNumber)
            put("totalInstallments", tx.totalInstallments)
            put("installmentGroupId", tx.installmentGroupId ?: JSONObject.NULL)
            put("isAnticipated", tx.isAnticipated)
            put("isRecurring", tx.isRecurring)
            put("recurringGroupId", tx.recurringGroupId ?: JSONObject.NULL)
            put("updatedAt", tx.updatedAt)
            put("isDeleted", tx.isDeleted)
        }
    }

    fun jsonToTransaction(json: JSONObject): TransactionEntity {
        return TransactionEntity(
            syncUuid = json.optString("syncUuid", java.util.UUID.randomUUID().toString()),
            title = json.optString("title", ""),
            amount = json.optDouble("amount", 0.0),
            type = json.optString("type", "EXPENSE"),
            category = json.optString("category", "Outros"),
            timestamp = json.optLong("timestamp", System.currentTimeMillis()),
            note = json.optString("note", ""),
            cardId = if (json.isNull("cardId")) null else json.optLong("cardId"),
            isInstallment = json.optBoolean("isInstallment", false),
            installmentNumber = json.optInt("installmentNumber", 1),
            totalInstallments = json.optInt("totalInstallments", 1),
            installmentGroupId = if (json.isNull("installmentGroupId")) null else json.optString("installmentGroupId"),
            isAnticipated = json.optBoolean("isAnticipated", false),
            isRecurring = json.optBoolean("isRecurring", false),
            recurringGroupId = if (json.isNull("recurringGroupId")) null else json.optString("recurringGroupId"),
            updatedAt = json.optLong("updatedAt", System.currentTimeMillis()),
            isDeleted = json.optBoolean("isDeleted", false)
        )
    }

    fun cardToJson(card: CreditCardEntity): JSONObject {
        return JSONObject().apply {
            put("syncUuid", card.syncUuid)
            put("name", card.name)
            put("lastFourDigits", card.lastFourDigits)
            put("colorHex", card.colorHex)
            put("limitAmount", card.limitAmount)
            put("closingDay", card.closingDay)
            put("dueDay", card.dueDay)
            put("updatedAt", card.updatedAt)
            put("isDeleted", card.isDeleted)
        }
    }

    fun jsonToCard(json: JSONObject): CreditCardEntity {
        return CreditCardEntity(
            syncUuid = json.optString("syncUuid", java.util.UUID.randomUUID().toString()),
            name = json.optString("name", "Cartão"),
            lastFourDigits = json.optString("lastFourDigits", ""),
            colorHex = json.optString("colorHex", "#8A05BE"),
            limitAmount = json.optDouble("limitAmount", 0.0),
            closingDay = json.optInt("closingDay", 10),
            dueDay = json.optInt("dueDay", 17),
            updatedAt = json.optLong("updatedAt", System.currentTimeMillis()),
            isDeleted = json.optBoolean("isDeleted", false)
        )
    }

    fun categoryToJson(cat: CustomCategoryEntity): JSONObject {
        return JSONObject().apply {
            put("syncUuid", cat.syncUuid)
            put("name", cat.name)
            put("type", cat.type)
            put("iconName", cat.iconName)
            put("colorHex", cat.colorHex)
            put("isDefault", cat.isDefault)
            put("createdAt", cat.createdAt)
            put("updatedAt", cat.updatedAt)
            put("isDeleted", cat.isDeleted)
        }
    }

    fun jsonToCategory(json: JSONObject): CustomCategoryEntity {
        return CustomCategoryEntity(
            syncUuid = json.optString("syncUuid", java.util.UUID.randomUUID().toString()),
            name = json.optString("name", "Categoria"),
            type = json.optString("type", "EXPENSE"),
            iconName = json.optString("iconName", "category"),
            colorHex = json.optString("colorHex", "#42A5F5"),
            isDefault = json.optBoolean("isDefault", false),
            createdAt = json.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = json.optLong("updatedAt", System.currentTimeMillis()),
            isDeleted = json.optBoolean("isDeleted", false)
        )
    }

    // Packet creation
    fun createHandshake(senderId: String, senderName: String, keyHash: String): JSONObject {
        return JSONObject().apply {
            put("type", "HANDSHAKE")
            put("senderId", senderId)
            put("senderName", senderName)
            put("keyHash", keyHash)
            put("timestamp", System.currentTimeMillis())
        }
    }

    fun createHandshakeAck(senderId: String, senderName: String, status: String): JSONObject {
        return JSONObject().apply {
            put("type", "HANDSHAKE_ACK")
            put("senderId", senderId)
            put("senderName", senderName)
            put("status", status)
            put("timestamp", System.currentTimeMillis())
        }
    }

    fun createSyncRequest(senderId: String): JSONObject {
        return JSONObject().apply {
            put("type", "SYNC_REQ")
            put("senderId", senderId)
            put("timestamp", System.currentTimeMillis())
        }
    }

    fun createSyncResponse(
        senderId: String,
        transactions: List<TransactionEntity>,
        cards: List<CreditCardEntity>,
        categories: List<CustomCategoryEntity>
    ): JSONObject {
        val txArray = JSONArray()
        transactions.forEach { txArray.put(transactionToJson(it)) }

        val cardsArray = JSONArray()
        cards.forEach { cardsArray.put(cardToJson(it)) }

        val catArray = JSONArray()
        categories.forEach { catArray.put(categoryToJson(it)) }

        return JSONObject().apply {
            put("type", "SYNC_RESP")
            put("senderId", senderId)
            put("transactions", txArray)
            put("cards", cardsArray)
            put("categories", catArray)
            put("timestamp", System.currentTimeMillis())
        }
    }

    fun createTxUpsert(senderId: String, tx: TransactionEntity): JSONObject {
        return JSONObject().apply {
            put("type", "TX_UPSERT")
            put("senderId", senderId)
            put("transaction", transactionToJson(tx))
            put("timestamp", System.currentTimeMillis())
        }
    }

    fun createTxDelete(senderId: String, syncUuid: String, groupId: String? = null): JSONObject {
        return JSONObject().apply {
            put("type", "TX_DELETE")
            put("senderId", senderId)
            put("syncUuid", syncUuid)
            put("groupId", groupId ?: JSONObject.NULL)
            put("timestamp", System.currentTimeMillis())
        }
    }

    fun createCardUpsert(senderId: String, card: CreditCardEntity): JSONObject {
        return JSONObject().apply {
            put("type", "CARD_UPSERT")
            put("senderId", senderId)
            put("card", cardToJson(card))
            put("timestamp", System.currentTimeMillis())
        }
    }

    fun createCardDelete(senderId: String, syncUuid: String): JSONObject {
        return JSONObject().apply {
            put("type", "CARD_DELETE")
            put("senderId", senderId)
            put("syncUuid", syncUuid)
            put("timestamp", System.currentTimeMillis())
        }
    }

    fun createCategoryUpsert(senderId: String, cat: CustomCategoryEntity): JSONObject {
        return JSONObject().apply {
            put("type", "CAT_UPSERT")
            put("senderId", senderId)
            put("category", categoryToJson(cat))
            put("timestamp", System.currentTimeMillis())
        }
    }

    fun createCategoryDelete(senderId: String, syncUuid: String): JSONObject {
        return JSONObject().apply {
            put("type", "CAT_DELETE")
            put("senderId", senderId)
            put("syncUuid", syncUuid)
            put("timestamp", System.currentTimeMillis())
        }
    }

    fun createClearAll(senderId: String): JSONObject {
        return JSONObject().apply {
            put("type", "CLEAR_ALL")
            put("senderId", senderId)
            put("timestamp", System.currentTimeMillis())
        }
    }
}
