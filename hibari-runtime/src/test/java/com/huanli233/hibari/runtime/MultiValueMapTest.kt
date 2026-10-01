package com.huanli233.hibari.runtime

import com.huanli233.hibari.runtime.collection.MultiValueMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `MultiValueMap` keeps one value under a key as the value itself and two or more as a list, so every
 * path that takes a value away has to put the key back into the shape it now matches. `removeValueIf`
 * asked a list for its `first()` after emptying it and wrote that back under the key the line above
 * had just removed, which both threw and left the key alive.
 */
class MultiValueMapTest {

    private fun valuesOf(map: MultiValueMap<String, String>, key: String): List<String> {
        val seen = mutableListOf<String>()
        map.forEachValue(key) { seen.add(it) }
        return seen
    }

    @Test
    fun `removing every value of a key drops the key`() {
        val map = MultiValueMap<String, String>()
        map.add("k", "a")
        map.add("k", "b")

        map.removeValueIf("k") { true }

        assertFalse("the emptied key must not stay in the map", "k" in map)
        assertTrue(map.isEmpty())
    }

    @Test
    fun `the value left behind is the only value under the key`() {
        val map = MultiValueMap<String, String>()
        map.add("k", "a")
        map.add("k", "b")

        map.removeValueIf("k") { it == "a" }

        assertEquals(listOf("b"), valuesOf(map, "k"))
    }

    @Test
    fun `a key holding one bare value is dropped the same way`() {
        val map = MultiValueMap<String, String>()
        map.add("k", "a")

        map.removeValueIf("k") { it == "a" }

        assertFalse("k" in map)
        assertEquals(emptyList<String>(), valuesOf(map, "k"))
    }

    /**
     * What is left has to behave like the single value the map stores for one value: `removeLast`
     * takes it and the key goes with it. A key still holding a list here would leave `removeLast`
     * answering for a list of one instead.
     */
    @Test
    fun `one value left behind is stored as one value again`() {
        val map = MultiValueMap<String, String>()
        map.add("k", "a")
        map.add("k", "b")
        map.add("k", "c")

        map.removeValueIf("k") { it != "c" }

        assertEquals(listOf("c"), valuesOf(map, "k"))
        assertEquals("c", map.removeLast("k"))
        assertFalse("k" in map)
    }
}
