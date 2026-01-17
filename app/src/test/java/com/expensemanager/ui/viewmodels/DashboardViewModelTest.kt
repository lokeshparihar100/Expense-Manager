package com.expensemanager.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.expensemanager.data.local.entity.Transaction
import com.expensemanager.data.local.entity.TransactionType
import com.expensemanager.data.local.entity.TransactionWithTags
import com.expensemanager.data.repository.TransactionRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for DashboardViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var viewModel: DashboardViewModel

    private val testTransaction = Transaction(
        id = 1L,
        amount = 100.0,
        description = "Test Expense",
        date = System.currentTimeMillis(),
        type = TransactionType.EXPENSE
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
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mockk()

        // Setup default mock responses
        every { transactionRepository.getTotalExpenses(any(), any()) } returns flowOf(1000.0)
        every { transactionRepository.getTotalIncome(any(), any()) } returns flowOf(5000.0)
        every { transactionRepository.getAllTransactionsWithTags() } returns flowOf(listOf(testTransactionWithTags))
        every { transactionRepository.getTransactionCount() } returns flowOf(10)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DashboardViewModel {
        return DashboardViewModel(transactionRepository)
    }

    // ==================== Initial State Tests ====================

    @Test
    fun `initial state shows loading`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dashboard loads data correctly`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(1000.0, state.totalExpenses, 0.01)
            assertEquals(5000.0, state.totalIncome, 0.01)
            assertEquals(4000.0, state.balance, 0.01) // income - expenses
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dashboard shows recent transactions`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.recentTransactions.size)
            assertEquals("Test Expense", state.recentTransactions[0].transaction.description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dashboard shows transaction count`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(10, state.transactionCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Balance Calculation Tests ====================

    @Test
    fun `balance is calculated correctly with positive value`() = runTest {
        every { transactionRepository.getTotalExpenses(any(), any()) } returns flowOf(2000.0)
        every { transactionRepository.getTotalIncome(any(), any()) } returns flowOf(5000.0)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(3000.0, state.balance, 0.01) // 5000 - 2000
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `balance is calculated correctly with negative value`() = runTest {
        every { transactionRepository.getTotalExpenses(any(), any()) } returns flowOf(8000.0)
        every { transactionRepository.getTotalIncome(any(), any()) } returns flowOf(5000.0)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(-3000.0, state.balance, 0.01) // 5000 - 8000
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `balance handles null values`() = runTest {
        every { transactionRepository.getTotalExpenses(any(), any()) } returns flowOf(null)
        every { transactionRepository.getTotalIncome(any(), any()) } returns flowOf(null)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(0.0, state.totalExpenses, 0.01)
            assertEquals(0.0, state.totalIncome, 0.01)
            assertEquals(0.0, state.balance, 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Delete Transaction Tests ====================

    @Test
    fun `deleteTransaction calls repository delete`() = runTest {
        coEvery { transactionRepository.deleteById(any()) } just Runs

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteTransaction(1L)
        advanceUntilIdle()

        coVerify { transactionRepository.deleteById(1L) }
    }

    // ==================== Refresh Tests ====================

    @Test
    fun `refresh sets loading state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.refresh()

        viewModel.uiState.test {
            val state = awaitItem()
            // After refresh completes, loading should be false
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Recent Transactions Limit Tests ====================

    @Test
    fun `recent transactions limited to 10 items`() = runTest {
        val manyTransactions = (1..20).map { 
            testTransactionWithTags.copy(
                transaction = testTransaction.copy(id = it.toLong())
            )
        }
        every { transactionRepository.getAllTransactionsWithTags() } returns flowOf(manyTransactions)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(10, state.recentTransactions.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty transactions list handled correctly`() = runTest {
        every { transactionRepository.getAllTransactionsWithTags() } returns flowOf(emptyList())
        every { transactionRepository.getTransactionCount() } returns flowOf(0)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.recentTransactions.size)
            assertEquals(0, state.transactionCount)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
