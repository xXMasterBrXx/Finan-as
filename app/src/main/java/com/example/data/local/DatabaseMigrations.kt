package com.example.data.local

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    private const val TAG = "DatabaseMigrations"

    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            // Direct migrations to Version 9 from any previous version
            createMigration(1, 9),
            createMigration(2, 9),
            createMigration(3, 9),
            createMigration(4, 9),
            createMigration(5, 9),
            createMigration(6, 9),
            createMigration(7, 9),
            createMigration(8, 9),

            // Stepwise migrations
            createMigration(1, 2),
            createMigration(2, 3),
            createMigration(3, 4),
            createMigration(4, 5),
            createMigration(5, 6),
            createMigration(6, 7),
            createMigration(7, 8),
            createMigration(8, 9)
        )
    }

    private fun createMigration(from: Int, to: Int): Migration {
        return object : Migration(from, to) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Log.i(TAG, "Migrating database from version $from to $to without data loss...")
                safeMigrateToVersion9(db)
                Log.i(TAG, "Database migration from $from to $to completed successfully.")
            }
        }
    }

    fun safeMigrateToVersion9(db: SupportSQLiteDatabase) {
        // 1. Transactions Table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `transactions` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `type` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `note` TEXT NOT NULL DEFAULT '',
                `cardId` INTEGER,
                `isInstallment` INTEGER NOT NULL DEFAULT 0,
                `installmentNumber` INTEGER NOT NULL DEFAULT 1,
                `totalInstallments` INTEGER NOT NULL DEFAULT 1,
                `installmentGroupId` TEXT,
                `isAnticipated` INTEGER NOT NULL DEFAULT 0,
                `isRecurring` INTEGER NOT NULL DEFAULT 0,
                `recurringGroupId` TEXT,
                `syncUuid` TEXT NOT NULL DEFAULT '',
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `isDeleted` INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        ensureColumnExists(db, "transactions", "note", "TEXT NOT NULL DEFAULT ''")
        ensureColumnExists(db, "transactions", "cardId", "INTEGER DEFAULT NULL")
        ensureColumnExists(db, "transactions", "isInstallment", "INTEGER NOT NULL DEFAULT 0")
        ensureColumnExists(db, "transactions", "installmentNumber", "INTEGER NOT NULL DEFAULT 1")
        ensureColumnExists(db, "transactions", "totalInstallments", "INTEGER NOT NULL DEFAULT 1")
        ensureColumnExists(db, "transactions", "installmentGroupId", "TEXT DEFAULT NULL")
        ensureColumnExists(db, "transactions", "isAnticipated", "INTEGER NOT NULL DEFAULT 0")
        ensureColumnExists(db, "transactions", "isRecurring", "INTEGER NOT NULL DEFAULT 0")
        ensureColumnExists(db, "transactions", "recurringGroupId", "TEXT DEFAULT NULL")
        ensureColumnExists(db, "transactions", "syncUuid", "TEXT NOT NULL DEFAULT ''")
        ensureColumnExists(db, "transactions", "updatedAt", "INTEGER NOT NULL DEFAULT 0")
        ensureColumnExists(db, "transactions", "isDeleted", "INTEGER NOT NULL DEFAULT 0")

        // 2. Credit Cards Table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `credit_cards` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `lastFourDigits` TEXT NOT NULL DEFAULT '',
                `colorHex` TEXT NOT NULL DEFAULT '#8A05BE',
                `limitAmount` REAL NOT NULL DEFAULT 0.0,
                `closingDay` INTEGER NOT NULL DEFAULT 10,
                `dueDay` INTEGER NOT NULL DEFAULT 17,
                `syncUuid` TEXT NOT NULL DEFAULT '',
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `isDeleted` INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        ensureColumnExists(db, "credit_cards", "lastFourDigits", "TEXT NOT NULL DEFAULT ''")
        ensureColumnExists(db, "credit_cards", "colorHex", "TEXT NOT NULL DEFAULT '#8A05BE'")
        ensureColumnExists(db, "credit_cards", "limitAmount", "REAL NOT NULL DEFAULT 0.0")
        ensureColumnExists(db, "credit_cards", "closingDay", "INTEGER NOT NULL DEFAULT 10")
        ensureColumnExists(db, "credit_cards", "dueDay", "INTEGER NOT NULL DEFAULT 17")
        ensureColumnExists(db, "credit_cards", "syncUuid", "TEXT NOT NULL DEFAULT ''")
        ensureColumnExists(db, "credit_cards", "updatedAt", "INTEGER NOT NULL DEFAULT 0")
        ensureColumnExists(db, "credit_cards", "isDeleted", "INTEGER NOT NULL DEFAULT 0")

        // 3. Custom Categories Table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `custom_categories` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `iconName` TEXT NOT NULL DEFAULT 'category',
                `colorHex` TEXT NOT NULL DEFAULT '#42A5F5',
                `isDefault` INTEGER NOT NULL DEFAULT 0,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `syncUuid` TEXT NOT NULL DEFAULT '',
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `isDeleted` INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        ensureColumnExists(db, "custom_categories", "iconName", "TEXT NOT NULL DEFAULT 'category'")
        ensureColumnExists(db, "custom_categories", "colorHex", "TEXT NOT NULL DEFAULT '#42A5F5'")
        ensureColumnExists(db, "custom_categories", "isDefault", "INTEGER NOT NULL DEFAULT 0")
        ensureColumnExists(db, "custom_categories", "createdAt", "INTEGER NOT NULL DEFAULT 0")
        ensureColumnExists(db, "custom_categories", "syncUuid", "TEXT NOT NULL DEFAULT ''")
        ensureColumnExists(db, "custom_categories", "updatedAt", "INTEGER NOT NULL DEFAULT 0")
        ensureColumnExists(db, "custom_categories", "isDeleted", "INTEGER NOT NULL DEFAULT 0")

        // 4. Notifications Table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `notifications` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `message` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `isRead` INTEGER NOT NULL DEFAULT 0,
                `referenceId` INTEGER DEFAULT NULL,
                `actionRoute` TEXT DEFAULT NULL,
                `severity` TEXT NOT NULL DEFAULT 'INFO',
                `createdAt` INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        // 5. Imported Bank Notifications Table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `imported_bank_notifications` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `packageName` TEXT NOT NULL,
                `bankName` TEXT NOT NULL,
                `rawTitle` TEXT NOT NULL,
                `rawText` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `type` TEXT NOT NULL,
                `merchant` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `cardLastFourDigits` TEXT DEFAULT NULL,
                `matchedCardId` INTEGER DEFAULT NULL,
                `timestamp` INTEGER NOT NULL,
                `status` TEXT NOT NULL DEFAULT 'PENDING',
                `importedTransactionId` INTEGER DEFAULT NULL
            )
            """.trimIndent()
        )

        // Ensure default syncUuid values for legacy data
        try {
            db.execSQL("UPDATE `transactions` SET syncUuid = hex(randomblob(16)) WHERE syncUuid IS NULL OR syncUuid = ''")
            db.execSQL("UPDATE `credit_cards` SET syncUuid = hex(randomblob(16)) WHERE syncUuid IS NULL OR syncUuid = ''")
            db.execSQL("UPDATE `custom_categories` SET syncUuid = hex(randomblob(16)) WHERE syncUuid IS NULL OR syncUuid = ''")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update legacy syncUuid: ${e.message}")
        }
    }

    private fun ensureColumnExists(
        db: SupportSQLiteDatabase,
        tableName: String,
        columnName: String,
        columnDefinition: String
    ) {
        var columnExists = false
        try {
            db.query("PRAGMA table_info(`$tableName`)").use { cursor ->
                val nameIdx = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (nameIdx != -1 && cursor.getString(nameIdx).equals(columnName, ignoreCase = true)) {
                        columnExists = true
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking column $columnName in $tableName: ${e.message}")
        }

        if (!columnExists) {
            try {
                db.execSQL("ALTER TABLE `$tableName` ADD COLUMN `$columnName` $columnDefinition")
                Log.i(TAG, "Added missing column $columnName to $tableName")
            } catch (e: Exception) {
                Log.w(TAG, "Could not add column $columnName to $tableName: ${e.message}")
            }
        }
    }
}
