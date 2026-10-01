package com.huanli233.hibari.runtime.snapshots

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `SnapshotIdSet` is the bit set every snapshot advance reshapes: it keeps the top two words in longs
 * and the ids below them in a sorted array, and it is documented as immutable - an operation that
 * changes nothing returns the same instance, so a caller can tell a reshape from a no-op. Its own
 * comment says a normalised form is not guaranteed, which is what makes the operations worth pinning
 * from the outside rather than trusting the field layout.
 */
class SnapshotIdSetTest {

    private fun idsOf(vararg ids: SnapshotId): SnapshotIdSet =
        ids.fold(SnapshotIdSet.EMPTY) { set, id -> set.set(id) }

    private fun SnapshotIdSet.drain(): List<SnapshotId> {
        val out = ArrayList<SnapshotId>()
        fastForEach { out.add(it) }
        return out
    }

    @Test
    fun `an id set is readable in either word and below the bound`() {
        // 300 pulls the lower bound up past it, so 5 lands in the sparse array below the bound while
        // 300 sits in one of the two longs. Both have to answer.
        val set = idsOf(300, 5, 260)

        assertTrue(set.get(300))
        assertTrue(set.get(5))
        assertTrue(set.get(260))
        assertFalse(set.get(6))
        assertFalse(set.get(299))
        assertEquals(listOf(5L, 260L, 300L), set.drain())
    }

    @Test
    fun `an operation that changes nothing returns the same instance`() {
        val set = idsOf(1, 2, 3)

        assertSame("setting a set bit", set, set.set(2))
        assertSame("clearing a clear bit", set, set.clear(99))
        assertSame("andNot with the empty set", set, set.andNot(SnapshotIdSet.EMPTY))
        assertSame("or with the empty set", set, set.or(SnapshotIdSet.EMPTY))
    }

    @Test
    fun `an operation that changes something returns a different set and leaves the first alone`() {
        val set = idsOf(1, 2, 3)
        val cleared = set.clear(2)

        assertNotSame(set, cleared)
        assertTrue("the previous set is not modified", set.get(2))
        assertFalse(cleared.get(2))
        assertEquals(listOf(1L, 3L), cleared.drain())
    }

    @Test
    fun `andNot drops exactly the ids of the other set`() {
        val set = idsOf(1, 2, 70, 130, 300)
        val dropped = idsOf(70, 300)

        val result = set.andNot(dropped)

        assertTrue(result.get(1))
        assertTrue(result.get(2))
        assertTrue(result.get(130))
        assertFalse(result.get(70))
        assertFalse(result.get(300))
        // and the set on the right is untouched by the operation
        assertTrue(dropped.get(70))
    }

    @Test
    fun `or unions and and intersects across the word boundary`() {
        val left = idsOf(1, 200)
        val right = idsOf(200, 500)

        val union = left.or(right)
        assertTrue(union.get(1))
        assertTrue(union.get(200))
        assertTrue(union.get(500))

        val intersection = left.and(right)
        assertFalse(intersection.get(1))
        assertTrue(intersection.get(200))
        assertFalse(intersection.get(500))
    }

    @Test
    fun `lowest answers the smallest id and the default when empty`() {
        assertEquals(5L, idsOf(300, 260, 5).lowest(-1))
        assertEquals(260L, idsOf(300, 260).lowest(-1))
        assertEquals(-1L, SnapshotIdSet.EMPTY.lowest(-1))
        assertEquals(1L, idsOf(1, 2).lowest(-1))
    }

    /**
     * The ids arrive out of order and the drain has to come back sorted ascending: the sparse array is
     * kept in order by a binary search, and the two longs are read low bit first, so the array is what
     * makes the whole thing ordered.
     */
    @Test
    fun `every id comes back once and in order however they went in`() {
        val ids = listOf(5L, 900L, 64L, 63L, 130L, 7L, 4000L, 128L, 2L)
        val set = idsOf(*ids.toLongArray())

        assertEquals(ids.sorted(), set.drain())
        assertEquals(ids.size, set.drain().size)
    }
}
