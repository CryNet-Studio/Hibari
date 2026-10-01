package com.huanli233.hibari.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * The tables on [Renderer]'s companion are shared by every renderer in the process and nothing ever
 * leaves them, so whatever they hold is what the process keeps alive until it dies.
 */
class RendererStaticCacheTest {

    @Test
    fun `the cached view constructor holds no reference to its renderer`() {
        val nested = Class.forName("com.huanli233.hibari.runtime.Renderer\$ViewConstructor")

        // A Kotlin `inner` class carries its outer instance in `this$0`. Cached here, that would pin
        // one renderer - and with it the parent view group it renders into, the tree hanging off that
        // group and the context behind it - for every distinct view class the app ever builds.
        assertFalse(
            "ViewConstructor must not capture its Renderer: " +
                    nested.declaredFields.joinToString { "${it.name}:${it.type.simpleName}" },
            nested.declaredFields.any { it.name == "this$0" }
        )
    }

    @Test
    fun `a name already in the id table is answered from the table`() {
        val name = "hibariRendererTestNamedId"
        Renderer.viewIds[name] = 4242

        assertEquals(name to 4242, Renderer.generateViewId(name))

        Renderer.viewIds.remove(name)
    }

    /**
     * Only a name the app can ask for later is worth memoising. `Modifier.constraint { }` mints a fresh
     * anonymous name for a child on every tune, and an entry per tune in a table that is never evicted
     * is a leak of the size of the retune count.
     */
    @Test
    fun `a fresh anonymous name is not registered in the id table`() {
        val before = Renderer.viewIds.size
        val (name, first) = Renderer.generateViewId(null)
        val (other, second) = Renderer.generateViewId(null)

        assertFalse(name.isEmpty())
        assertFalse(first == second)
        assertEquals(before, Renderer.viewIds.size)
    }
}
