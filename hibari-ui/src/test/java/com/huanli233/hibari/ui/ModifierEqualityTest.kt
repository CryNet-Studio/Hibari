package com.huanli233.hibari.ui

import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * `Modifier` chains are compared on every reconfigure, and the diff only short-circuits through
 * `equals` because a true result there implies the attribute-wise comparison in
 * `HibariDiffCallback.areContentsTheSame` agrees. These tests pin that contract.
 */
class ModifierEqualityTest {

    private object Applier : AttributeApplier<View, Any> {
        override fun apply(target: View, value: Any) = Unit
    }

    private fun attr(key: String, value: Any, reuseSupported: Boolean = true): Modifier =
        ViewAttribute(key, Applier, value, reuseSupported)

    private fun chain(vararg values: Pair<String, Any>): Modifier {
        var result: Modifier = Modifier
        for ((key, value) in values) result = result.then(attr(key, value))
        return result
    }

    @Test
    fun `structurally identical chains are equal`() {
        val first = chain("alpha" to 1f, "scale" to 2f)
        val second = chain("alpha" to 1f, "scale" to 2f)

        assertNotSameChain(first, second)
        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `a changed value breaks equality`() {
        assertNotEquals(chain("alpha" to 1f), chain("alpha" to 0.5f))
    }

    @Test
    fun `order matters because attributes apply in order`() {
        assertNotEquals(
            chain("a" to 1, "b" to 2),
            chain("b" to 2, "a" to 1)
        )
    }

    @Test
    fun `the key separates two attributes carrying the same value`() {
        assertNotEquals(chain("alpha" to 1f), chain("scale" to 1f))
    }

    @Test
    fun `reuseSupported is part of the comparison since it changes how a view is patched`() {
        val reusable = Modifier.then(attr("alpha", 1f, reuseSupported = true))
        val recreate = Modifier.then(attr("alpha", 1f, reuseSupported = false))

        assertNotEquals(reusable, recreate)
    }

    @Test
    fun `an applier swap with the same key and value stays equal, as the attribute-wise compare does`() {
        val a = Modifier.then(ViewAttribute("alpha", Applier, 1f))
        val b = Modifier.then(
            ViewAttribute("alpha", object : AttributeApplier<View, Any> {
                override fun apply(target: View, value: Any) = Unit
            }, 1f)
        )

        assertEquals(a, b)
    }

    @Test
    fun `a single element chain collapses to that element`() {
        val element = attr("alpha", 1f)

        assertEquals(Modifier.then(element), element)
        assertNotEquals(chain("alpha" to 1f, "scale" to 1f), element)
    }

    @Test
    fun `a chain holding a ref modifier compares by instance identity`() {
        val block: (View) -> Unit = {}
        val shared = RefModifier(block)

        assertEquals(
            chain("alpha" to 1f).then(shared),
            chain("alpha" to 1f).then(shared)
        )
        // Two separate lambdas are two instances, so such a chain never short-circuits to "same".
        assertNotEquals(
            chain("alpha" to 1f).then(RefModifier(block)),
            chain("alpha" to 1f).then(RefModifier(block))
        )
    }

    private fun assertNotSameChain(first: Modifier, second: Modifier) {
        if (first === second) throw AssertionError("the two chains must be distinct instances")
    }
}
