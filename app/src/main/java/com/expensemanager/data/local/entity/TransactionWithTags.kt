package com.expensemanager.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Data class representing a Transaction with all its associated Tags
 * Used for displaying complete transaction details
 */
data class TransactionWithTags(
    @Embedded
    val transaction: Transaction,
    
    val payeeName: String? = null,
    val categoryName: String? = null,
    val paymentMethodName: String? = null,
    val statusName: String? = null
)
