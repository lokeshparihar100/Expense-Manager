package com.expensemanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.expensemanager.data.local.dao.TagDao
import com.expensemanager.data.local.dao.TransactionDao
import com.expensemanager.data.local.entity.Tag
import com.expensemanager.data.local.entity.TagType
import com.expensemanager.data.local.entity.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room Database for the Expense Manager app
 */
@Database(
    entities = [Transaction::class, Tag::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun transactionDao(): TransactionDao
    abstract fun tagDao(): TagDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_manager_db"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.tagDao())
                }
            }
        }
        
        suspend fun populateDatabase(tagDao: TagDao) {
            // Pre-populate with default tags
            val defaultTags = listOf(
                // Payees
                Tag(name = "Shopkeeper", type = TagType.PAYEE, isDefault = true),
                Tag(name = "Mart", type = TagType.PAYEE, isDefault = true),
                Tag(name = "Amazon", type = TagType.PAYEE, isDefault = true),
                Tag(name = "Uber", type = TagType.PAYEE, isDefault = true),
                Tag(name = "Other", type = TagType.PAYEE, isDefault = true),
                
                // Categories
                Tag(name = "Shopping", type = TagType.CATEGORY, color = "#FF5722", isDefault = true),
                Tag(name = "Food", type = TagType.CATEGORY, color = "#4CAF50", isDefault = true),
                Tag(name = "Healthcare", type = TagType.CATEGORY, color = "#E91E63", isDefault = true),
                Tag(name = "Insurance", type = TagType.CATEGORY, color = "#9C27B0", isDefault = true),
                Tag(name = "Loan", type = TagType.CATEGORY, color = "#F44336", isDefault = true),
                Tag(name = "Transportation", type = TagType.CATEGORY, color = "#2196F3", isDefault = true),
                Tag(name = "Entertainment", type = TagType.CATEGORY, color = "#FF9800", isDefault = true),
                Tag(name = "Utilities", type = TagType.CATEGORY, color = "#607D8B", isDefault = true),
                Tag(name = "Salary", type = TagType.CATEGORY, color = "#8BC34A", isDefault = true),
                Tag(name = "Investment", type = TagType.CATEGORY, color = "#00BCD4", isDefault = true),
                Tag(name = "Other", type = TagType.CATEGORY, color = "#795548", isDefault = true),
                
                // Payment Methods
                Tag(name = "Cash", type = TagType.PAYMENT_METHOD, isDefault = true),
                Tag(name = "Visa Credit Card", type = TagType.PAYMENT_METHOD, isDefault = true),
                Tag(name = "Master Credit Card", type = TagType.PAYMENT_METHOD, isDefault = true),
                Tag(name = "UPI", type = TagType.PAYMENT_METHOD, isDefault = true),
                Tag(name = "Debit Card", type = TagType.PAYMENT_METHOD, isDefault = true),
                Tag(name = "Net Banking", type = TagType.PAYMENT_METHOD, isDefault = true),
                Tag(name = "Other", type = TagType.PAYMENT_METHOD, isDefault = true),
                
                // Statuses
                Tag(name = "Done", type = TagType.STATUS, color = "#4CAF50", isDefault = true),
                Tag(name = "Pending", type = TagType.STATUS, color = "#FF9800", isDefault = true),
                Tag(name = "InFuture", type = TagType.STATUS, color = "#2196F3", isDefault = true)
            )
            
            tagDao.insertAll(defaultTags)
        }
    }
}
