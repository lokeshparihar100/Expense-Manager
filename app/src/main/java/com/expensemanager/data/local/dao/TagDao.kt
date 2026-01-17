package com.expensemanager.data.local.dao

import androidx.room.*
import com.expensemanager.data.local.entity.Tag
import com.expensemanager.data.local.entity.TagType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Tag entity
 */
@Dao
interface TagDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: Tag): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<Tag>)
    
    @Update
    suspend fun update(tag: Tag)
    
    @Delete
    suspend fun delete(tag: Tag)
    
    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: Long): Tag?
    
    @Query("SELECT * FROM tags ORDER BY type, name")
    fun getAllTags(): Flow<List<Tag>>
    
    @Query("SELECT * FROM tags WHERE type = :type ORDER BY name")
    fun getTagsByType(type: TagType): Flow<List<Tag>>
    
    @Query("SELECT * FROM tags WHERE type = :type ORDER BY name")
    suspend fun getTagsByTypeSync(type: TagType): List<Tag>
    
    @Query("SELECT * FROM tags WHERE type = 'PAYEE' ORDER BY name")
    fun getPayees(): Flow<List<Tag>>
    
    @Query("SELECT * FROM tags WHERE type = 'CATEGORY' ORDER BY name")
    fun getCategories(): Flow<List<Tag>>
    
    @Query("SELECT * FROM tags WHERE type = 'PAYMENT_METHOD' ORDER BY name")
    fun getPaymentMethods(): Flow<List<Tag>>
    
    @Query("SELECT * FROM tags WHERE type = 'STATUS' ORDER BY name")
    fun getStatuses(): Flow<List<Tag>>
    
    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' AND type = :type ORDER BY name")
    fun searchTagsByType(query: String, type: TagType): Flow<List<Tag>>
    
    @Query("SELECT COUNT(*) FROM tags WHERE type = :type")
    suspend fun getTagCountByType(type: TagType): Int
    
    @Query("SELECT * FROM tags WHERE isDefault = 1 AND type = :type")
    suspend fun getDefaultTagsByType(type: TagType): List<Tag>
}
