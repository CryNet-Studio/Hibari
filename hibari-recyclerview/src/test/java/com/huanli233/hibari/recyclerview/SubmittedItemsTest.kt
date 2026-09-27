package com.huanli233.hibari.recyclerview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Rows are keyed by something stable and rendered by a lambda the caller rebuilds every tune, so the
 * only thing a diff can honestly compare is the data behind the lambda. These pin both halves: a list
 * that really did not change must not reach the differ, and an item edited under a stable key must.
 */
class SubmittedItemsTest {

    private fun item(key: Any?, data: Any? = key) =
        LazyListItem(key = key, contentType = null, content = {}, data = data)

    @Test
    fun `the same list twice is not submitted twice`() {
        val submitted = SubmittedItems()

        assertNotNull(submitted.take(listOf(item(1), item(2))))
        assertNull(submitted.take(listOf(item(1), item(2))))
    }

    @Test
    fun `an item edited under a stable key goes back to the differ`() {
        val submitted = SubmittedItems()
        submitted.take(listOf(item("a", "first")))

        assertNotNull(submitted.take(listOf(item("a", "second"))))
    }

    @Test
    fun `a grown list is a change`() {
        val submitted = SubmittedItems()
        submitted.take(listOf(item(1)))

        assertNotNull(submitted.take(listOf(item(1), item(2))))
    }

    @Test
    fun `what the differ gets to walk is a copy`() {
        val submitted = SubmittedItems()
        val callerList = mutableListOf(item(1), item(2))
        val snapshot = submitted.take(callerList)!!

        callerList.clear()

        assertEquals(2, snapshot.size)
        assertNotNull(snapshot)
    }

    @Test
    fun `equality follows data but the hash code stays consistent with it`() {
        assertEquals(item("a", "x"), item("a", "x"))
        assertNotEquals(item("a", "x"), item("a", "y"))
        assertEquals(item("a", "x").hashCode(), item("a", "x").hashCode())
    }
}
