package com.expensemanager.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.expensemanager.data.local.entity.Tag
import com.expensemanager.data.local.entity.TagType
import com.expensemanager.data.repository.TagRepository
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
 * Unit tests for TagViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TagViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var tagRepository: TagRepository
    private lateinit var viewModel: TagViewModel

    private val testPayeeTag = Tag(
        id = 1L,
        name = "Amazon",
        type = TagType.PAYEE,
        color = "#FF5722",
        isDefault = true
    )

    private val testCategoryTag = Tag(
        id = 2L,
        name = "Shopping",
        type = TagType.CATEGORY,
        color = "#4CAF50",
        isDefault = true
    )

    private val testPaymentMethodTag = Tag(
        id = 3L,
        name = "Credit Card",
        type = TagType.PAYMENT_METHOD,
        isDefault = true
    )

    private val testStatusTag = Tag(
        id = 4L,
        name = "Done",
        type = TagType.STATUS,
        color = "#4CAF50",
        isDefault = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        tagRepository = mockk()

        // Setup default mock responses
        every { tagRepository.getPayees() } returns flowOf(listOf(testPayeeTag))
        every { tagRepository.getCategories() } returns flowOf(listOf(testCategoryTag))
        every { tagRepository.getPaymentMethods() } returns flowOf(listOf(testPaymentMethodTag))
        every { tagRepository.getStatuses() } returns flowOf(listOf(testStatusTag))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TagViewModel {
        return TagViewModel(tagRepository)
    }

    // ==================== Initial State Tests ====================

    @Test
    fun `initial state shows loading`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `tags load correctly`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(1, state.payees.size)
            assertEquals(1, state.categories.size)
            assertEquals(1, state.paymentMethods.size)
            assertEquals(1, state.statuses.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `default selected type is CATEGORY`() = runTest {
        viewModel = createViewModel()

        viewModel.selectedType.test {
            val type = awaitItem()
            assertEquals(TagType.CATEGORY, type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Selected Type Tests ====================

    @Test
    fun `setSelectedType updates selected type to PAYEE`() = runTest {
        viewModel = createViewModel()

        viewModel.setSelectedType(TagType.PAYEE)

        viewModel.selectedType.test {
            val type = awaitItem()
            assertEquals(TagType.PAYEE, type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSelectedType updates selected type to PAYMENT_METHOD`() = runTest {
        viewModel = createViewModel()

        viewModel.setSelectedType(TagType.PAYMENT_METHOD)

        viewModel.selectedType.test {
            val type = awaitItem()
            assertEquals(TagType.PAYMENT_METHOD, type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSelectedType updates selected type to STATUS`() = runTest {
        viewModel = createViewModel()

        viewModel.setSelectedType(TagType.STATUS)

        viewModel.selectedType.test {
            val type = awaitItem()
            assertEquals(TagType.STATUS, type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Form State Tests ====================

    @Test
    fun `updateFormName updates state`() = runTest {
        viewModel = createViewModel()

        viewModel.updateFormName("New Tag")

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals("New Tag", state.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateFormType updates state`() = runTest {
        viewModel = createViewModel()

        viewModel.updateFormType(TagType.PAYEE)

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals(TagType.PAYEE, state.type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateFormColor updates state`() = runTest {
        viewModel = createViewModel()

        viewModel.updateFormColor("#FF5722")

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals("#FF5722", state.color)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateFormColor with null clears color`() = runTest {
        viewModel = createViewModel()

        viewModel.updateFormColor("#FF5722")
        viewModel.updateFormColor(null)

        viewModel.formState.test {
            val state = awaitItem()
            assertNull(state.color)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateFormIcon updates state`() = runTest {
        viewModel = createViewModel()

        viewModel.updateFormIcon("shopping_cart")

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals("shopping_cart", state.icon)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetForm clears all form values`() = runTest {
        viewModel = createViewModel()

        viewModel.updateFormName("Test Tag")
        viewModel.updateFormColor("#FF5722")
        viewModel.resetForm()

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals("", state.name)
            assertNull(state.color)
            assertNull(state.icon)
            assertFalse(state.isEditing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetForm with type sets form type`() = runTest {
        viewModel = createViewModel()

        viewModel.resetForm(TagType.PAYEE)

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals(TagType.PAYEE, state.type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Load Tag for Edit Tests ====================

    @Test
    fun `loadTagForEdit loads tag data`() = runTest {
        coEvery { tagRepository.getById(1L) } returns testPayeeTag

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadTagForEdit(1L)
        advanceUntilIdle()

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals(1L, state.id)
            assertEquals("Amazon", state.name)
            assertEquals(TagType.PAYEE, state.type)
            assertEquals("#FF5722", state.color)
            assertTrue(state.isEditing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTagForEdit with non-existent id does not update form`() = runTest {
        coEvery { tagRepository.getById(999L) } returns null

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadTagForEdit(999L)
        advanceUntilIdle()

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals(0L, state.id)
            assertFalse(state.isEditing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Save Tag Tests ====================

    @Test
    fun `saveTag with valid data calls repository insert`() = runTest {
        val tagSlot = slot<Tag>()
        coEvery { tagRepository.insert(capture(tagSlot)) } returns 1L

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateFormName("New Tag")
        viewModel.updateFormType(TagType.CATEGORY)
        viewModel.updateFormColor("#4CAF50")

        var successCalled = false
        viewModel.saveTag(
            onSuccess = { successCalled = true },
            onError = { }
        )
        advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals("New Tag", tagSlot.captured.name)
        assertEquals(TagType.CATEGORY, tagSlot.captured.type)
        assertEquals("#4CAF50", tagSlot.captured.color)
    }

    @Test
    fun `saveTag with empty name calls onError`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        var errorMessage: String? = null
        viewModel.saveTag(
            onSuccess = { },
            onError = { errorMessage = it }
        )
        advanceUntilIdle()

        assertEquals("Please enter a tag name", errorMessage)
    }

    @Test
    fun `saveTag with blank name calls onError`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateFormName("   ")

        var errorMessage: String? = null
        viewModel.saveTag(
            onSuccess = { },
            onError = { errorMessage = it }
        )
        advanceUntilIdle()

        assertEquals("Please enter a tag name", errorMessage)
    }

    @Test
    fun `saveTag when editing calls repository update`() = runTest {
        coEvery { tagRepository.getById(1L) } returns testPayeeTag
        coEvery { tagRepository.update(any()) } just Runs

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadTagForEdit(1L)
        advanceUntilIdle()

        viewModel.updateFormName("Updated Amazon")

        var successCalled = false
        viewModel.saveTag(
            onSuccess = { successCalled = true },
            onError = { }
        )
        advanceUntilIdle()

        assertTrue(successCalled)
        coVerify { tagRepository.update(any()) }
    }

    @Test
    fun `saveTag trims whitespace from name`() = runTest {
        val tagSlot = slot<Tag>()
        coEvery { tagRepository.insert(capture(tagSlot)) } returns 1L

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateFormName("  New Tag  ")

        viewModel.saveTag(
            onSuccess = { },
            onError = { }
        )
        advanceUntilIdle()

        assertEquals("New Tag", tagSlot.captured.name)
    }

    // ==================== Delete Tag Tests ====================

    @Test
    fun `deleteTag calls repository and success callback`() = runTest {
        coEvery { tagRepository.deleteById(any()) } just Runs

        viewModel = createViewModel()
        advanceUntilIdle()

        var successCalled = false
        viewModel.deleteTag(
            tagId = 1L,
            onSuccess = { successCalled = true },
            onError = { }
        )
        advanceUntilIdle()

        assertTrue(successCalled)
        coVerify { tagRepository.deleteById(1L) }
    }

    @Test
    fun `deleteTag handles exception and calls error callback`() = runTest {
        coEvery { tagRepository.deleteById(any()) } throws RuntimeException("Delete failed")

        viewModel = createViewModel()
        advanceUntilIdle()

        var errorMessage: String? = null
        viewModel.deleteTag(
            tagId = 1L,
            onSuccess = { },
            onError = { errorMessage = it }
        )
        advanceUntilIdle()

        assertEquals("Delete failed", errorMessage)
    }

    // ==================== GetTagsByType Tests ====================

    @Test
    fun `getTagsByType returns payees for PAYEE type`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val tags = viewModel.getTagsByType(TagType.PAYEE)

        assertEquals(1, tags.size)
        assertEquals("Amazon", tags[0].name)
    }

    @Test
    fun `getTagsByType returns categories for CATEGORY type`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val tags = viewModel.getTagsByType(TagType.CATEGORY)

        assertEquals(1, tags.size)
        assertEquals("Shopping", tags[0].name)
    }

    @Test
    fun `getTagsByType returns payment methods for PAYMENT_METHOD type`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val tags = viewModel.getTagsByType(TagType.PAYMENT_METHOD)

        assertEquals(1, tags.size)
        assertEquals("Credit Card", tags[0].name)
    }

    @Test
    fun `getTagsByType returns statuses for STATUS type`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val tags = viewModel.getTagsByType(TagType.STATUS)

        assertEquals(1, tags.size)
        assertEquals("Done", tags[0].name)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `empty tags list handled correctly`() = runTest {
        every { tagRepository.getPayees() } returns flowOf(emptyList())
        every { tagRepository.getCategories() } returns flowOf(emptyList())
        every { tagRepository.getPaymentMethods() } returns flowOf(emptyList())
        every { tagRepository.getStatuses() } returns flowOf(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.payees.size)
            assertEquals(0, state.categories.size)
            assertEquals(0, state.paymentMethods.size)
            assertEquals(0, state.statuses.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
