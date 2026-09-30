package com.huanli233.hibari.material

import com.huanli233.hibari.ui.Attribute
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.flattenToList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * The patch diff groups a node's attributes by key, so two attributes sharing one key collapse into
 * one slot and only the last of them can ever be seen to change. Every rule helper builds its
 * attribute in the same function, which used to hand them all the same key.
 */
class RelativeLayoutRuleKeyTest {

    private fun Modifier.attributeKeys(): List<Any> =
        flattenToList().filterIsInstance<Attribute<*>>().map { it.key }

    private fun Modifier.attributes(): List<Attribute<*>> =
        flattenToList().filterIsInstance<Attribute<*>>()

    @Test
    fun `two rules on one chain keep two keys`() {
        val keys = with(RelativeLayoutScopeInstance) {
            Modifier.below(1).above(2).attributeKeys()
        }

        assertEquals(2, keys.size)
        assertEquals(2, keys.distinct().size)
    }

    @Test
    fun `the same rule with a different subject is a different attribute`() {
        val belowOne = with(RelativeLayoutScopeInstance) { Modifier.below(1) }
        val belowTwo = with(RelativeLayoutScopeInstance) { Modifier.below(2) }

        assertNotEquals(belowOne, belowTwo)
        assertEquals(belowOne, with(RelativeLayoutScopeInstance) { Modifier.below(1) })
    }

    @Test
    fun `each rule attribute carries the rule it writes`() {
        val values = with(RelativeLayoutScopeInstance) {
            Modifier.centerHorizontal().alignParentTop(align = false).attributes().map { it.value }
        }

        assertEquals(2, values.distinct().size)
    }
}
