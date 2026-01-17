package com.expensemanager.sms

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.expensemanager.data.local.entity.Transaction
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for reading SMS messages from the device
 */
@Singleton
class SmsReader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val smsParser: SmsParser
) {
    
    companion object {
        private const val TAG = "SmsReader"
        private val SMS_URI = Uri.parse("content://sms/inbox")
    }
    
    /**
     * Read and parse bank SMS messages from the device
     * @param daysBack Number of days to look back (default 30)
     * @return List of parsed transactions
     */
    fun readBankSms(daysBack: Int = 30): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        
        val cutoffTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                SMS_URI,
                arrayOf("_id", "address", "body", "date"),
                "date > ?",
                arrayOf(cutoffTime.toString()),
                "date DESC"
            )
            
            cursor?.let {
                val addressIndex = it.getColumnIndex("address")
                val bodyIndex = it.getColumnIndex("body")
                val dateIndex = it.getColumnIndex("date")
                
                while (it.moveToNext()) {
                    val sender = it.getString(addressIndex) ?: continue
                    val body = it.getString(bodyIndex) ?: continue
                    val timestamp = it.getLong(dateIndex)
                    
                    if (smsParser.isBankSms(sender)) {
                        val parsed = smsParser.parseSms(body, timestamp)
                        if (parsed != null) {
                            transactions.add(smsParser.createTransaction(parsed))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading SMS", e)
        } finally {
            cursor?.close()
        }
        
        return transactions
    }
    
    /**
     * Get total count of bank SMS messages
     */
    fun getBankSmsCount(daysBack: Int = 30): Int {
        var count = 0
        val cutoffTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                SMS_URI,
                arrayOf("_id", "address"),
                "date > ?",
                arrayOf(cutoffTime.toString()),
                null
            )
            
            cursor?.let {
                val addressIndex = it.getColumnIndex("address")
                
                while (it.moveToNext()) {
                    val sender = it.getString(addressIndex) ?: continue
                    if (smsParser.isBankSms(sender)) {
                        count++
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error counting SMS", e)
        } finally {
            cursor?.close()
        }
        
        return count
    }
}
