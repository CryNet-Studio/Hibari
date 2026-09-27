package com.huanli233.hibari.animation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every wear motion value routes through these curves — the scaling-list interpolator is
 * `CubicBezierEasing(0.3f, 0f, 0.7f, 1f)` — so a solved-curve regression here would show up as wrong
 * item scaling across the whole list, not as a failing component.
 */
class EasingTest {

    private fun assertEndpoints(easing: Easing, label: String) {
        assertEquals("$label must map 0 to 0", 0f, easing.transform(0f), 1e-5f)
        assertEquals("$label must map 1 to 1", 1f, easing.transform(1f), 1e-5f)
    }

    @Test
    fun namedCurvesHonourTheEasingContract() {
        assertEndpoints(FastOutSlowInEasing, "FastOutSlowIn")
        assertEndpoints(LinearOutSlowInEasing, "LinearOutSlowIn")
        assertEndpoints(FastOutLinearInEasing, "FastOutLinearIn")
        assertEndpoints(LinearEasing, "Linear")
    }

    @Test
    fun linearIsIdentityAcrossTheRange() {
        for (i in 0..10) {
            val f = i / 10f
            assertEquals(f, LinearEasing.transform(f), 1e-6f)
        }
    }

    @Test
    fun wearScalingCurveIsMonotonicAndSymmetric() {
        val curve = CubicBezierEasing(0.3f, 0f, 0.7f, 1f)
        assertEndpoints(curve, "wear scale curve")
        assertEquals(0.5f, curve.transform(0.5f), 1e-4f)
        var previous = -1f
        var steps = 0
        while (steps <= 100) {
            val value = curve.transform(steps / 100f)
            assertTrue("curve went backwards at $steps", value >= previous)
            previous = value
            steps++
        }
        // Ease-in-out: it must lag linear early and lead linear late.
        assertTrue(curve.transform(0.25f) < 0.25f)
        assertTrue(curve.transform(0.75f) > 0.75f)
    }

    @Test
    fun standardCurveDepartsSlowlyAndSettlesSlowly() {
        // Reference values solved independently from cubic-bezier(0.4, 0, 0.2, 1) by bisecting the
        // x polynomial, which is a different method from this class' analytic root finder. The curve
        // is behind linear at a quarter, then sprints, then takes most of its time to settle.
        assertEquals(0.2366f, FastOutSlowInEasing.transform(0.25f), 2e-3f)
        assertEquals(0.7756f, FastOutSlowInEasing.transform(0.5f), 2e-3f)
        assertEquals(0.9594f, FastOutSlowInEasing.transform(0.75f), 2e-3f)
    }

    @Test
    fun overshootIsNotClamped() {
        val springy = CubicBezierEasing(0.5f, 0f, 0.5f, 1.6f)
        assertTrue(springy.transform(0.8f) > 1f)
    }
}
