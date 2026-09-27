package com.huanli233.hibari.ui.graphics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the sRGB branch of the packed representation, which is the claim that lets this stand in
 * for Compose's Color without carrying the color-space machinery.
 */
class ColorTest {

    /** Components are stored as bytes, so one step (1/255) is the floor of any comparison. */
    private fun assertClose(expected: Float, actual: Float) =
        assertEquals(expected, actual, 1f / 255f)

    @Test
    fun `long literal unpacks to its sRGB bytes`() {
        // E9 DD FF is Wear's primary token.
        val color = Color(0xFFE9DDFF)
        assertClose(0xE9 / 255f, color.red)
        assertClose(0xDD / 255f, color.green)
        assertClose(0xFF / 255f, color.blue)
        assertClose(1f, color.alpha)
    }

    @Test
    fun `int constructor round-trips through toArgb`() {
        val argb = 0x804D3D76
        assertEquals(argb.toInt(), Color(argb).toArgb())
    }

    @Test
    fun `float constructor quantises to the nearest byte`() {
        val color = Color(red = 0.5f, green = 0.25f, blue = 1f, alpha = 0.75f)
        assertEquals(0xBF, (color.alpha * 255f).toInt())
        assertClose(0x80 / 255f, color.red)
        assertClose(0x40 / 255f, color.green)
        assertClose(1f, color.blue)
    }

    @Test
    fun `alpha may be replaced without touching the channels`() {
        val source = Color(0xFFE9DDFF)
        val faded = source.copy(alpha = 0.5f)
        assertClose(0xE9 / 255f, faded.red)
        assertClose(0xDD / 255f, faded.green)
        assertClose(0.5f, faded.alpha)
    }

    @Test
    fun `unspecified is the only value that reports itself as such`() {
        assertTrue(Color.Unspecified.isUnspecified)
        assertFalse(Color.Unspecified.isSpecified)
        assertFalse(Color.Transparent.isUnspecified)
        assertFalse(Color(0x10000000).isUnspecified)
    }

    @Test
    fun `takeOrElse only substitutes for unspecified`() {
        val fallback = Color(0xFF112233)
        assertEquals(fallback, Color.Unspecified.takeOrElse { fallback })
        assertEquals(Color.White, Color.White.takeOrElse { fallback })
    }

    @Test
    fun `an opaque foreground hides the background completely`() {
        val composited = Color.Black.compositeOver(Color.White)
        assertEquals(0xFF000000.toInt(), composited.toArgb())
        assertClose(1f, composited.alpha)
    }

    @Test
    fun `a transparent foreground leaves the background untouched`() {
        val background = Color(0xFF4D3D76.toInt())
        val composited = Color.Transparent.compositeOver(background)
        assertClose(background.red, composited.red)
        assertClose(background.green, composited.green)
        assertClose(background.blue, composited.blue)
    }

    @Test
    fun `half transparent white over black keeps luminance at the midpoint`() {
        val half = Color.White.copy(alpha = 0.5f)
        val composited = half.compositeOver(Color.Black)
        // Over an opaque background the result stays opaque; only the channel is halved.
        assertClose(1f, composited.alpha)
        assertClose(0.5f, composited.red)
    }

    @Test
    fun `lerp endpoints return the inputs and the midpoint is linear in light`() {
        assertEquals(Color.Black, lerp(Color.Black, Color.White, 0f))
        assertEquals(Color.White, lerp(Color.Black, Color.White, 1f))
        val mid = lerp(Color.Black, Color.White, 0.5f)
        // 0.5 in linear light is 0.7354 in sRGB, not 0.5 — the whole point of interpolating in light.
        assertClose(0.7354f, mid.red)
        assertClose(0.7354f, mid.green)
        assertClose(0.7354f, mid.blue)
    }

    @Test
    fun `lerp clamps out-of-range fractions from overshooting easings`() {
        assertEquals(Color.Black, lerp(Color.Black, Color.White, -0.4f))
        assertEquals(Color.White, lerp(Color.Black, Color.White, 1.6f))
    }

    @Test
    fun `packing matches the layout the component getters assume`() {
        // A-R-G-B must sit in bits 56/48/40/32 so toArgb is a single shift.
        val color = Color(red = 0x12, green = 0x34, blue = 0x56, alpha = 0x78)
        assertEquals(0x78123456L shl 32, color.value.toLong())
    }
}
