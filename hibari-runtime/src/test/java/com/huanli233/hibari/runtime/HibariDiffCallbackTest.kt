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
 * The modifier short-circuit answers it through value equality on the chain and the subtree
 * comparison is memoized, so these pin all of it — an unchanged chain reports no change, a change at
 * any depth still reports, and a memoized verdict never crosses over to another pairing.
 */
class HibariDiffCallbackTest {

    private object Applier : AttributeApplier<View, Any> {
        override fun apply(target: View, value: Any) = Unit
    }

    private fun modifierWith(alpha: Any, viewClass: Class<out View> = View::class.java): Modifier =
        Modifier
            .then(ViewClassAttribute(viewClass))
            .then(ViewAttribute("alpha", Applier, alpha))

    private fun node(
        alpha: Any,
        key: String? = null,
        children: List<Node> = emptyList(),
        viewClass: Class<out View> = View::class.java
    ): Node = Node(modifier = modifierWith(alpha, viewClass)).apply {
        this.children = children
        this.key = key
    }

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
            listOf(node(1f, viewClass = View::class.java)),
            listOf(node(1f, viewClass = TextViewLike::class.java))
        )

        assertFalse(callback.areContentsTheSame(0, 0))
        assertNull(callback.getChangePayload(0, 0))
    }

    @Test
    fun `a nested subtree that matches all the way down counts as unchanged`() {
        val callback = HibariDiffCallback(
            listOf(node(1f, children = listOf(node(2f, children = listOf(node(3f)))))),
            listOf(node(1f, children = listOf(node(2f, children = listOf(node(3f))))))
        )

        assertTrue(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `a change three levels down is still reported`() {
        val callback = HibariDiffCallback(
            listOf(node(1f, children = listOf(node(2f, children = listOf(node(3f)))))),
            listOf(node(1f, children = listOf(node(2f, children = listOf(node(-3f))))))
        )

        assertFalse(callback.areContentsTheSame(0, 0))

        val payload = callback.getChangePayload(0, 0) as HibariDiffCallback.ModifierChangePayload
        assertTrue(payload.isChildrenChanged)
        assertTrue(payload.changedAttributes.isEmpty())
    }

    @Test
    fun `a nested key difference counts as a changed subtree`() {
        val callback = HibariDiffCallback(
            listOf(node(1f, children = listOf(node(2f, key = "a")))),
            listOf(node(1f, children = listOf(node(2f, key = "b"))))
        )

        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `the subtree memo never answers for a different old node`() {
        val newChild = node(2f)
        val callback = HibariDiffCallback(
            listOf(
                node(1f, children = listOf(node(9f))),
                node(1f, children = listOf(node(2f)))
            ),
            listOf(node(1f, children = listOf(newChild)))
        )

        // The first question records a verdict for newChild against the 9f node; the second pairs the
        // same new node with a matching one and must not inherit that answer.
        assertFalse(callback.areContentsTheSame(0, 0))
        assertTrue(callback.areContentsTheSame(1, 0))
    }

    @Test
    fun `an unchanged pair of leaves never builds the caches`() {
        val callback = HibariDiffCallback(
            listOf(node(1f, key = "0"), node(2f, key = "1")),
            listOf(node(1f, key = "0"), node(2f, key = "1"))
        )

        assertTrue(callback.areContentsTheSame(0, 0))
        assertTrue(callback.areContentsTheSame(1, 1))
        assertFalse(callback.verdictsMemoized())
    }

    @Test
    fun `a container is what starts the memo`() {
        val callback = HibariDiffCallback(
            listOf(node(1f, children = listOf(node(2f)))),
            listOf(node(1f, children = listOf(node(2f))))
        )

        assertTrue(callback.areContentsTheSame(0, 0))
        assertTrue(callback.verdictsMemoized())
    }

    @Test
    fun `asking twice gives the same answers`() {
        val callback = HibariDiffCallback(listOf(node(1f)), listOf(node(0.5f)))

        val contents = callback.areContentsTheSame(0, 0)
        val payload = callback.getChangePayload(0, 0)

        assertEquals(contents, callback.areContentsTheSame(0, 0))
        assertEquals(payload, callback.getChangePayload(0, 0))
    }

    private class TextViewLike : View(null)
}
