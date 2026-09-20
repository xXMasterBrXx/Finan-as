package com.example.data.repository

import com.example.data.local.CustomCategoryDao
import com.example.data.local.CustomCategoryEntity
import com.example.data.model.Categories
import com.example.data.model.CategoryIconHelper
import com.example.data.model.CategoryItem
import com.example.data.model.TransactionType
import com.example.data.p2p.P2PSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CategoryRepository(
    private val customCategoryDao: CustomCategoryDao,
    var p2pSyncManager: P2PSyncManager? = null
) {

    val allCustomCategories: Flow<List<CategoryItem>> = customCategoryDao.getAllCategories()
        .map { entities ->
            entities.map { it.toCategoryItem() }
        }

    fun getCategoriesByType(type: TransactionType): Flow<List<CategoryItem>> {
        return customCategoryDao.getCategoriesByType(type.name)
            .map { entities ->
                entities.map { it.toCategoryItem() }
            }
    }

    suspend fun addCategory(
        name: String,
        type: TransactionType,
        iconName: String,
        colorHex: String
    ): Long = withContext(Dispatchers.IO) {
        val entity = CustomCategoryEntity(
            name = name.trim(),
            type = type.name,
            iconName = iconName,
            colorHex = colorHex,
            isDefault = false
        )
        val id = customCategoryDao.insertCategory(entity)
        p2pSyncManager?.broadcastCategoryUpsert(entity.copy(id = id))
        id
    }

    suspend fun updateCategory(
        id: Long,
        name: String,
        type: TransactionType,
        iconName: String,
        colorHex: String
    ) = withContext(Dispatchers.IO) {
        val existing = customCategoryDao.getCategoryById(id)
        val entity = CustomCategoryEntity(
            id = id,
            name = name.trim(),
            type = type.name,
            iconName = iconName,
            colorHex = colorHex,
            isDefault = false,
            syncUuid = existing?.syncUuid ?: java.util.UUID.randomUUID().toString(),
            updatedAt = System.currentTimeMillis()
        )
        customCategoryDao.updateCategory(entity)
        p2pSyncManager?.broadcastCategoryUpsert(entity)
    }

    suspend fun deleteCategory(id: Long) = withContext(Dispatchers.IO) {
        val existing = customCategoryDao.getCategoryById(id)
        if (existing != null) {
            val tombstone = existing.copy(isDeleted = true, updatedAt = System.currentTimeMillis())
            customCategoryDao.updateCategory(tombstone)
            p2pSyncManager?.broadcastCategoryDelete(existing.syncUuid)
        }
    }

    suspend fun deleteAllCustomCategories() = withContext(Dispatchers.IO) {
        customCategoryDao.deleteCustomCategories()
    }

    private fun CustomCategoryEntity.toCategoryItem(): CategoryItem {
        val parsedType = try {
            TransactionType.valueOf(type)
        } catch (e: Exception) {
            TransactionType.EXPENSE
        }
        val icon = CategoryIconHelper.getIcon(iconName)
        val color = CategoryIconHelper.parseColor(colorHex)
        return CategoryItem(
            id = "custom_$id",
            name = name,
            icon = icon,
            color = color,
            type = parsedType,
            iconName = iconName,
            colorHex = colorHex,
            isCustom = true,
            dbId = id
        )
    }
}

