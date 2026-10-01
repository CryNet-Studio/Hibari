package com.huanli233.hibari.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * A local read rests on two halves: one stack per local in the map, and `updatedStateOf` deciding
 * whether the holder a previous round pushed can be kept. Keeping it is the point - a static value
 * that did not change reuses its holder, so a provider costs nothing per tune - and not keeping it
 * when the value did change is the other half, or the read would answer with the previous round's
 * value.
 *
 * The stack unwinding is also what the tuner's self-heal depends on: a round that threw leaves its
 * providers pushed, so the next round clears the stacks before it starts, and a cleared stack has to
 * read back as the default rather than as whatever the dead round provided.
 */
class TunationLocalsTest {

    private val local = staticTunationLocalOf<String> { "default" }
    private val dynamic = tunationLocalOf { "dynamic default" }

    @Suppress("UNCHECKED_CAST")
    private fun stackOf(map: TunationLocalsHashMap, key: TunationLocal<String>): ArrayDeque<ValueHolder<out Any?>> =
        map.getOrPut(key as TunationLocal<Any?>) { ArrayDeque() }

    private fun holderFor(
        key: ProvidableTunationLocal<String>,
        value: String,
        previous: ValueHolder<String>? = null
    ): ValueHolder<String> = key.updatedStateOf(key.provides(value), previous)

    @Test
    fun `a local nobody provided answers with its default`() {
        val map = tunationLocalHashMapOf()

        with(map) { assertEquals("default", local.currentValue) }
        with(map) { assertEquals("dynamic default", dynamic.currentValue) }
    }

    @Test
    fun `a provider pushed over the default is what a read answers`() {
        val map = tunationLocalHashMapOf()

        stackOf(map, local).addFirst(holderFor(local, "provided"))

        with(map) { assertEquals("provided", local.currentValue) }
    }

    @Test
    fun `the same static value keeps the holder it already had`() {
        val first = holderFor(local, "unchanged")

        val second = holderFor(local, "unchanged", first)

        assertSame("an unchanged static value must not allocate a second holder", first, second)
    }

    @Test
    fun `a changed static value does not keep the holder`() {
        val first = holderFor(local, "before")

        val second = holderFor(local, "after", first)

        assertNotSame("keeping the old holder would answer with the previous round's value", first, second)
        assertEquals("after", second.readValue(tunationLocalHashMapOf()))
    }

    @Test
    fun `a dynamic provider keeps one holder and moves the value inside it`() {
        val map = tunationLocalHashMapOf()
        val first = holderFor(dynamic, "one")

        val second = holderFor(dynamic, "two", first)
        stackOf(map, dynamic).addFirst(second)

        assertSame("a dynamic value is read out of a state the holder already holds", first, second)
        with(map) { assertEquals("two", dynamic.currentValue) }
    }

    @Test
    fun `nested providers unwind back to the value below and then to the default`() {
        val map = tunationLocalHashMapOf()
        val outer = holderFor(local, "outer")
        stackOf(map, local).addFirst(outer)

        stackOf(map, local).addFirst(holderFor(local, "inner", outer))
        with(map) { assertEquals("inner", local.currentValue) }

        stackOf(map, local).removeFirst()
        with(map) { assertEquals("outer", local.currentValue) }

        // What a crashed round left behind, and what the next round's drain has to undo.
        stackOf(map, local).clear()
        with(map) { assertEquals("default", local.currentValue) }
    }
}
