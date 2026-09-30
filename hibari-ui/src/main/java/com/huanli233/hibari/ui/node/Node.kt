package com.huanli233.hibari.ui.node

import android.content.Context
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.flattenToList
import com.huanli233.hibari.ui.unit.Constraints

data class Node(
    val modifier: Modifier = Modifier,
    var children: List<Node> = emptyList(),
    var key: String? = null,
    val measurePolicy: MeasurePolicy? = null
) {
    private var flattenedCache: List<Modifier.Element>? = null

    /**
     * The modifier chain as a list, taken once per node. The renderer, the patch diff and the
     * measurable cache each used to walk and copy the same chain within one round.
     *
     * Only written while tuning, rendering and measuring, which all run on the main thread, and left
     * null for the nodes none of them asks about.
     */
    val flattened: List<Modifier.Element>
        get() = flattenedCache ?: modifier.flattenToList().also { flattenedCache = it }
}

fun interface MeasurePolicy {
    fun measure(measurables: List<Measurable>, constraints: Constraints): Placeable
}

interface Measurable {
    val parentData: Any?
    val context: Context
    fun measure(constraints: Constraints): Placeable
}

abstract class Placeable {
    var width: Int = 0
        protected set
    var height: Int = 0
        protected set

    abstract fun placeAt(x: Int, y: Int)
}