package com.expensemanager.sms

import com.expensemanager.data.local.entity.Transaction
import com.expensemanager.data.local.entity.TransactionType
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for parsing bank/credit card SMS messages
 * to extract transaction information
 */
@Singleton
class SmsParser @Inject constructor() {
    
    companion object {
        // Common bank SMS patterns
        private val AMOUNT_PATTERNS = listOf(
            Pattern.compile("(?:Rs\\.?|INR|₹)\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("([\\d,]+\\.?\\d*)\\s*(?:Rs\\.?|INR|₹)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:amount|amt)[:\\s]*(?:Rs\\.?|INR|₹)?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
        )
        
        private val DEBIT_KEYWORDS = listOf(
            "debited", "debit", "spent", "paid", "payment", "purchase", 
            "withdrawn", "withdrawal", "transferred", "sent", "charged"
        )
        
        private val CREDIT_KEYWORDS = listOf(
            "credited", "credit", "received", "deposited", "deposit",
            "refund", "cashback", "reversed"
        )
        
        private val MERCHANT_PATTERNS = listOf(
            Pattern.compile("(?:at|to|from|@)\\s+([A-Za-z0-9\\s&]+?)(?:\\s+on|\\s+dated|\\s+ref|\\.|$)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:merchant|payee|beneficiary)[:\\s]+([A-Za-z0-9\\s&]+?)(?:\\s+on|\\s+ref|\\.|$)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("to\\s+([A-Za-z][A-Za-z0-9\\s&]{2,20}?)\\s+(?:via|UPI|ref)", Pattern.CASE_INSENSITIVE)
        )
        
        private val CARD_PATTERNS = listOf(
            Pattern.compile("(?:card|a/c|account)\\s*(?:ending|no\\.?|xx+)\\s*(\\d{4})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("xx+(\\d{4})", Pattern.CASE_INSENSITIVE)
        )
        
        private val UPI_PATTERN = Pattern.compile("(?:UPI|IMPS|NEFT|RTGS)", Pattern.CASE_INSENSITIVE)
        
        private val BANK_SENDERS = listOf(
            "HDFCBK", "SBIINB", "ICICIB", "AXISBK", "KOTAKB", "PNBSMS",
            "BOIIND", "UNIONB", "CANARAB", "IABORB", "SCISMS", "YESBK",
            "INDUSB", "RBLBNK", "FEDBNK", "AMEXIN", "PAYTMB", "GLOSEL"
        )
    }
    
    /**
     * Check if the SMS is from a bank or financial institution
     */
    fun isBankSms(sender: String): Boolean {
        val upperSender = sender.uppercase()
        return BANK_SENDERS.any { upperSender.contains(it) } ||
               upperSender.contains("BANK") ||
               upperSender.matches(Regex(".*[A-Z]{2}\\d{6}.*")) // Format like VM-HDFCBK
    }
    
    /**
     * Parse SMS body to extract transaction details
     */
    fun parseSms(smsBody: String, timestamp: Long): ParsedTransaction? {
        val body = smsBody.trim()
        
        // Extract amount
        val amount = extractAmount(body) ?: return null
        
        // Determine transaction type
        val type = determineTransactionType(body)
        
        // Extract merchant/payee
        val merchant = extractMerchant(body)
        
        // Extract card/account info
        val cardInfo = extractCardInfo(body)
        
        // Detect payment method
        val paymentMethod = detectPaymentMethod(body, cardInfo)
        
        return ParsedTransaction(
            amount = amount,
            type = type,
            merchant = merchant,
            cardInfo = cardInfo,
            paymentMethod = paymentMethod,
            originalSms = body,
            timestamp = timestamp
        )
    }
    
    private fun extractAmount(body: String): Double? {
        for (pattern in AMOUNT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val amountStr = matcher.group(1)?.replace(",", "") ?: continue
                return try {
                    amountStr.toDouble()
                } catch (e: NumberFormatException) {
                    null
                }
            }
        }
        return null
    }
    
    private fun determineTransactionType(body: String): TransactionType {
        val lowerBody = body.lowercase()
        
        val debitScore = DEBIT_KEYWORDS.count { lowerBody.contains(it) }
        val creditScore = CREDIT_KEYWORDS.count { lowerBody.contains(it) }
        
        return if (creditScore > debitScore) TransactionType.INCOME else TransactionType.EXPENSE
    }
    
    private fun extractMerchant(body: String): String? {
        for (pattern in MERCHANT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val merchant = matcher.group(1)?.trim()
                if (!merchant.isNullOrBlank() && merchant.length >= 2) {
                    return merchant.take(50) // Limit length
                }
            }
        }
        return null
    }
    
    private fun extractCardInfo(body: String): String? {
        for (pattern in CARD_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                return "xxxx${matcher.group(1)}"
            }
        }
        return null
    }
    
    private fun detectPaymentMethod(body: String, cardInfo: String?): String {
        val upperBody = body.uppercase()
        
        return when {
            UPI_PATTERN.matcher(body).find() -> "UPI"
            upperBody.contains("CREDIT CARD") || upperBody.contains("CC ") -> {
                when {
                    upperBody.contains("VISA") -> "Visa Credit Card"
                    upperBody.contains("MASTER") -> "Master Credit Card"
                    else -> "Credit Card"
                }
            }
            upperBody.contains("DEBIT CARD") || upperBody.contains("DC ") -> "Debit Card"
            upperBody.contains("NET BANKING") || upperBody.contains("NETBANKING") -> "Net Banking"
            upperBody.contains("WALLET") -> "Wallet"
            cardInfo != null -> "Card"
            else -> "Other"
        }
    }
    
    /**
     * Create a Transaction entity from parsed SMS data
     */
    fun createTransaction(parsed: ParsedTransaction): Transaction {
        val description = buildString {
            if (parsed.merchant != null) {
                append(parsed.merchant)
            } else {
                append(if (parsed.type == TransactionType.EXPENSE) "Expense" else "Income")
            }
            if (parsed.cardInfo != null) {
                append(" (${parsed.cardInfo})")
            }
        }
        
        return Transaction(
            amount = parsed.amount,
            description = description,
            date = parsed.timestamp,
            type = parsed.type,
            isFromSms = true,
            smsBody = parsed.originalSms
        )
    }
}

/**
 * Data class representing parsed SMS transaction
 */
data class ParsedTransaction(
    val amount: Double,
    val type: TransactionType,
    val merchant: String?,
    val cardInfo: String?,
    val paymentMethod: String,
    val originalSms: String,
    val timestamp: Long
)
