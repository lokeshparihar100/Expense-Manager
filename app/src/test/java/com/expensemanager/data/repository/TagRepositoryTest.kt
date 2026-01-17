package com.expensemanager.data.repository

import app.cash.turbine.test
import com.expensemanager.data.local.dao.TagDao
import com.expensemanager.data.local.entity.Tag
import com.expensemanager.data.local.entity.TagType
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
 * Unit tests for TagRepository
 */
class TagRepositoryTest {

    private lateinit var tagDao: TagDao
    private lateinit var repository: TagRepository

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
        tagDao = mockk()
        repository = TagRepository(tagDao)
    }

    // ==================== Insert Tests ====================

    @Test
    fun `insert calls dao insert and returns id`() = runTest {
        coEvery { tagDao.insert(any()) } returns 1L

        val result = repository.insert(testPayeeTag)

        assertEquals(1L, result)
        coVerify { tagDao.insert(testPayeeTag) }
    }

    @Test
    fun `insertAll calls dao insertAll`() = runTest {
        val tags = listOf(testPayeeTag, testCategoryTag)
        coEvery { tagDao.insertAll(any()) } just Runs

        repository.insertAll(tags)

        coVerify { tagDao.insertAll(tags) }
    }

    // ==================== Update Tests ====================

    @Test
    fun `update calls dao update with updated timestamp`() = runTest {
        val tagSlot = slot<Tag>()
        coEvery { tagDao.update(capture(tagSlot)) } just Runs

        repository.update(testPayeeTag)

        coVerify { tagDao.update(any()) }
        assertNotNull(tagSlot.captured.updatedAt)
    }

    @Test
    fun `update preserves original data except timestamp`() = runTest {
        val tagSlot = slot<Tag>()
        coEvery { tagDao.update(capture(tagSlot)) } just Runs

        repository.update(testPayeeTag)

        val captured = tagSlot.captured
        assertEquals(testPayeeTag.id, captured.id)
        assertEquals(testPayeeTag.name, captured.name)
        assertEquals(testPayeeTag.type, captured.type)
        assertEquals(testPayeeTag.color, captured.color)
    }

    // ==================== Delete Tests ====================

    @Test
    fun `delete calls dao delete`() = runTest {
        coEvery { tagDao.delete(any()) } just Runs

        repository.delete(testPayeeTag)

        coVerify { tagDao.delete(testPayeeTag) }
    }

    @Test
    fun `deleteById calls dao deleteById`() = runTest {
        coEvery { tagDao.deleteById(any()) } just Runs

        repository.deleteById(1L)

        coVerify { tagDao.deleteById(1L) }
    }

    // ==================== GetById Tests ====================

    @Test
    fun `getById returns tag when found`() = runTest {
        coEvery { tagDao.getById(1L) } returns testPayeeTag

        val result = repository.getById(1L)

        assertNotNull(result)
        assertEquals(testPayeeTag.id, result?.id)
        assertEquals(testPayeeTag.name, result?.name)
    }

    @Test
    fun `getById returns null when not found`() = runTest {
        coEvery { tagDao.getById(999L) } returns null

        val result = repository.getById(999L)

        assertNull(result)
    }

    // ==================== GetAll Tests ====================

    @Test
    fun `getAllTags returns flow of all tags`() = runTest {
        val allTags = listOf(testPayeeTag, testCategoryTag, testPaymentMethodTag, testStatusTag)
        every { tagDao.getAllTags() } returns flowOf(allTags)

        repository.getAllTags().test {
            val result = awaitItem()
            assertEquals(4, result.size)
            awaitComplete()
        }
    }

    @Test
    fun `getTagsByType returns flow of tags filtered by type`() = runTest {
        every { tagDao.getTagsByType(TagType.PAYEE) } returns flowOf(listOf(testPayeeTag))

        repository.getTagsByType(TagType.PAYEE).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(TagType.PAYEE, result[0].type)
            awaitComplete()
        }
    }

    @Test
    fun `getTagsByTypeSync returns list of tags synchronously`() = runTest {
        coEvery { tagDao.getTagsByTypeSync(TagType.CATEGORY) } returns listOf(testCategoryTag)

        val result = repository.getTagsByTypeSync(TagType.CATEGORY)

        assertEquals(1, result.size)
        assertEquals(TagType.CATEGORY, result[0].type)
    }

    // ==================== Specific Type Tests ====================

    @Test
    fun `getPayees returns flow of payee tags`() = runTest {
        every { tagDao.getPayees() } returns flowOf(listOf(testPayeeTag))

        repository.getPayees().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Amazon", result[0].name)
            awaitComplete()
        }
    }

    @Test
    fun `getCategories returns flow of category tags`() = runTest {
        every { tagDao.getCategories() } returns flowOf(listOf(testCategoryTag))

        repository.getCategories().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Shopping", result[0].name)
            awaitComplete()
        }
    }

    @Test
    fun `getPaymentMethods returns flow of payment method tags`() = runTest {
        every { tagDao.getPaymentMethods() } returns flowOf(listOf(testPaymentMethodTag))

        repository.getPaymentMethods().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Credit Card", result[0].name)
            awaitComplete()
        }
    }

    @Test
    fun `getStatuses returns flow of status tags`() = runTest {
        every { tagDao.getStatuses() } returns flowOf(listOf(testStatusTag))

        repository.getStatuses().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Done", result[0].name)
            awaitComplete()
        }
    }

    // ==================== Search Tests ====================

    @Test
    fun `searchTagsByType filters tags by query`() = runTest {
        every { tagDao.searchTagsByType("shop", TagType.CATEGORY) } returns flowOf(listOf(testCategoryTag))

        repository.searchTagsByType("shop", TagType.CATEGORY).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Shopping", result[0].name)
            awaitComplete()
        }
    }

    @Test
    fun `searchTagsByType returns empty list for no match`() = runTest {
        every { tagDao.searchTagsByType("xyz", TagType.CATEGORY) } returns flowOf(emptyList())

        repository.searchTagsByType("xyz", TagType.CATEGORY).test {
            val result = awaitItem()
            assertEquals(0, result.size)
            awaitComplete()
        }
    }

    // ==================== Count Tests ====================

    @Test
    fun `getTagCountByType returns correct count`() = runTest {
        coEvery { tagDao.getTagCountByType(TagType.PAYEE) } returns 5

        val result = repository.getTagCountByType(TagType.PAYEE)

        assertEquals(5, result)
    }

    @Test
    fun `getTagCountByType returns zero for no tags`() = runTest {
        coEvery { tagDao.getTagCountByType(TagType.PAYEE) } returns 0

        val result = repository.getTagCountByType(TagType.PAYEE)

        assertEquals(0, result)
    }

    // ==================== Default Tags Tests ====================

    @Test
    fun `getDefaultTagsByType returns default tags`() = runTest {
        coEvery { tagDao.getDefaultTagsByType(TagType.PAYEE) } returns listOf(testPayeeTag)

        val result = repository.getDefaultTagsByType(TagType.PAYEE)

        assertEquals(1, result.size)
        assertEquals(true, result[0].isDefault)
    }

    @Test
    fun `getDefaultTagsByType returns empty list when no defaults`() = runTest {
        coEvery { tagDao.getDefaultTagsByType(TagType.STATUS) } returns emptyList()

        val result = repository.getDefaultTagsByType(TagType.STATUS)

        assertEquals(0, result.size)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `getAllTags returns empty list when no tags`() = runTest {
        every { tagDao.getAllTags() } returns flowOf(emptyList())

        repository.getAllTags().test {
            val result = awaitItem()
            assertEquals(0, result.size)
            awaitComplete()
        }
    }

    @Test
    fun `insert tag with null color succeeds`() = runTest {
        val tagWithoutColor = testPayeeTag.copy(color = null)
        coEvery { tagDao.insert(any()) } returns 1L

        val result = repository.insert(tagWithoutColor)

        assertEquals(1L, result)
    }

    @Test
    fun `insert tag with null icon succeeds`() = runTest {
        val tagWithoutIcon = testPayeeTag.copy(icon = null)
        coEvery { tagDao.insert(any()) } returns 1L

        val result = repository.insert(tagWithoutIcon)

        assertEquals(1L, result)
    }
}
