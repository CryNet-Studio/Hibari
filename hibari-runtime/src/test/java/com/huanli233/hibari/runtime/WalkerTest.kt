package com.huanli233.hibari.runtime

import org.junit.Assert.assertEquals
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

        walker.start(3)
        walker.start(4)
        assertEquals("7-3-4", walker.path())

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
}
