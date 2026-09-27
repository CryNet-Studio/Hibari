package com.huanli233.hibari.runtime

import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.flattenToList
import com.huanli233.hibari.ui.viewAttributes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * A state binding holds the state, never its contents, so the diff sees one stable attribute while
 * the animation writes every frame. These pin the two halves of that: no per-frame change for the
 * patcher to re-apply (which would restart the subscription), and a real swap of the bound state is
 * still noticed.
 */
class StateBindingTest {

    private fun binding(key: String, state: State<Float>) =
        Modifier.bindState(key, state) { }.flattenToList().viewAttributes().single()

    @Test
    fun `a binding is unchanged while the bound state moves`() {
        val state = mutableStateOf(0f)
        val before = binding("alpha", state)

        state.value = 0.75f

        assertEquals(before, binding("alpha", state))
    }

    @Test
    fun `the bound state itself is the compared value`() {
        val state = mutableStateOf(1f)

        assertEquals(state, binding("alpha", state).value)
    }

    @Test
    fun `swapping the bound state is a change the diff sees`() {
        assertNotEquals(binding("alpha", mutableStateOf(0f)), binding("alpha", mutableStateOf(0f)))
    }

    @Test
    fun `the key separates two bindings on one view`() {
        val state = mutableStateOf(0.5f)

        assertNotEquals(binding("alpha", state), binding("rotation", state))
    }
}
