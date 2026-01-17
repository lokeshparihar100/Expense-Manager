package com.expensemanager.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.expensemanager.data.local.entity.Transaction
import com.expensemanager.data.local.entity.TransactionType
import com.expensemanager.data.repository.TransactionRepository
import com.expensemanager.sms.SmsReader
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for SmsViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SmsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var smsReader: SmsReader
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var viewModel: SmsViewModel

    private val testTransaction1 = Transaction(
        id = 0L,
        amount = 100.0,
        description = "Amazon Purchase",
        date = System.currentTimeMillis(),
        type = TransactionType.EXPENSE,
        isFromSms = true,
        smsBody = "Rs.100 debited at Amazon"
    )

    private val testTransaction2 = Transaction(
        id = 0L,
        amount = 500.0,
        description = "Uber Ride",
        date = System.currentTimeMillis(),
        type = TransactionType.EXPENSE,
        isFromSms = true,
        smsBody = "Rs.500 debited for Uber"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        smsReader = mockk()
        transactionRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SmsViewModel {
        return SmsViewModel(smsReader, transactionRepository)
    }

    // ==================== Initial State Tests ====================

    @Test
    fun `initial state has empty transactions`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.scannedTransactions.size)
            assertEquals(0, state.selectedTransactions.size)
            assertFalse(state.isScanning)
            assertFalse(state.isImporting)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Scan SMS Tests ====================

    @Test
    fun `scanSms sets scanning state while scanning`() = runTest {
        every { smsReader.readBankSms(any()) } returns listOf(testTransaction1)

        viewModel = createViewModel()

        viewModel.scanSms(30)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isScanning || state.scannedTransactions.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `scanSms loads transactions successfully`() = runTest {
        every { smsReader.readBankSms(30) } returns listOf(testTransaction1, testTransaction2)

        viewModel = createViewModel()

        viewModel.scanSms(30)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.scannedTransactions.size)
            assertEquals(2, state.selectedTransactions.size)
            assertTrue(state.selectedTransactions.all { it })
            assertFalse(state.isScanning)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `scanSms with custom days back`() = runTest {
        every { smsReader.readBankSms(7) } returns listOf(testTransaction1)

        viewModel = createViewModel()

        viewModel.scanSms(7)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.scannedTransactions.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `scanSms handles error`() = runTest {
        every { smsReader.readBankSms(any()) } throws RuntimeException("Permission denied")

        viewModel = createViewModel()

        viewModel.scanSms(30)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isScanning)
            assertEquals("Permission denied", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `scanSms handles empty result`() = runTest {
        every { smsReader.readBankSms(any()) } returns emptyList()

        viewModel = createViewModel()

        viewModel.scanSms(30)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.scannedTransactions.size)
            assertFalse(state.isScanning)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Toggle Selection Tests ====================

    @Test
    fun `toggleTransactionSelection toggles selection state`() = runTest {
        every { smsReader.readBankSms(any()) } returns listOf(testTransaction1, testTransaction2)

        viewModel = createViewModel()
        viewModel.scanSms(30)
        advanceUntilIdle()

        viewModel.toggleTransactionSelection(0)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.selectedTransactions[0])
            assertTrue(state.selectedTransactions[1])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleTransactionSelection toggles back`() = runTest {
        every { smsReader.readBankSms(any()) } returns listOf(testTransaction1)

        viewModel = createViewModel()
        viewModel.scanSms(30)
        advanceUntilIdle()

        viewModel.toggleTransactionSelection(0)
        viewModel.toggleTransactionSelection(0)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.selectedTransactions[0])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleTransactionSelection ignores invalid index`() = runTest {
        every { smsReader.readBankSms(any()) } returns listOf(testTransaction1)

        viewModel = createViewModel()
        viewModel.scanSms(30)
        advanceUntilIdle()

        viewModel.toggleTransactionSelection(999)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.selectedTransactions.size)
            assertTrue(state.selectedTransactions[0])
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Select/Deselect All Tests ====================

    @Test
    fun `selectAll selects all transactions`() = runTest {
        every { smsReader.readBankSms(any()) } returns listOf(testTransaction1, testTransaction2)

        viewModel = createViewModel()
        viewModel.scanSms(30)
        advanceUntilIdle()

        viewModel.toggleTransactionSelection(0)
        viewModel.toggleTransactionSelection(1)
        viewModel.selectAll()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.selectedTransactions.all { it })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deselectAll deselects all transactions`() = runTest {
        every { smsReader.readBankSms(any()) } returns listOf(testTransaction1, testTransaction2)

        viewModel = createViewModel()
        viewModel.scanSms(30)
        advanceUntilIdle()

        viewModel.deselectAll()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.selectedTransactions.none { it })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Import Tests ====================

    @Test
    fun `importSelectedTransactions imports selected transactions`() = runTest {
        every { smsReader.readBankSms(any()) } returns listOf(testTransaction1, testTransaction2)
        coEvery { transactionRepository.insert(any()) } returns 1L

        viewModel = createViewModel()
        viewModel.scanSms(30)
        advanceUntilIdle()

        var importedCount = 0
        viewModel.importSelectedTransactions(
            onSuccess = { count -> importedCount = count },
            onError = { }
        )
        advanceUntilIdle()

        assertEquals(2, importedCount)
        coVerify(exactly = 2) { transactionRepository.insert(any()) }
    }

    @Test
    fun `importSelectedTransactions only imports selected`() = runTest {
        every { smsReader.readBankSms(any()) } returns listOf(testTransaction1, testTransaction2)
        coEvery { transactionRepository.insert(any()) } returns 1L

        viewModel = createViewModel()
        viewModel.scanSms(30)
        advanceUntilIdle()

        viewModel.toggleTransactionSelection(0) // Deselect first

        var importedCount = 0
        viewModel.importSelectedTransactions(
            onSuccess = { count -> importedCount = count },
            onError = { }
        )
        advanceUntilIdle()

        assertEquals(1, importedCount)
        coVerify(exactly = 1) { transactionRepository.insert(any()) }
    }

    @Test
    fun `importSelectedTransactions with none selected calls error`() = runTest {
        every { smsReader.readBankSms(any()) } returns listOf(testTransaction1)

        viewModel = createViewModel()
        viewModel.scanSms(30)
        advanceUntilIdle()

        viewModel.deselectAll()

        var errorMessage: String? = null
        viewModel.importSelectedTransactions(
            onSuccess = { },
            onError = { errorMessage = it }
        )
        advanceUntilIdle()

        assertEquals("No transactions selected", errorMessage)
    }

    @Test
    fun `importSelectedTransactions handles error`() = runTest {
        every { smsReader.readBankSms(any()) } returns listOf(testTransaction1)
        coEvery { transactionRepository.insert(any()) } throws RuntimeException("Database error")

        viewModel = createViewModel()
        viewModel.scanSms(30)
        advanceUntilIdle()

        var errorMessage: String? = null
        viewModel.importSelectedTransactions(
            onSuccess = { },
            onError = { errorMessage = it }
        )
        advanceUntilIdle()

        assertEquals("Database error", errorMessage)
    }

    @Test
    fun `importSelectedTransactions resets state on success`() = runTest {
        every { smsReader.readBankSms(any()) } returns listOf(testTransaction1)
        coEvery { transactionRepository.insert(any()) } returns 1L

        viewModel = createViewModel()
        viewModel.scanSms(30)
        advanceUntilIdle()

        viewModel.importSelectedTransactions(
            onSuccess = { },
            onError = { }
        )
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.scannedTransactions.size)
            assertFalse(state.isImporting)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Clear Tests ====================

    @Test
    fun `clearScannedTransactions resets state`() = runTest {
        every { smsReader.readBankSms(any()) } returns listOf(testTransaction1)

        viewModel = createViewModel()
        viewModel.scanSms(30)
        advanceUntilIdle()

        viewModel.clearScannedTransactions()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.scannedTransactions.size)
            assertEquals(0, state.selectedTransactions.size)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
