package com.expensemanager.data.repository

import com.expensemanager.data.local.dao.TagDao
import com.expensemanager.data.local.entity.Tag
import com.expensemanager.data.local.entity.TagType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing Tag data
 */
@Singleton
class TagRepository @Inject constructor(
    private val tagDao: TagDao
) {
    
    suspend fun insert(tag: Tag): Long {
        return tagDao.insert(tag)
    }
    
    suspend fun insertAll(tags: List<Tag>) {
        tagDao.insertAll(tags)
    }
    
    suspend fun update(tag: Tag) {
        tagDao.update(tag.copy(updatedAt = System.currentTimeMillis()))
    }
    
    suspend fun delete(tag: Tag) {
        tagDao.delete(tag)
    }
    
    suspend fun deleteById(id: Long) {
        tagDao.deleteById(id)
    }
    
    suspend fun getById(id: Long): Tag? {
        return tagDao.getById(id)
    }
    
    fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAllTags()
    }
    
    fun getTagsByType(type: TagType): Flow<List<Tag>> {
        return tagDao.getTagsByType(type)
    }
    
    suspend fun getTagsByTypeSync(type: TagType): List<Tag> {
        return tagDao.getTagsByTypeSync(type)
    }
    
    fun getPayees(): Flow<List<Tag>> {
        return tagDao.getPayees()
    }
    
    fun getCategories(): Flow<List<Tag>> {
        return tagDao.getCategories()
    }
    
    fun getPaymentMethods(): Flow<List<Tag>> {
        return tagDao.getPaymentMethods()
    }
    
    fun getStatuses(): Flow<List<Tag>> {
        return tagDao.getStatuses()
    }
    
    fun searchTagsByType(query: String, type: TagType): Flow<List<Tag>> {
        return tagDao.searchTagsByType(query, type)
    }
    
    suspend fun getTagCountByType(type: TagType): Int {
        return tagDao.getTagCountByType(type)
    }
    
    suspend fun getDefaultTagsByType(type: TagType): List<Tag> {
        return tagDao.getDefaultTagsByType(type)
    }
}
