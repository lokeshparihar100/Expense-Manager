package com.expensemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a tag that can be applied to transactions
 * Tags are categorized by type: PAYEE, CATEGORY, PAYMENT_METHOD, STATUS
 */
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TagType,
    val color: String? = null, // Hex color code
    val icon: String? = null, // Icon name/resource
    val isDefault: Boolean = false, // Pre-defined tags
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class TagType {
    PAYEE,      // Shopkeeper, Mart, Amazon, Uber, etc.
    CATEGORY,   // Shopping, Food, Healthcare, Insurance, Loan, etc.
    PAYMENT_METHOD, // Cash, Visa credit card, Master credit card, UPI, etc.
    STATUS      // Done, Pending, InFuture
}
