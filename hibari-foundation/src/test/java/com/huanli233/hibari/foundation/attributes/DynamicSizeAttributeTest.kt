package com.huanli233.hibari.foundation.attributes

import android.view.View
import android.view.ViewGroup
import com.huanli233.hibari.ui.Attribute
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.flattenToList
import com.huanli233.hibari.ui.thenUnitLayoutAttribute
import com.huanli233.hibari.ui.thenViewAttribute
import com.huanli233.hibari.ui.unit.DpSize
import com.huanli233.hibari.ui.unit.PaddingValues
import com.huanli233.hibari.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * The patch diff compares attributes by `equals`, which reads the value but never the applier
 * closure. A size or margin that only the closure sees therefore looks unchanged on every tune, and
 * the layout params keep the first value written when the view was created. These go through the
 * public modifiers so the contract is checked where apps actually meet it.
 */
class DynamicSizeAttributeTest {

    private fun Modifier.attribute(): Attribute<*> =
        flattenToList().filterIsInstance<Attribute<*>>().single()

    @Test
    fun `width carries its Dp as the attribute value`() {
        assertEquals(10.dp, Modifier.width(10.dp).attribute().value)
        assertEquals(Modifier.width(10.dp), Modifier.width(10.dp))
        assertNotEquals(Modifier.width(10.dp), Modifier.width(12.dp))
    }

    @Test
    fun `height carries its Dp as the attribute value`() {
        assertEquals(10.dp, Modifier.height(10.dp).attribute().value)
        assertNotEquals(Modifier.height(10.dp), Modifier.height(12.dp))
    }

    @Test
    fun `size carries the whole DpSize as the attribute value`() {
        assertEquals(DpSize(10.dp, 20.dp), Modifier.size(DpSize(10.dp, 20.dp)).attribute().value)
        assertNotEquals(
            Modifier.size(DpSize(10.dp, 20.dp)),
            Modifier.size(DpSize(10.dp, 24.dp))
        )
    }

    @Test
    fun `margin carries its PaddingValues as the attribute value`() {
        assertEquals(PaddingValues(8.dp), Modifier.margin(8.dp).attribute().value)
        assertEquals(Modifier.margin(4.dp), Modifier.margin(4.dp))
        assertNotEquals(Modifier.margin(4.dp), Modifier.margin(8.dp))
    }

    @Test
    fun `a modifier with nothing to vary stays a unit attribute`() {
        assertEquals(Unit, Modifier.matchParentWidth().attribute().value)
        assertEquals(Modifier.matchParentWidth(), Modifier.matchParentWidth())
    }

    @Test
    fun `an explicit reuseSupported reaches the attribute`() {
        val layout = Modifier.thenUnitLayoutAttribute<ViewGroup.LayoutParams>(
            "layout-key",
            reuseSupported = false
        ) { }.attribute()
        val view = Modifier.thenViewAttribute<View, Int>("view-key", 1, reuseSupported = false) { }
            .attribute()

        // Both helpers used to build their attribute with the default, so opting out of reuse had no
        // effect and an attribute that cannot be patched onto a live view was patched anyway.
        assertEquals(false, layout.reuseSupported)
        assertEquals(false, view.reuseSupported)
    }
}
