package com.huanli233.hibari.sample

import com.huanli233.hibari.ui.node.Measurable
import com.huanli233.hibari.ui.node.MeasurePolicy
import com.huanli233.hibari.ui.node.Placeable
import com.huanli233.hibari.ui.unit.Constraints

/**
 * Diagnostic, and only diagnostic.
 *
 * Rebuilding the size animation needs a container that measures its content twice - once loose, to
 * learn the size the animation should grow to, and once clamped, to run it. Hibari has the pieces for
 * that (`Node(measurePolicy = …)` backed by a host that measures and lays out real child views), but
 * nothing in this repository has ever supplied a policy, so the whole path is unexercised.
 *
 * This policy is the probe: stack the children and size the block to the widest one. Three rows
 * stacked with no overlap means the substrate holds and the animation work can build on it; rows on
 * top of each other, or missing, says the host has to be fixed first, which is a different and larger
 * job than the animation.
 */
internal object StackingProbePolicy : MeasurePolicy {

    override fun measure(measurables: List<Measurable>, constraints: Constraints): Placeable {
        var widest = constraints.minWidth
        var tallest = 0

        val placeables = ArrayList<Placeable>(measurables.size)
        for (measurable in measurables) {
            val placeable = measurable.measure(constraints)
            placeables.add(placeable)
            if (placeable.width > widest) widest = placeable.width
            tallest += placeable.height
        }

        return StackingProbePlaceable(
            runWidth = widest.coerceAtMost(constraints.maxWidth),
            runHeight = tallest.coerceAtMost(constraints.maxHeight),
            placeables = placeables,
        )
    }
}

private class StackingProbePlaceable(
    runWidth: Int,
    runHeight: Int,
    private val placeables: List<Placeable>,
) : Placeable() {

    init {
        width = runWidth
        height = runHeight
    }

    override fun placeAt(x: Int, y: Int) {
        var top = y
        for (placeable in placeables) {
            placeable.placeAt(x, top)
            top += placeable.height
        }
    }
}
