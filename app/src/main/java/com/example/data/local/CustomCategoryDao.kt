package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCategoryDao {

    @Query("SELECT * FROM custom_categories WHERE isDeleted = 0 ORDER BY id ASC")
    fun getAllCategories(): Flow<List<CustomCategoryEntity>>

    @Query("SELECT * FROM custom_categories WHERE isDeleted = 0 AND type = :type ORDER BY id ASC")
    fun getCategoriesByType(type: String): Flow<List<CustomCategoryEntity>>

    @Query("SELECT * FROM custom_categories WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getCategoryById(id: Long): CustomCategoryEntity?

    @Query("SELECT * FROM custom_categories WHERE syncUuid = :syncUuid LIMIT 1")
    suspend fun getBySyncUuid(syncUuid: String): CustomCategoryEntity?

    @Query("SELECT * FROM custom_categories")
    suspend fun getAllRawCategories(): List<CustomCategoryEntity>

    @Query("SELECT COUNT(*) FROM custom_categories WHERE isDeleted = 0")
    suspend fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CustomCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CustomCategoryEntity>): List<Long>

    @Update
    suspend fun updateCategory(category: CustomCategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CustomCategoryEntity)

    @Query("DELETE FROM custom_categories WHERE isDefault = 0")
    suspend fun deleteCustomCategories()

    @Query("DELETE FROM custom_categories")
    suspend fun deleteAllCategories()
}

