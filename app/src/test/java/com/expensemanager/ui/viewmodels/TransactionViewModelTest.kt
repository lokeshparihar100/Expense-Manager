package com.expensemanager.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.expensemanager.data.local.entity.Tag
import com.expensemanager.data.local.entity.TagType
import com.expensemanager.data.local.entity.Transaction
import com.expensemanager.data.local.entity.TransactionType
import com.expensemanager.data.local.entity.TransactionWithTags
import com.expensemanager.data.repository.TagRepository
import com.expensemanager.data.repository.TransactionRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for TransactionViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var tagRepository: TagRepository
    private lateinit var viewModel: TransactionViewModel

    private val testTransaction = Transaction(
        id = 1L,
        amount = 100.0,
        description = "Test Expense",
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

    private val testPayeeTag = Tag(id = 1L, name = "Amazon", type = TagType.PAYEE)
    private val testCategoryTag = Tag(id = 2L, name = "Shopping", type = TagType.CATEGORY)
    private val testPaymentMethodTag = Tag(id = 3L, name = "Credit Card", type = TagType.PAYMENT_METHOD)
    private val testStatusTag = Tag(id = 4L, name = "Done", type = TagType.STATUS)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mockk()
        tagRepository = mockk()

        // Setup default mock responses
        every { transactionRepository.getAllTransactionsWithTags() } returns flowOf(listOf(testTransactionWithTags))
        every { tagRepository.getPayees() } returns flowOf(listOf(testPayeeTag))
        every { tagRepository.getCategories() } returns flowOf(listOf(testCategoryTag))
        every { tagRepository.getPaymentMethods() } returns flowOf(listOf(testPaymentMethodTag))
        every { tagRepository.getStatuses() } returns flowOf(listOf(testStatusTag))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TransactionViewModel {
        return TransactionViewModel(transactionRepository, tagRepository)
    }

    // ==================== Initial State Tests ====================

    @Test
    fun `initial transactions state shows loading`() = runTest {
        viewModel = createViewModel()

        viewModel.transactionsState.test {
            val state = awaitItem()
            assertTrue(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `transactions load correctly`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.transactionsState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(1, state.transactions.size)
            assertEquals("Test Expense", state.transactions[0].transaction.description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `tags load correctly`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.tags.test {
            val state = awaitItem()
            assertEquals(1, state.payees.size)
            assertEquals(1, state.categories.size)
            assertEquals(1, state.paymentMethods.size)
            assertEquals(1, state.statuses.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Form State Tests ====================

    @Test
    fun `updateFormAmount updates state`() = runTest {
        viewModel = createViewModel()

        viewModel.updateFormAmount("100.50")

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals("100.50", state.amount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateFormDescription updates state`() = runTest {
        viewModel = createViewModel()

        viewModel.updateFormDescription("Test Description")

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals("Test Description", state.description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateFormDate updates state`() = runTest {
        viewModel = createViewModel()
        val testDate = 1704067200000L

        viewModel.updateFormDate(testDate)

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals(testDate, state.date)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateFormType updates state`() = runTest {
        viewModel = createViewModel()

        viewModel.updateFormType(TransactionType.INCOME)

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals(TransactionType.INCOME, state.type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateFormPayee updates state`() = runTest {
        viewModel = createViewModel()

        viewModel.updateFormPayee(1L)

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals(1L, state.payeeId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateFormCategory updates state`() = runTest {
        viewModel = createViewModel()

        viewModel.updateFormCategory(2L)

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals(2L, state.categoryId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateFormPaymentMethod updates state`() = runTest {
        viewModel = createViewModel()

        viewModel.updateFormPaymentMethod(3L)

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals(3L, state.paymentMethodId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateFormStatus updates state`() = runTest {
        viewModel = createViewModel()

        viewModel.updateFormStatus(4L)

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals(4L, state.statusId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetForm clears all form values`() = runTest {
        viewModel = createViewModel()

        viewModel.updateFormAmount("100")
        viewModel.updateFormDescription("Test")
        viewModel.updateFormPayee(1L)
        viewModel.resetForm()

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals("", state.amount)
            assertEquals("", state.description)
            assertNull(state.payeeId)
            assertEquals(TransactionType.EXPENSE, state.type)
            assertFalse(state.isEditing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetForm with income type`() = runTest {
        viewModel = createViewModel()

        viewModel.resetForm(TransactionType.INCOME)

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals(TransactionType.INCOME, state.type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Load Transaction for Edit Tests ====================

    @Test
    fun `loadTransactionForEdit loads transaction data`() = runTest {
        coEvery { transactionRepository.getTransactionWithTagsById(1L) } returns testTransactionWithTags

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadTransactionForEdit(1L)
        advanceUntilIdle()

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals(1L, state.id)
            assertEquals("100.0", state.amount)
            assertEquals("Test Expense", state.description)
            assertEquals(1L, state.payeeId)
            assertEquals(2L, state.categoryId)
            assertTrue(state.isEditing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Save Transaction Tests ====================

    @Test
    fun `saveTransaction with valid data calls repository insert`() = runTest {
        val transactionSlot = slot<Transaction>()
        coEvery { transactionRepository.insert(capture(transactionSlot)) } returns 1L

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateFormAmount("100")
        viewModel.updateFormDescription("Test Transaction")

        var successCalled = false
        viewModel.saveTransaction(
            onSuccess = { successCalled = true },
            onError = { }
        )
        advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals(100.0, transactionSlot.captured.amount, 0.01)
        assertEquals("Test Transaction", transactionSlot.captured.description)
    }

    @Test
    fun `saveTransaction with empty amount calls onError`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateFormDescription("Test Transaction")

        var errorMessage: String? = null
        viewModel.saveTransaction(
            onSuccess = { },
            onError = { errorMessage = it }
        )
        advanceUntilIdle()

        assertEquals("Please enter a valid amount", errorMessage)
    }

    @Test
    fun `saveTransaction with invalid amount calls onError`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateFormAmount("abc")
        viewModel.updateFormDescription("Test Transaction")

        var errorMessage: String? = null
        viewModel.saveTransaction(
            onSuccess = { },
            onError = { errorMessage = it }
        )
        advanceUntilIdle()

        assertEquals("Please enter a valid amount", errorMessage)
    }

    @Test
    fun `saveTransaction with zero amount calls onError`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateFormAmount("0")
        viewModel.updateFormDescription("Test Transaction")

        var errorMessage: String? = null
        viewModel.saveTransaction(
            onSuccess = { },
            onError = { errorMessage = it }
        )
        advanceUntilIdle()

        assertEquals("Please enter a valid amount", errorMessage)
    }

    @Test
    fun `saveTransaction with empty description calls onError`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateFormAmount("100")

        var errorMessage: String? = null
        viewModel.saveTransaction(
            onSuccess = { },
            onError = { errorMessage = it }
        )
        advanceUntilIdle()

        assertEquals("Please enter a description", errorMessage)
    }

    @Test
    fun `saveTransaction when editing calls repository update`() = runTest {
        coEvery { transactionRepository.getTransactionWithTagsById(1L) } returns testTransactionWithTags
        coEvery { transactionRepository.update(any()) } just Runs

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadTransactionForEdit(1L)
        advanceUntilIdle()

        viewModel.updateFormAmount("200")

        var successCalled = false
        viewModel.saveTransaction(
            onSuccess = { successCalled = true },
            onError = { }
        )
        advanceUntilIdle()

        assertTrue(successCalled)
        coVerify { transactionRepository.update(any()) }
    }

    // ==================== Delete Transaction Tests ====================

    @Test
    fun `deleteTransaction calls repository and callback`() = runTest {
        coEvery { transactionRepository.deleteById(any()) } just Runs

        viewModel = createViewModel()
        advanceUntilIdle()

        var successCalled = false
        viewModel.deleteTransaction(1L) { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        coVerify { transactionRepository.deleteById(1L) }
    }

    // ==================== Filter Tests ====================

    @Test
    fun `filterByType with null loads all transactions`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.filterByType(null)
        advanceUntilIdle()

        viewModel.transactionsState.test {
            val state = awaitItem()
            assertNull(state.filterType)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filterByType with EXPENSE filters transactions`() = runTest {
        every { transactionRepository.getTransactionsWithTagsByType(TransactionType.EXPENSE) } returns 
            flowOf(listOf(testTransactionWithTags))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.filterByType(TransactionType.EXPENSE)
        advanceUntilIdle()

        viewModel.transactionsState.test {
            val state = awaitItem()
            assertEquals(TransactionType.EXPENSE, state.filterType)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filterByDateRange updates state`() = runTest {
        val startDate = 1704067200000L
        val endDate = 1704153600000L
        every { transactionRepository.getTransactionsWithTagsByDateRange(startDate, endDate) } returns 
            flowOf(listOf(testTransactionWithTags))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.filterByDateRange(startDate, endDate)
        advanceUntilIdle()

        viewModel.transactionsState.test {
            val state = awaitItem()
            assertEquals(startDate, state.startDate)
            assertEquals(endDate, state.endDate)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
