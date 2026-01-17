package com.expensemanager.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.expensemanager.data.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Broadcast receiver for incoming SMS messages
 * Automatically captures and parses bank/credit card transaction SMS
 */
@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var smsParser: SmsParser
    
    @Inject
    lateinit var transactionRepository: TransactionRepository
    
    companion object {
        private const val TAG = "SmsReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }
        
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        
        for (message in messages) {
            val sender = message.displayOriginatingAddress ?: continue
            val body = message.messageBody ?: continue
            val timestamp = message.timestampMillis
            
            Log.d(TAG, "SMS received from: $sender")
            
            // Check if it's a bank SMS
            if (smsParser.isBankSms(sender)) {
                Log.d(TAG, "Bank SMS detected, parsing...")
                
                val parsed = smsParser.parseSms(body, timestamp)
                
                if (parsed != null) {
                    Log.d(TAG, "Parsed transaction: amount=${parsed.amount}, type=${parsed.type}")
                    
                    val transaction = smsParser.createTransaction(parsed)
                    
                    // Save transaction to database
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            transactionRepository.insert(transaction)
                            Log.d(TAG, "Transaction saved successfully")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to save transaction", e)
                        }
                    }
                }
            }
        }
    }
}
