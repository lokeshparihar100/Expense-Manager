package com.expensemanager.data.local

import androidx.room.TypeConverter
import com.expensemanager.data.local.entity.TagType
import com.expensemanager.data.local.entity.TransactionType

/**
 * Type converters for Room database
 * Converts enum types to/from strings for database storage
 */
class Converters {
    
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }
    
    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
    
    @TypeConverter
    fun fromTagType(type: TagType): String {
        return type.name
    }
    
    @TypeConverter
    fun toTagType(value: String): TagType {
        return TagType.valueOf(value)
    }
}
