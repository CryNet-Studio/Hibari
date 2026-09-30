package com.huanli233.hibari.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * One write to a `remember` slot owns the lifecycle of what it stores: forget what the slot gave up,
 * remember what it now holds, once each. The cache helper used to run the same pair around this call,
 * so every observer was notified twice - and a `DisposableEffect`, whose `onRemembered` runs the
 * effect and stores its cleanup, ran the body twice and kept only the second cleanup, leaving the
 * first subscription with nothing that could ever release it.
 */
class RememberSlotLifecycleTest {

    private val memory = hashMapOf<String, Any?>()
    private val owned = hashMapOf<String, Any?>()
    private val touched = hashSetOf<String>()

    private class Counting : RememberObserver {
        var remembered = 0
        var forgotten = 0
        var abandoned = 0

        override fun onRemembered() {
            remembered++
        }

        override fun onForgotten() {
            forgotten++
        }

        override fun onAbandoned() {
            abandoned++
        }
    }

    private fun slot(value: Any) = Pair(arrayOf<Any?>(value), value)

    @Test
    fun `a first write remembers once and forgets nothing`() {
        val observer = Counting()

        putRememberedSlot(memory, owned, touched, "7-1", slot(observer))

        assertEquals(1, observer.remembered)
        assertEquals(0, observer.forgotten)
    }

    @Test
    fun `replacing a slot notifies each observer exactly once`() {
        val first = Counting()
        val second = Counting()

        putRememberedSlot(memory, owned, touched, "7-1", slot(first))
        putRememberedSlot(memory, owned, touched, "7-1", slot(second))

        assertEquals(1, first.remembered)
        assertEquals(1, first.forgotten)
        assertEquals(1, second.remembered)
        assertEquals(0, second.forgotten)
    }

    @Test
    fun `a slot holding a plain value notifies nothing`() {
        putRememberedSlot(memory, owned, touched, "7-1", slot(3))
        putRememberedSlot(memory, owned, touched, "7-1", slot(4))

        assertEquals(4, (memory["7-1"] as Pair<*, *>).second)
    }

    @Test
    fun `the write leaves the bookkeeping the pruning pass needs`() {
        val observer = Counting()
        val value = slot(observer)

        putRememberedSlot(memory, owned, touched, "7-1", value)

        assertTrue("7-1" in touched)
        assertEquals(value, owned["7-1"])
        assertEquals(value, memory["7-1"])

        // The next round does not reach this slot, so the pruning pass releases it - and releases it
        // only because the value it wrote is still the one in memory.
        touched.clear()
        forgetUntouchedSlots(memory, owned, touched, arrayListOf())

        assertEquals(1, observer.forgotten)
        assertTrue(memory.isEmpty())
    }
}
