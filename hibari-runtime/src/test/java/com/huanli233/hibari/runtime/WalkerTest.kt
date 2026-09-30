package com.huanli233.hibari.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * The walk path is the slot key for `remember` and is now cached, so every move of the walk has to
 * drop the cached value — a stale path would hand remembered state back from the wrong slot.
 */
class WalkerTest {

    @Test
    fun `path follows start, end and clear`() {
        val walker = Walker()
        assertEquals("", walker.path())

        walker.start(7)
        assertEquals("7", walker.path())

        walker.start(3)
        assertEquals("7-3", walker.path())

        walker.end()
        assertEquals("7", walker.path())

        // Two siblings this level has not counted before, so neither picks up an occurrence suffix.
        walker.start(8)
        walker.start(4)
        assertEquals("7-8-4", walker.path())

        walker.clear()
        assertEquals("", walker.path())
    }

    @Test
    fun `reading the path twice does not change it`() {
        val walker = Walker().apply {
            start(1)
            start(2)
        }

        assertEquals(walker.path(), walker.path())
    }

    @Test
    fun `returning to an earlier position rebuilds the path it had`() {
        val walker = Walker()
        walker.start(1)
        val outer = walker.path()

        walker.start(2)
        walker.path()
        walker.end()

        assertEquals(outer, walker.path())
    }

    @Test
    fun `two siblings from one call site get different slots`() {
        val walker = Walker()
        walker.start(5)

        walker.start(9)
        val first = walker.path()
        walker.end()

        walker.start(9)
        val second = walker.path()
        walker.end()

        // Same call site, second sibling of the same parent: a loop body that shares one path hands
        // every iteration the same remember slot and the same node key.
        assertEquals("5-9", first)
        assertNotEquals(first, second)
    }

    @Test
    fun `a fresh composition replays the same sibling slots`() {
        val walker = Walker()
        walker.start(5)
        walker.start(9)
        val first = walker.path()
        walker.end()
        walker.start(9)
        val second = walker.path()
        walker.end()
        walker.end()

        walker.clear()
        walker.start(5)
        walker.start(9)
        assertEquals(first, walker.path())
        walker.end()
        walker.start(9)
        assertEquals(second, walker.path())
    }

    @Test
    fun `a level that only ever enters each key once keeps the old shape`() {
        val walker = Walker()
        walker.start(11)
        walker.start(12)
        assertEquals("11-12", walker.path())
        walker.end()
        walker.start(13)
        assertEquals("11-13", walker.path())
    }

    /**
     * The point of remembering a position instead of naming it again: the slot maps key on this string,
     * so a stable one is a stable hash and a round that touches a thousand slots builds no string.
     */
    @Test
    fun `a position hands out the same string instance across rounds`() {
        val walker = Walker()
        walker.start(5)
        walker.start(9)
        val firstRound = walker.path()
        walker.end()
        walker.end()

        walker.clear()
        walker.start(5)
        walker.start(9)

        assertSame(firstRound, walker.path())
    }

    @Test
    fun `a sibling count does not survive the position above it being entered again`() {
        val walker = Walker()

        // Two turns of a loop that holds the whole subtree: the second turn's children are new
        // siblings, not the first turn's counted on.
        walker.start(1)
        walker.start(2)
        walker.start(3)
        assertEquals("1-2-3", walker.path())
        walker.end()
        walker.start(3)
        assertEquals("1-2-3#2", walker.path())
        walker.end()
        walker.end()
        walker.end()

        walker.start(1)
        walker.start(2)
        assertEquals("1#2-2", walker.path())
        walker.start(3)
        assertEquals("1#2-2-3", walker.path())
    }

    @Test
    fun `ending past the root is an error and not a silent pop`() {
        val walker = Walker()
        walker.start(4)

        walker.end()

        assertThrows(HibariRuntimeError::class.java) { walker.end() }
        // The walk is still where the balanced calls left it, so the next round starts clean.
        assertEquals("", walker.path())
    }
}
