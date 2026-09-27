package com.huanli233.hibari.runtime

import com.huanli233.hibari.ui.node.Node
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The verdict here decides whether a patch pays for Myers at all, so it is worth pinning from both
 * sides: too eager means a reorder silently patches the wrong view, too shy means the fast path
 * never fires.
 */
class PatcherPositionalTest {

    private fun keys(vararg names: String?): List<Node> = names.map { Node(key = it) }

    @Test
    fun `an unchanged shape pairs index for index`() {
        assertTrue(pairsPositionally(keys("0", "1", "2"), keys("0", "1", "2")))
    }

    @Test
    fun `two empty lists have nothing to diff`() {
        assertTrue(pairsPositionally(emptyList(), emptyList()))
    }

    @Test
    fun `an unkeyed shape pairs too, the same way item identity already compares nulls`() {
        assertTrue(pairsPositionally(keys(null, null), keys(null, null)))
    }

    @Test
    fun `a grown list is a structural change`() {
        assertFalse(pairsPositionally(keys("0", "1"), keys("0", "1", "2")))
    }

    @Test
    fun `a shrunk list is a structural change`() {
        assertFalse(pairsPositionally(keys("0", "1", "2"), keys("0", "1")))
    }

    @Test
    fun `a swapped pair is a structural change`() {
        assertFalse(pairsPositionally(keys("0", "1"), keys("1", "0")))
    }

    @Test
    fun `a renamed key is a structural change`() {
        assertFalse(pairsPositionally(keys("0", "1"), keys("0", "1x")))
    }
}
