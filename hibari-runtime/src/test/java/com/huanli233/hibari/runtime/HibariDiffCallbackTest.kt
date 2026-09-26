package com.huanli233.hibari.runtime

import android.view.View
import com.huanli233.hibari.ui.Attribute
import com.huanli233.hibari.ui.AttributeApplier
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.ViewAttribute
import com.huanli233.hibari.ui.ViewClassAttribute
import com.huanli233.hibari.ui.node.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * "Contents the same" is the one verdict the diff may not get wrong: it means nothing is re-applied.
 * The modifier short-circuit now answers it through value equality on the chain, so these pin both
 * halves — an unchanged chain reports no change, and a changed one still reports exactly what
 * changed instead of being swallowed by the shortcut.
 */
class HibariDiffCallbackTest {

    private object Applier : AttributeApplier<View, Any> {
        override fun apply(target: View, value: Any) = Unit
    }

    private fun modifierWith(alpha: Any, viewClass: Class<out View> = View::class.java): Modifier =
        Modifier
            .then(ViewClassAttribute(viewClass))
            .then(ViewAttribute("alpha", Applier, alpha))

    private fun node(alpha: Any, children: List<Node> = emptyList()): Node =
        Node(modifier = modifierWith(alpha)).apply { this.children = children }

    @Test
    fun `an equal but separately built modifier counts as unchanged`() {
        val callback = HibariDiffCallback(listOf(node(1f)), listOf(node(1f)))

        assertTrue(callback.areContentsTheSame(0, 0))
        assertNull(callback.getChangePayload(0, 0))
    }

    @Test
    fun `a changed attribute value is reported as the payload and nothing else`() {
        val oldNode = node(1f)
        val newNode = node(0.5f)
        val callback = HibariDiffCallback(listOf(oldNode), listOf(newNode))

        assertFalse(callback.areContentsTheSame(0, 0))

        val payload = callback.getChangePayload(0, 0) as HibariDiffCallback.ModifierChangePayload
        assertEquals(listOf<Any>(0.5f), payload.changedAttributes.map(Attribute<*>::value))
        assertFalse(payload.isChildrenChanged)
        assertEquals(oldNode, payload.oldNode)
    }

    @Test
    fun `only the children reporting themselves changed asks for a recursion`() {
        val oldNode = node(1f, children = listOf(node(1f)))
        val newNode = node(1f, children = listOf(node(1f), node(1f)))
        val callback = HibariDiffCallback(listOf(oldNode), listOf(newNode))

        assertFalse(callback.areContentsTheSame(0, 0))

        val payload = callback.getChangePayload(0, 0) as HibariDiffCallback.ModifierChangePayload
        assertTrue(payload.isChildrenChanged)
        assertTrue(payload.changedAttributes.isEmpty())
        assertEquals(1, payload.oldNode.children.size)
    }

    @Test
    fun `a different view class forces a recreate instead of a payload`() {
        val callback = HibariDiffCallback(
            listOf(Node(modifier = modifierWith(1f, View::class.java))),
            listOf(Node(modifier = modifierWith(1f, TextViewLike::class.java)))
        )

        assertFalse(callback.areContentsTheSame(0, 0))
        assertNull(callback.getChangePayload(0, 0))
    }

    private class TextViewLike : View(null)
}
