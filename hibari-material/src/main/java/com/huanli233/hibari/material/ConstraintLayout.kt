package com.huanli233.hibari.material

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.huanli233.hibari.foundation.Node
import com.huanli233.hibari.runtime.IdAttribute
import com.huanli233.hibari.runtime.Renderer
import com.huanli233.hibari.runtime.Tunable
import com.huanli233.hibari.runtime.id
import com.huanli233.hibari.runtime.intId
import com.huanli233.hibari.runtime.viewId
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.flattenToList
import com.huanli233.hibari.ui.thenViewAttribute
import com.huanli233.hibari.ui.uniqueKey
import com.huanli233.hibari.ui.viewClass

data class VerticalAnchor(val viewId: Int, val side: Int)

data class HorizontalAnchor(val viewId: Int, val side: Int)

sealed class Dimension {
    object WrapContent : Dimension()
    object MatchParent : Dimension()
    object MatchConstraint : Dimension()
    data class Fixed(val dp: Int) : Dimension()
}

class ConstraintSetBuilder(
    private val viewId: Int,
    private val constraintSet: ConstraintSet
) {
    val top = VerticalAnchor(viewId, ConstraintSet.TOP)
    val bottom = VerticalAnchor(viewId, ConstraintSet.BOTTOM)
    val baseline = VerticalAnchor(viewId, ConstraintSet.BASELINE)
    val start = HorizontalAnchor(viewId, ConstraintSet.START)
    val end = HorizontalAnchor(viewId, ConstraintSet.END)

    infix fun VerticalAnchor.constraintTo(other: VerticalAnchor) {
        constraintSet.connect(this.viewId, this.side, other.viewId, other.side)
    }

    infix fun HorizontalAnchor.constraintTo(other: HorizontalAnchor) {
        constraintSet.connect(this.viewId, this.side, other.viewId, other.side)
    }

    /**
     * The layout's own anchors, spelled through a property: a nested classifier is not reachable by
     * simple name from the implicit receiver of `constraint { }`, so `parent.top` resolved for no
     * caller at all while `parent` was only a nested `object`.
     */
    val parent = ParentAnchors

    object ParentAnchors {
        val top = VerticalAnchor(ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        val bottom = VerticalAnchor(ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        val start = HorizontalAnchor(ConstraintSet.PARENT_ID, ConstraintSet.START)
        val end = HorizontalAnchor(ConstraintSet.PARENT_ID, ConstraintSet.END)
    }

    var width: Dimension = Dimension.WrapContent
        set(value) {
            field = value
            val dimensionValue = when (value) {
                is Dimension.Fixed -> value.dp
                is Dimension.MatchConstraint -> ConstraintSet.MATCH_CONSTRAINT
                is Dimension.MatchParent -> ConstraintSet.MATCH_CONSTRAINT
                is Dimension.WrapContent -> ConstraintSet.WRAP_CONTENT
            }
            constraintSet.constrainWidth(viewId, dimensionValue)
        }

    var height: Dimension = Dimension.WrapContent
        set(value) {
            field = value
            val dimensionValue = when (value) {
                is Dimension.Fixed -> value.dp
                is Dimension.MatchConstraint -> ConstraintSet.MATCH_CONSTRAINT
                is Dimension.MatchParent -> ConstraintSet.MATCH_CONSTRAINT
                is Dimension.WrapContent -> ConstraintSet.WRAP_CONTENT
            }
            constraintSet.constrainHeight(viewId, dimensionValue)
        }

    var verticalBias: Float = 0.5f
        set(value) {
            field = value
            constraintSet.setVerticalBias(viewId, value)
        }

    var horizontalBias: Float = 0.5f
        set(value) {
            field = value
            constraintSet.setHorizontalBias(viewId, value)
        }
}

interface ConstraintLayoutScope {
    fun Modifier.constraint(block: ConstraintSetBuilder.() -> Unit): Modifier
}

/**
 * One scope per [ConstraintLayout] call, owning the [ConstraintSet] its children write into. This
 * used to be a shared singleton holding the current set in a field, which a nested
 * `ConstraintLayout` could not survive: it left its own set behind, so every sibling emitted after
 * it connected its views to the inner layout instead of the outer one.
 */
internal class ConstraintLayoutScopeImpl(
    private val constraintSet: ConstraintSet
) : ConstraintLayoutScope {

    override fun Modifier.constraint(block: ConstraintSetBuilder.() -> Unit): Modifier {
        val (id, viewId) = this.getViewId()

        ConstraintSetBuilder(viewId, constraintSet).block()

        // The int is pinned next to the string: without an explicit `id` the string above is
        // anonymous, and only this exact int is guaranteed to be the one the set was built with.
        return id(id).intId(viewId)
    }

    private fun Modifier.getViewId(): Pair<String, Int> {
        return (this.flattenToList().firstOrNull { it is IdAttribute } as? IdAttribute)?.id?.let {
            it to it.viewId
        } ?: Renderer.generateViewId(null)
    }
}


@Tunable
fun ConstraintLayout(
    modifier: Modifier = Modifier,
    content: @Tunable ConstraintLayoutScope.() -> Unit
) {
    val constraintSet = ConstraintSet()
    val scope = ConstraintLayoutScopeImpl(constraintSet)

    Node(
        modifier = modifier
            .viewClass(ConstraintLayout::class.java)
            .thenViewAttribute<ConstraintLayout, ConstraintSet>(uniqueKey, constraintSet) {
                it.applyTo(this)
            },
        content = {
            scope.content()
        }
    )
}