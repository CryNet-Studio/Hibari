package com.huanli233.hibari.ui.geometry

import com.huanli233.hibari.ui.unit.Dp
import com.huanli233.hibari.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `Shape.toRadii` is the one geometry every wear container funnels through, and its clamping rule is
 * what stops a 36.dp "large" shape from turning a small chip into a pill.
 */
class ShapeTest {

    private fun radii(shape: Shape, w: Float, h: Float, density: Float): FloatArray =
        FloatArray(8).also { shape.toRadii(w, h, density, it) }

    @Test
    fun radiiAreDpScaledIntoPixelPairs() {
        val r = radii(RoundedCornerShape(8.dp), 200f, 100f, 2f)
        // Canvas order is top-left, top-right, bottom-right, bottom-left, each as an x/y pair.
        assertEquals(List(8) { 16f }, r.toList())
    }

    @Test
    fun cornersKeepTheirOwnValues() {
        val r = radii(
            CornerBasedShape(topStart = 10.dp, topEnd = 20.dp, bottomEnd = 30.dp, bottomStart = 40.dp),
            1000f, 1000f, 1f,
        )
        assertEquals(listOf(10f, 20f, 30f, 40f), listOf(r[0], r[2], r[4], r[6]))
        assertEquals(r[0], r[1], 0f)
        assertEquals(r[2], r[3], 0f)
    }

    @Test
    fun radiusIsClampedToShareOfShorterSide() {
        val max = 100f * CornerBasedShape.MaxCornerPercentage
        val r = radii(RoundedCornerShape(500.dp), 400f, 100f, 1f)
        assertEquals(max, r[0], 0f)
        // The clamp follows the short side, so a tall box still gets pill-safe corners.
        val tall = radii(RoundedCornerShape(500.dp), 100f, 900f, 1f)
        assertEquals(max, tall[0], 0f)
    }

    @Test
    fun infinityMeansHalfTheShorterSide() {
        val r = radii(RoundedCornerShape(Dp.Infinity), 300f, 100f, 1f)
        assertEquals(45f, r[0], 0f)
        assertEquals(45f, r[6], 0f)
    }

    @Test
    fun maxCornerPercentageIsHalfOfNinety() {
        assertEquals(0.45f, CornerBasedShape.MaxCornerPercentage, 1e-6f)
    }

    @Test
    fun circleUsesHalfOfShorterSide() {
        val r = radii(CircleShape, 300f, 120f, 3f)
        assertTrue(r.all { it == 60f })
    }

    @Test
    fun rectangleIsSquare() {
        assertTrue(radii(RectangleShape, 300f, 120f, 3f).all { it == 0f })
    }

    @Test
    fun unspecifiedAndInfinityFallBackToZero() {
        assertEquals(0f, Dp.Unspecified.orZero().value, 0f)
        assertEquals(0f, Dp.Infinity.orZero().value, 0f)
        assertEquals(6f, 6.dp.orZero().value, 0f)
    }
}
