package com.expensemanager.sms

import com.expensemanager.data.local.entity.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SmsParser
 */
class SmsParserTest {

    private lateinit var smsParser: SmsParser

    @Before
    fun setup() {
        smsParser = SmsParser()
    }

    // ==================== isBankSms Tests ====================

    @Test
    fun `isBankSms returns true for HDFC bank sender`() {
        assertTrue(smsParser.isBankSms("VM-HDFCBK"))
        assertTrue(smsParser.isBankSms("AD-HDFCBK"))
        assertTrue(smsParser.isBankSms("HDFCBK"))
    }

    @Test
    fun `isBankSms returns true for SBI sender`() {
        assertTrue(smsParser.isBankSms("VM-SBIINB"))
        assertTrue(smsParser.isBankSms("SBIINB"))
    }

    @Test
    fun `isBankSms returns true for ICICI sender`() {
        assertTrue(smsParser.isBankSms("VM-ICICIB"))
        assertTrue(smsParser.isBankSms("ICICIB"))
    }

    @Test
    fun `isBankSms returns true for Axis bank sender`() {
        assertTrue(smsParser.isBankSms("VM-AXISBK"))
        assertTrue(smsParser.isBankSms("AXISBK"))
    }

    @Test
    fun `isBankSms returns true for Kotak sender`() {
        assertTrue(smsParser.isBankSms("VM-KOTAKB"))
        assertTrue(smsParser.isBankSms("KOTAKB"))
    }

    @Test
    fun `isBankSms returns true for generic bank sender`() {
        assertTrue(smsParser.isBankSms("XYZ-BANK"))
        assertTrue(smsParser.isBankSms("MYBANK"))
    }

    @Test
    fun `isBankSms returns false for personal numbers`() {
        assertFalse(smsParser.isBankSms("+919876543210"))
        assertFalse(smsParser.isBankSms("9876543210"))
    }

    @Test
    fun `isBankSms returns false for non-bank senders`() {
        assertFalse(smsParser.isBankSms("VM-AMAZON"))
        assertFalse(smsParser.isBankSms("FLIPKART"))
    }

    // ==================== parseSms Amount Extraction Tests ====================

    @Test
    fun `parseSms extracts amount with Rs prefix`() {
        val sms = "Rs.1000.00 debited from your account"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(1000.0, result!!.amount, 0.01)
    }

    @Test
    fun `parseSms extracts amount with Rs dot prefix`() {
        val sms = "Rs. 500.50 debited from your account"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(500.50, result!!.amount, 0.01)
    }

    @Test
    fun `parseSms extracts amount with INR prefix`() {
        val sms = "INR 2500 debited from account"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(2500.0, result!!.amount, 0.01)
    }

    @Test
    fun `parseSms extracts amount with rupee symbol`() {
        val sms = "₹5000.00 spent at Amazon"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(5000.0, result!!.amount, 0.01)
    }

    @Test
    fun `parseSms extracts amount with commas`() {
        val sms = "Rs.1,00,000.00 credited to your account"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(100000.0, result!!.amount, 0.01)
    }

    @Test
    fun `parseSms extracts amount with comma thousands separator`() {
        val sms = "Rs.10,500.75 debited from account"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(10500.75, result!!.amount, 0.01)
    }

    @Test
    fun `parseSms returns null when no amount found`() {
        val sms = "Your account balance is now available"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNull(result)
    }

    // ==================== parseSms Transaction Type Tests ====================

    @Test
    fun `parseSms identifies expense for debited keyword`() {
        val sms = "Rs.1000 debited from account xx1234"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(TransactionType.EXPENSE, result!!.type)
    }

    @Test
    fun `parseSms identifies expense for spent keyword`() {
        val sms = "Rs.500 spent at Walmart using card"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(TransactionType.EXPENSE, result!!.type)
    }

    @Test
    fun `parseSms identifies expense for payment keyword`() {
        val sms = "Payment of Rs.750 made to merchant"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(TransactionType.EXPENSE, result!!.type)
    }

    @Test
    fun `parseSms identifies expense for withdrawn keyword`() {
        val sms = "Rs.2000 withdrawn from ATM"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(TransactionType.EXPENSE, result!!.type)
    }

    @Test
    fun `parseSms identifies income for credited keyword`() {
        val sms = "Rs.50000 credited to account xx5678"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(TransactionType.INCOME, result!!.type)
    }

    @Test
    fun `parseSms identifies income for received keyword`() {
        val sms = "Rs.1500 received from John Doe"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(TransactionType.INCOME, result!!.type)
    }

    @Test
    fun `parseSms identifies income for deposited keyword`() {
        val sms = "Rs.10000 deposited to your account"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(TransactionType.INCOME, result!!.type)
    }

    @Test
    fun `parseSms identifies income for refund keyword`() {
        val sms = "Refund of Rs.299 processed to your account"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(TransactionType.INCOME, result!!.type)
    }

    @Test
    fun `parseSms defaults to expense when no keyword found`() {
        val sms = "Rs.100 transaction completed"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(TransactionType.EXPENSE, result!!.type)
    }

    // ==================== parseSms Merchant Extraction Tests ====================

    @Test
    fun `parseSms extracts merchant with at keyword`() {
        val sms = "Rs.1000 spent at Amazon on 12/01/2024"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals("Amazon", result!!.merchant)
    }

    @Test
    fun `parseSms extracts merchant with to keyword`() {
        val sms = "Rs.500 paid to Uber via UPI"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertNotNull(result!!.merchant)
    }

    @Test
    fun `parseSms extracts long merchant name`() {
        val sms = "Rs.2500 spent at Walmart Supermarket on card"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertNotNull(result!!.merchant)
    }

    // ==================== parseSms Card Info Tests ====================

    @Test
    fun `parseSms extracts card number with xx pattern`() {
        val sms = "Rs.1000 debited from card xx1234"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals("xxxx1234", result!!.cardInfo)
    }

    @Test
    fun `parseSms extracts card number with ending pattern`() {
        val sms = "Rs.500 spent using card ending 5678"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals("xxxx5678", result!!.cardInfo)
    }

    @Test
    fun `parseSms extracts account number`() {
        val sms = "Rs.2000 debited from A/c XX9012"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals("xxxx9012", result!!.cardInfo)
    }

    // ==================== parseSms Payment Method Tests ====================

    @Test
    fun `parseSms detects UPI payment method`() {
        val sms = "Rs.100 paid via UPI to merchant"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals("UPI", result!!.paymentMethod)
    }

    @Test
    fun `parseSms detects IMPS payment method as UPI`() {
        val sms = "Rs.500 transferred via IMPS"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals("UPI", result!!.paymentMethod)
    }

    @Test
    fun `parseSms detects credit card payment method`() {
        val sms = "Rs.1000 spent on Credit Card xx1234"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertTrue(result!!.paymentMethod.contains("Credit Card"))
    }

    @Test
    fun `parseSms detects Visa credit card`() {
        val sms = "Rs.2000 charged on VISA Credit Card"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals("Visa Credit Card", result!!.paymentMethod)
    }

    @Test
    fun `parseSms detects Master credit card`() {
        val sms = "Rs.1500 spent using Mastercard Credit Card"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals("Master Credit Card", result!!.paymentMethod)
    }

    @Test
    fun `parseSms detects debit card payment method`() {
        val sms = "Rs.500 debited from Debit Card xx5678"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals("Debit Card", result!!.paymentMethod)
    }

    @Test
    fun `parseSms detects net banking payment method`() {
        val sms = "Rs.10000 transferred via Net Banking"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals("Net Banking", result!!.paymentMethod)
    }

    // ==================== createTransaction Tests ====================

    @Test
    fun `createTransaction creates correct transaction entity`() {
        val timestamp = System.currentTimeMillis()
        val parsed = ParsedTransaction(
            amount = 1000.0,
            type = TransactionType.EXPENSE,
            merchant = "Amazon",
            cardInfo = "xxxx1234",
            paymentMethod = "Credit Card",
            originalSms = "Rs.1000 spent at Amazon",
            timestamp = timestamp
        )

        val transaction = smsParser.createTransaction(parsed)

        assertEquals(1000.0, transaction.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertTrue(transaction.description.contains("Amazon"))
        assertEquals(timestamp, transaction.date)
        assertTrue(transaction.isFromSms)
        assertEquals("Rs.1000 spent at Amazon", transaction.smsBody)
    }

    @Test
    fun `createTransaction uses default description when merchant is null`() {
        val parsed = ParsedTransaction(
            amount = 500.0,
            type = TransactionType.EXPENSE,
            merchant = null,
            cardInfo = null,
            paymentMethod = "UPI",
            originalSms = "Rs.500 debited",
            timestamp = System.currentTimeMillis()
        )

        val transaction = smsParser.createTransaction(parsed)

        assertEquals("Expense", transaction.description)
    }

    @Test
    fun `createTransaction includes card info in description`() {
        val parsed = ParsedTransaction(
            amount = 750.0,
            type = TransactionType.EXPENSE,
            merchant = "Walmart",
            cardInfo = "xxxx5678",
            paymentMethod = "Credit Card",
            originalSms = "Rs.750 spent",
            timestamp = System.currentTimeMillis()
        )

        val transaction = smsParser.createTransaction(parsed)

        assertTrue(transaction.description.contains("Walmart"))
        assertTrue(transaction.description.contains("xxxx5678"))
    }

    @Test
    fun `createTransaction sets income description correctly`() {
        val parsed = ParsedTransaction(
            amount = 50000.0,
            type = TransactionType.INCOME,
            merchant = null,
            cardInfo = null,
            paymentMethod = "Other",
            originalSms = "Rs.50000 credited",
            timestamp = System.currentTimeMillis()
        )

        val transaction = smsParser.createTransaction(parsed)

        assertEquals("Income", transaction.description)
        assertEquals(TransactionType.INCOME, transaction.type)
    }

    // ==================== Edge Cases Tests ====================

    @Test
    fun `parseSms handles empty string`() {
        val result = smsParser.parseSms("", System.currentTimeMillis())
        assertNull(result)
    }

    @Test
    fun `parseSms handles whitespace only string`() {
        val result = smsParser.parseSms("   ", System.currentTimeMillis())
        assertNull(result)
    }

    @Test
    fun `parseSms handles SMS with only non-amount text`() {
        val result = smsParser.parseSms("Your OTP is 123456", System.currentTimeMillis())
        assertNull(result)
    }

    @Test
    fun `parseSms handles mixed case keywords`() {
        val sms = "RS.1000 DEBITED from account"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(1000.0, result!!.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, result.type)
    }

    @Test
    fun `parseSms handles real HDFC SMS format`() {
        val sms = "Rs.1,234.56 debited from A/c **1234 on 15-Jan-24 to VPA merchant@upi (UPI Ref No 123456789012)"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(1234.56, result!!.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals("UPI", result.paymentMethod)
    }

    @Test
    fun `parseSms handles real SBI SMS format`() {
        val sms = "Your A/c X1234 is credited with Rs.25,000.00 on 20Jan24 by NEFT-HDFC123456"
        val result = smsParser.parseSms(sms, System.currentTimeMillis())

        assertNotNull(result)
        assertEquals(25000.0, result!!.amount, 0.01)
        assertEquals(TransactionType.INCOME, result.type)
    }

    @Test
    fun `parseSms preserves timestamp`() {
        val timestamp = 1704067200000L // 2024-01-01 00:00:00 UTC
        val sms = "Rs.100 debited"
        val result = smsParser.parseSms(sms, timestamp)

        assertNotNull(result)
        assertEquals(timestamp, result!!.timestamp)
    }
}
