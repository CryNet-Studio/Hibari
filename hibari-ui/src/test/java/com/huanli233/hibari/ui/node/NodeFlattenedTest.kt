package com.huanli233.hibari.ui.node

import android.view.View
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.ViewClassAttribute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * The renderer, the patch diff and the measurable cache all want the modifier chain as a list, and
 * they ask within the same round, so the walk happens once per node and the nodes nobody asks about
 * keep the list null rather than paying for it.
 */
class NodeFlattenedTest {

    private val chain: Modifier = Modifier.then(ViewClassAttribute(View::class.java))

    @Test
    fun `the same node hands back the list it built`() {
        val node = Node(modifier = chain)

        assertSame(node.flattened, node.flattened)
        assertEquals(listOf<Any>(chain), node.flattened)
    }

    @Test
    fun `two nodes over one chain keep their own list`() {
        assertNotSame(Node(chain).flattened, Node(chain).flattened)
    }
}
