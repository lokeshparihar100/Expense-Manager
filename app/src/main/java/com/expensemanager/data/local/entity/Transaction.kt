package com.expensemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a financial transaction (expense or income)
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val date: Long, // Unix timestamp
    val type: TransactionType, // EXPENSE or INCOME
    val payeeId: Long? = null,
    val categoryId: Long? = null,
    val paymentMethodId: Long? = null,
    val statusId: Long? = null,
    val isFromSms: Boolean = false,
    val smsBody: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class TransactionType {
    EXPENSE,
    INCOME
}
