package com.huanli233.hibari.foundation.attributes

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * `View.setPadding` is absolute, and the insets writer used to add onto whatever the view already
 * showed — which is its own previous output, so every re-apply inflated the padding again. These pin
 * the arithmetic the writers now share: each contribution is stored once, and the value written is
 * the sum, however often a writer runs.
 */
class PaddingPartsTest {

    @Test
    fun `insets add on top of the padding asked for`() {
        val parts = PaddingParts()
            .setOwn(8, 8, 8, 8)
            .setInsets("statusBars", 0, 24, 0, 0)

        assertEquals(8, parts.left)
        assertEquals(32, parts.top)
        assertEquals(8, parts.right)
        assertEquals(8, parts.bottom)
    }

    @Test
    fun `writing the same contributions again does not grow the padding`() {
        val parts = PaddingParts()
        repeat(5) {
            parts.setOwn(8, 8, 8, 8).setInsets("statusBars", 0, 24, 0, 0)
        }

        assertEquals(8, parts.left)
        assertEquals(32, parts.top)
    }

    @Test
    fun `two insets sources both count`() {
        val parts = PaddingParts()
            .setInsets("statusBars", 0, 24, 0, 0)
            .setInsets("navigationBars", 0, 0, 0, 48)

        assertEquals(24, parts.top)
        assertEquals(48, parts.bottom)
        assertEquals(0, parts.left)
    }

    @Test
    fun `an insets source that changes replaces only its own contribution`() {
        val parts = PaddingParts()
            .setInsets("statusBars", 0, 24, 0, 0)
            .setInsets("navigationBars", 0, 0, 0, 48)

        parts.setInsets("statusBars", 0, 30, 0, 0)

        assertEquals(30, parts.top)
        assertEquals(48, parts.bottom)
    }

    @Test
    fun `the padding writer replaces only what it wrote`() {
        val parts = PaddingParts()
            .setOwn(8, 8, 8, 8)
            .setInsets("statusBars", 0, 24, 0, 0)

        parts.setOwn(16, 16, 16, 16)

        assertEquals(16, parts.left)
        assertEquals(40, parts.top)
    }

    @Test
    fun `a view nobody has written to has no padding`() {
        val parts = PaddingParts()

        assertEquals(0, parts.left)
        assertEquals(0, parts.top)
        assertEquals(0, parts.right)
        assertEquals(0, parts.bottom)
    }
}
