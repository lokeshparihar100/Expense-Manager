package com.expensemanager.data.local.dao

import androidx.room.*
import com.expensemanager.data.local.entity.Transaction
import com.expensemanager.data.local.entity.TransactionType
import com.expensemanager.data.local.entity.TransactionWithTags
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Transaction entity
 */
@Dao
interface TransactionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long
    
    @Update
    suspend fun update(transaction: Transaction)
    
    @Delete
    suspend fun delete(transaction: Transaction)
    
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?
    
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE payeeId = :payeeId ORDER BY date DESC")
    fun getTransactionsByPayee(payeeId: Long): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE paymentMethodId = :paymentMethodId ORDER BY date DESC")
    fun getTransactionsByPaymentMethod(paymentMethodId: Long): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE statusId = :statusId ORDER BY date DESC")
    fun getTransactionsByStatus(statusId: Long): Flow<List<Transaction>>
    
    @Query("""
        SELECT t.*, 
            p.name as payeeName, 
            c.name as categoryName, 
            pm.name as paymentMethodName, 
            s.name as statusName
        FROM transactions t
        LEFT JOIN tags p ON t.payeeId = p.id
        LEFT JOIN tags c ON t.categoryId = c.id
        LEFT JOIN tags pm ON t.paymentMethodId = pm.id
        LEFT JOIN tags s ON t.statusId = s.id
        ORDER BY t.date DESC
    """)
    fun getAllTransactionsWithTags(): Flow<List<TransactionWithTags>>
    
    @Query("""
        SELECT t.*, 
            p.name as payeeName, 
            c.name as categoryName, 
            pm.name as paymentMethodName, 
            s.name as statusName
        FROM transactions t
        LEFT JOIN tags p ON t.payeeId = p.id
        LEFT JOIN tags c ON t.categoryId = c.id
        LEFT JOIN tags pm ON t.paymentMethodId = pm.id
        LEFT JOIN tags s ON t.statusId = s.id
        WHERE t.id = :id
    """)
    suspend fun getTransactionWithTagsById(id: Long): TransactionWithTags?
    
    @Query("""
        SELECT t.*, 
            p.name as payeeName, 
            c.name as categoryName, 
            pm.name as paymentMethodName, 
            s.name as statusName
        FROM transactions t
        LEFT JOIN tags p ON t.payeeId = p.id
        LEFT JOIN tags c ON t.categoryId = c.id
        LEFT JOIN tags pm ON t.paymentMethodId = pm.id
        LEFT JOIN tags s ON t.statusId = s.id
        WHERE t.type = :type
        ORDER BY t.date DESC
    """)
    fun getTransactionsWithTagsByType(type: TransactionType): Flow<List<TransactionWithTags>>
    
    @Query("""
        SELECT t.*, 
            p.name as payeeName, 
            c.name as categoryName, 
            pm.name as paymentMethodName, 
            s.name as statusName
        FROM transactions t
        LEFT JOIN tags p ON t.payeeId = p.id
        LEFT JOIN tags c ON t.categoryId = c.id
        LEFT JOIN tags pm ON t.paymentMethodId = pm.id
        LEFT JOIN tags s ON t.statusId = s.id
        WHERE t.date BETWEEN :startDate AND :endDate
        ORDER BY t.date DESC
    """)
    fun getTransactionsWithTagsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionWithTags>>
    
    // Summary queries
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate")
    fun getTotalExpenses(startDate: Long, endDate: Long): Flow<Double?>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND date BETWEEN :startDate AND :endDate")
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double?>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpensesAllTime(): Flow<Double?>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncomeAllTime(): Flow<Double?>
    
    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM transactions 
        WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate
        GROUP BY categoryId
    """)
    fun getExpensesByCategory(startDate: Long, endDate: Long): Flow<List<CategoryTotal>>
    
    @Query("SELECT COUNT(*) FROM transactions")
    fun getTransactionCount(): Flow<Int>
}

data class CategoryTotal(
    val categoryId: Long?,
    val total: Double
)
