package com.huanli233.hibari.runtime

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * The collector is the instrument the next optimization round gets read off, so the two ways it can
 * lie are worth pinning: counting while switched off, and averaging over a window that has nothing
 * in it. The auto-log at 60 tunes stays out of here on purpose — that path needs a real Log.
 */
class TuneStatsTest {

    @Before
    fun switchOff() {
        TuneStats.enabled = false
        TuneStats.startWindow()
    }

    @After
    fun clearWindow() {
        TuneStats.enabled = false
        TuneStats.startWindow()
    }

    @Test
    fun `a switched off collector records nothing`() {
        TuneStats.markNode("1-1")
        TuneStats.markViewCreated()
        TuneStats.addAttributeWrites(5)
        TuneStats.markPositionalPatch(2)
        TuneStats.markMyersPatch()
        TuneStats.recordTune(1_000_000L, 2_000_000L, -1L, -1L)

        assertEquals(0L, TuneStats.tunes)
        assertEquals(0L, TuneStats.nodesEmitted)
        assertEquals(0L, TuneStats.viewsCreated)
        assertEquals(0L, TuneStats.attributeWrites)
        assertEquals(0L, TuneStats.positionalPatches)
        assertEquals(0L, TuneStats.myersPatches)
        assertTrue(TuneStats.report().contains("no tunes"))
    }

    @Test
    fun `the off switches hand back a zero cost rather than a fake sample`() {
        assertEquals(0L, TuneStats.now())
        assertEquals(-1L, TuneStats.allocNow())
    }

    @Test
    fun `an enabled window sums the phases and the hits`() {
        TuneStats.enabled = true

        TuneStats.markNode("7-1")
        TuneStats.markNode("7-2")
        TuneStats.addAttributeWrites(3)
        TuneStats.markPositionalPatch(2)
        TuneStats.markMyersPatch()
        TuneStats.recordTune(2_000_000L, 1_000_000L, -1L, -1L)

        assertEquals(1L, TuneStats.tunes)
        assertEquals(2L, TuneStats.nodesEmitted)
        assertEquals(3L, TuneStats.attributeWrites)
        assertEquals(1L, TuneStats.positionalPatches)
        assertEquals(1L, TuneStats.myersPatches)
        assertEquals(2L, TuneStats.slotsChanged)
        assertEquals(2_000_000L, TuneStats.compositionNanos)
        assertEquals(1_000_000L, TuneStats.patchNanos)
        // Both samples came back unavailable, so the window must not claim a zero allocation count.
        assertFalse(TuneStats.allocCounterAvailable)
        assertTrue(TuneStats.report().contains("window=1"))
        assertTrue(TuneStats.report().contains("composition=2.00ms"))
        assertTrue(TuneStats.report().contains("patch=1.00ms"))
    }

    @Test
    fun `the report names the fattest top level groups`() {
        TuneStats.enabled = true
        TuneStats.markNode("3-1")
        TuneStats.markNode("3-2")
        TuneStats.markNode("9#2-4")
        TuneStats.recordTune(1_000_000L, 1_000_000L, -1L, -1L)

        // Averaged over the window and keyed by the root group, because that is the subtree a skip
        // pass would have to cover; a loop sibling counts as its own group entry.
        val report = TuneStats.report()
        assertTrue(report.contains("3=2"))
        assertTrue(report.contains("9=1"))
    }

    @Test
    fun `starting a window drops what the last one counted`() {
        TuneStats.enabled = true
        TuneStats.recordTune(1L, 1L, 10L, 30L)

        assertEquals(1L, TuneStats.tunes)
        assertEquals(20L, TuneStats.allocations)
        assertTrue(TuneStats.allocCounterAvailable)

        TuneStats.startWindow()

        assertEquals(0L, TuneStats.tunes)
        assertEquals(0L, TuneStats.allocations)
        assertFalse(TuneStats.allocCounterAvailable)
    }
}
