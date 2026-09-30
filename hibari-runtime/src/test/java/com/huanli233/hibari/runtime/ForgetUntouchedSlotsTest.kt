package com.huanli233.hibari.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A slot whose branch stopped rendering has to release its observer, or the coroutine behind it runs
 * forever. Ownership is the subtle half: sub-tunations share one memory map, so this tuner may only
 * drop the entries it still holds itself.
 */
class ForgetUntouchedSlotsTest {

    private class Probe : RememberObserver {
        var remembered = 0
        var forgotten = 0
        override fun onRemembered() {
            remembered++
        }

        override fun onForgotten() {
            forgotten++
        }
    }

    private fun slot(value: Any) = Pair(arrayOf<Any?>(value), value)

    @Test
    fun `a slot the composition stopped writing gets forgotten`() {
        val probe = Probe()
        val value = slot(probe)
        val memory = hashMapOf<String, Any?>("1-2" to value)
        val owned = hashMapOf<String, Any?>("1-2" to value)

        forgetUntouchedSlots(memory, owned, hashSetOf())

        assertEquals(1, probe.forgotten)
        assertFalse(memory.containsKey("1-2"))
        assertFalse(owned.containsKey("1-2"))
    }

    @Test
    fun `a slot that was read this round stays where it is`() {
        val probe = Probe()
        val value = slot(probe)
        val memory = hashMapOf<String, Any?>("1-2" to value)
        val owned = hashMapOf<String, Any?>("1-2" to value)
        val touched = hashSetOf("1-2")

        forgetUntouchedSlots(memory, owned, touched)

        assertEquals(0, probe.forgotten)
        assertTrue(memory.containsKey("1-2"))
        assertTrue(owned.containsKey("1-2"))
        // The set is per composition, so it has to come back empty for the next one.
        assertTrue(touched.isEmpty())
    }

    @Test
    fun `a slot another tuner has taken over is left to them`() {
        val mine = Probe()
        val mineValue = slot(mine)
        val theirsValue = slot(Probe())
        val memory = hashMapOf<String, Any?>("1-2" to theirsValue)
        val owned = hashMapOf<String, Any?>("1-2" to mineValue)

        forgetUntouchedSlots(memory, owned, hashSetOf())

        assertEquals(0, mine.forgotten)
        assertTrue(memory.containsKey("1-2"))
        assertEquals(theirsValue, memory["1-2"])
        assertFalse(owned.containsKey("1-2"))
    }

    @Test
    fun `the first composition has nothing to forget`() {
        val memory = hashMapOf<String, Any?>("1-2" to slot(Probe()))

        forgetUntouchedSlots(memory, hashMapOf(), hashSetOf())

        assertTrue(memory.containsKey("1-2"))
    }

    @Test
    fun `only the abandoned slots go`() {
        val kept = Probe()
        val keptValue = slot(kept)
        val dropped = Probe()
        val droppedValue = slot(dropped)
        val memory = hashMapOf<String, Any?>("1" to keptValue, "2" to droppedValue)
        val owned = hashMapOf<String, Any?>("1" to keptValue, "2" to droppedValue)

        forgetUntouchedSlots(memory, owned, hashSetOf("1"))

        assertEquals(0, kept.forgotten)
        assertEquals(1, dropped.forgotten)
        assertEquals(mapOf("1" to keptValue), memory)
    }

    /**
     * The touched set is the record of one round, so it is drained whether or not the round dropped
     * anything: left standing, every slot of the next round would already look touched and nothing
     * would ever be released again.
     */
    @Test
    fun `the touched set is drained when nothing was dropped`() {
        val value = slot(Probe())
        val memory = hashMapOf<String, Any?>("1" to value)
        val owned = hashMapOf<String, Any?>("1" to value)
        val touched = hashSetOf("1")

        forgetUntouchedSlots(memory, owned, touched)

        assertTrue(touched.isEmpty())
    }

    @Test
    fun `the touched set is drained when the tuner owns nothing`() {
        val touched = hashSetOf("1")

        forgetUntouchedSlots(hashMapOf<String, Any?>(), hashMapOf(), touched)

        assertTrue(touched.isEmpty())
    }
}
