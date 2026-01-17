package com.expensemanager.data.repository

import app.cash.turbine.test
import com.expensemanager.data.local.dao.CategoryTotal
import com.expensemanager.data.local.dao.TransactionDao
import com.expensemanager.data.local.entity.Transaction
import com.expensemanager.data.local.entity.TransactionType
import com.expensemanager.data.local.entity.TransactionWithTags
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TransactionRepository
 */
class TransactionRepositoryTest {

    private lateinit var transactionDao: TransactionDao
    private lateinit var repository: TransactionRepository

    private val testTransaction = Transaction(
        id = 1L,
        amount = 100.0,
        description = "Test Transaction",
        date = System.currentTimeMillis(),
        type = TransactionType.EXPENSE,
        payeeId = 1L,
        categoryId = 2L,
        paymentMethodId = 3L,
        statusId = 4L
    )

    private val testTransactionWithTags = TransactionWithTags(
        transaction = testTransaction,
        payeeName = "Amazon",
        categoryName = "Shopping",
        paymentMethodName = "Credit Card",
        statusName = "Done"
    )

    @Before
    fun setup() {
        transactionDao = mockk()
        repository = TransactionRepository(transactionDao)
    }

    // ==================== Insert Tests ====================

    @Test
    fun `insert calls dao insert and returns id`() = runTest {
        coEvery { transactionDao.insert(any()) } returns 1L

        val result = repository.insert(testTransaction)

        assertEquals(1L, result)
        coVerify { transactionDao.insert(testTransaction) }
    }

    @Test
    fun `insert handles multiple transactions`() = runTest {
        coEvery { transactionDao.insert(any()) } returnsMany listOf(1L, 2L, 3L)

        val result1 = repository.insert(testTransaction)
        val result2 = repository.insert(testTransaction.copy(id = 0))
        val result3 = repository.insert(testTransaction.copy(id = 0))

        assertEquals(1L, result1)
        assertEquals(2L, result2)
        assertEquals(3L, result3)
    }

    // ==================== Update Tests ====================

    @Test
    fun `update calls dao update with updated timestamp`() = runTest {
        val transactionSlot = slot<Transaction>()
        coEvery { transactionDao.update(capture(transactionSlot)) } just Runs

        repository.update(testTransaction)

        coVerify { transactionDao.update(any()) }
        assertNotNull(transactionSlot.captured.updatedAt)
    }

    @Test
    fun `update preserves original data except timestamp`() = runTest {
        val transactionSlot = slot<Transaction>()
        coEvery { transactionDao.update(capture(transactionSlot)) } just Runs

        repository.update(testTransaction)

        val captured = transactionSlot.captured
        assertEquals(testTransaction.id, captured.id)
        assertEquals(testTransaction.amount, captured.amount, 0.01)
        assertEquals(testTransaction.description, captured.description)
    }

    // ==================== Delete Tests ====================

    @Test
    fun `delete calls dao delete`() = runTest {
        coEvery { transactionDao.delete(any()) } just Runs

        repository.delete(testTransaction)

        coVerify { transactionDao.delete(testTransaction) }
    }

    @Test
    fun `deleteById calls dao deleteById`() = runTest {
        coEvery { transactionDao.deleteById(any()) } just Runs

        repository.deleteById(1L)

        coVerify { transactionDao.deleteById(1L) }
    }

    // ==================== GetById Tests ====================

    @Test
    fun `getById returns transaction when found`() = runTest {
        coEvery { transactionDao.getById(1L) } returns testTransaction

        val result = repository.getById(1L)

        assertNotNull(result)
        assertEquals(testTransaction.id, result?.id)
        assertEquals(testTransaction.amount, result?.amount ?: 0.0, 0.01)
    }

    @Test
    fun `getById returns null when not found`() = runTest {
        coEvery { transactionDao.getById(999L) } returns null

        val result = repository.getById(999L)

        assertNull(result)
    }

    @Test
    fun `getTransactionWithTagsById returns transaction with tags`() = runTest {
        coEvery { transactionDao.getTransactionWithTagsById(1L) } returns testTransactionWithTags

        val result = repository.getTransactionWithTagsById(1L)

        assertNotNull(result)
        assertEquals("Amazon", result?.payeeName)
        assertEquals("Shopping", result?.categoryName)
    }

    // ==================== GetAll Tests ====================

    @Test
    fun `getAllTransactions returns flow of transactions`() = runTest {
        val transactions = listOf(testTransaction, testTransaction.copy(id = 2L))
        every { transactionDao.getAllTransactions() } returns flowOf(transactions)

        repository.getAllTransactions().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            awaitComplete()
        }
    }

    @Test
    fun `getAllTransactionsWithTags returns flow of transactions with tags`() = runTest {
        val transactionsWithTags = listOf(testTransactionWithTags)
        every { transactionDao.getAllTransactionsWithTags() } returns flowOf(transactionsWithTags)

        repository.getAllTransactionsWithTags().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Amazon", result[0].payeeName)
            awaitComplete()
        }
    }

    // ==================== Filter Tests ====================

    @Test
    fun `getTransactionsByType filters by expense`() = runTest {
        val expenses = listOf(testTransaction)
        every { transactionDao.getTransactionsByType(TransactionType.EXPENSE) } returns flowOf(expenses)

        repository.getTransactionsByType(TransactionType.EXPENSE).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(TransactionType.EXPENSE, result[0].type)
            awaitComplete()
        }
    }

    @Test
    fun `getTransactionsByType filters by income`() = runTest {
        val income = testTransaction.copy(type = TransactionType.INCOME)
        every { transactionDao.getTransactionsByType(TransactionType.INCOME) } returns flowOf(listOf(income))

        repository.getTransactionsByType(TransactionType.INCOME).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(TransactionType.INCOME, result[0].type)
            awaitComplete()
        }
    }

    @Test
    fun `getTransactionsByDateRange filters by date`() = runTest {
        val startDate = 1704067200000L
        val endDate = 1704153600000L
        val transactions = listOf(testTransaction)
        every { transactionDao.getTransactionsByDateRange(startDate, endDate) } returns flowOf(transactions)

        repository.getTransactionsByDateRange(startDate, endDate).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            awaitComplete()
        }
    }

    @Test
    fun `getTransactionsByCategory filters by category`() = runTest {
        every { transactionDao.getTransactionsByCategory(2L) } returns flowOf(listOf(testTransaction))

        repository.getTransactionsByCategory(2L).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(2L, result[0].categoryId)
            awaitComplete()
        }
    }

    @Test
    fun `getTransactionsByPayee filters by payee`() = runTest {
        every { transactionDao.getTransactionsByPayee(1L) } returns flowOf(listOf(testTransaction))

        repository.getTransactionsByPayee(1L).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(1L, result[0].payeeId)
            awaitComplete()
        }
    }

    @Test
    fun `getTransactionsByPaymentMethod filters by payment method`() = runTest {
        every { transactionDao.getTransactionsByPaymentMethod(3L) } returns flowOf(listOf(testTransaction))

        repository.getTransactionsByPaymentMethod(3L).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(3L, result[0].paymentMethodId)
            awaitComplete()
        }
    }

    @Test
    fun `getTransactionsByStatus filters by status`() = runTest {
        every { transactionDao.getTransactionsByStatus(4L) } returns flowOf(listOf(testTransaction))

        repository.getTransactionsByStatus(4L).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(4L, result[0].statusId)
            awaitComplete()
        }
    }

    // ==================== Summary Tests ====================

    @Test
    fun `getTotalExpenses returns sum of expenses`() = runTest {
        val startDate = 1704067200000L
        val endDate = 1704153600000L
        every { transactionDao.getTotalExpenses(startDate, endDate) } returns flowOf(5000.0)

        repository.getTotalExpenses(startDate, endDate).test {
            val result = awaitItem()
            assertEquals(5000.0, result ?: 0.0, 0.01)
            awaitComplete()
        }
    }

    @Test
    fun `getTotalIncome returns sum of income`() = runTest {
        val startDate = 1704067200000L
        val endDate = 1704153600000L
        every { transactionDao.getTotalIncome(startDate, endDate) } returns flowOf(10000.0)

        repository.getTotalIncome(startDate, endDate).test {
            val result = awaitItem()
            assertEquals(10000.0, result ?: 0.0, 0.01)
            awaitComplete()
        }
    }

    @Test
    fun `getTotalExpensesAllTime returns all time expenses`() = runTest {
        every { transactionDao.getTotalExpensesAllTime() } returns flowOf(50000.0)

        repository.getTotalExpensesAllTime().test {
            val result = awaitItem()
            assertEquals(50000.0, result ?: 0.0, 0.01)
            awaitComplete()
        }
    }

    @Test
    fun `getTotalIncomeAllTime returns all time income`() = runTest {
        every { transactionDao.getTotalIncomeAllTime() } returns flowOf(100000.0)

        repository.getTotalIncomeAllTime().test {
            val result = awaitItem()
            assertEquals(100000.0, result ?: 0.0, 0.01)
            awaitComplete()
        }
    }

    @Test
    fun `getExpensesByCategory returns category totals`() = runTest {
        val categoryTotals = listOf(
            CategoryTotal(1L, 1000.0),
            CategoryTotal(2L, 2000.0)
        )
        val startDate = 1704067200000L
        val endDate = 1704153600000L
        every { transactionDao.getExpensesByCategory(startDate, endDate) } returns flowOf(categoryTotals)

        repository.getExpensesByCategory(startDate, endDate).test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals(1000.0, result[0].total, 0.01)
            assertEquals(2000.0, result[1].total, 0.01)
            awaitComplete()
        }
    }

    @Test
    fun `getTransactionCount returns count`() = runTest {
        every { transactionDao.getTransactionCount() } returns flowOf(42)

        repository.getTransactionCount().test {
            val result = awaitItem()
            assertEquals(42, result)
            awaitComplete()
        }
    }

    // ==================== Edge Cases ====================

    @Test
    fun `getAllTransactions returns empty list when no transactions`() = runTest {
        every { transactionDao.getAllTransactions() } returns flowOf(emptyList())

        repository.getAllTransactions().test {
            val result = awaitItem()
            assertEquals(0, result.size)
            awaitComplete()
        }
    }

    @Test
    fun `getTotalExpenses returns null when no expenses`() = runTest {
        val startDate = 1704067200000L
        val endDate = 1704153600000L
        every { transactionDao.getTotalExpenses(startDate, endDate) } returns flowOf(null)

        repository.getTotalExpenses(startDate, endDate).test {
            val result = awaitItem()
            assertNull(result)
            awaitComplete()
        }
    }
}
