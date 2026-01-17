package com.expensemanager.data.repository

import com.expensemanager.data.local.dao.CategoryTotal
import com.expensemanager.data.local.dao.TransactionDao
import com.expensemanager.data.local.entity.Transaction
import com.expensemanager.data.local.entity.TransactionType
import com.expensemanager.data.local.entity.TransactionWithTags
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing Transaction data
 */
@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    
    suspend fun insert(transaction: Transaction): Long {
        return transactionDao.insert(transaction)
    }
    
    suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction.copy(updatedAt = System.currentTimeMillis()))
    }
    
    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }
    
    suspend fun deleteById(id: Long) {
        transactionDao.deleteById(id)
    }
    
    suspend fun getById(id: Long): Transaction? {
        return transactionDao.getById(id)
    }
    
    suspend fun getTransactionWithTagsById(id: Long): TransactionWithTags? {
        return transactionDao.getTransactionWithTagsById(id)
    }
    
    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }
    
    fun getAllTransactionsWithTags(): Flow<List<TransactionWithTags>> {
        return transactionDao.getAllTransactionsWithTags()
    }
    
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }
    
    fun getTransactionsWithTagsByType(type: TransactionType): Flow<List<TransactionWithTags>> {
        return transactionDao.getTransactionsWithTagsByType(type)
    }
    
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }
    
    fun getTransactionsWithTagsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionWithTags>> {
        return transactionDao.getTransactionsWithTagsByDateRange(startDate, endDate)
    }
    
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(categoryId)
    }
    
    fun getTransactionsByPayee(payeeId: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByPayee(payeeId)
    }
    
    fun getTransactionsByPaymentMethod(paymentMethodId: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByPaymentMethod(paymentMethodId)
    }
    
    fun getTransactionsByStatus(statusId: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByStatus(statusId)
    }
    
    fun getTotalExpenses(startDate: Long, endDate: Long): Flow<Double?> {
        return transactionDao.getTotalExpenses(startDate, endDate)
    }
    
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double?> {
        return transactionDao.getTotalIncome(startDate, endDate)
    }
    
    fun getTotalExpensesAllTime(): Flow<Double?> {
        return transactionDao.getTotalExpensesAllTime()
    }
    
    fun getTotalIncomeAllTime(): Flow<Double?> {
        return transactionDao.getTotalIncomeAllTime()
    }
    
    fun getExpensesByCategory(startDate: Long, endDate: Long): Flow<List<CategoryTotal>> {
        return transactionDao.getExpensesByCategory(startDate, endDate)
    }
    
    fun getTransactionCount(): Flow<Int> {
        return transactionDao.getTransactionCount()
    }
}
