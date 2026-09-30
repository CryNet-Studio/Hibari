package com.huanli233.hibari.foundation.attributes

import android.view.View
import com.huanli233.hibari.runtime.paddingPartsTagId

/**
 * The padding a view has been told about, split into the part from `Modifier.padding` and the part
 * from each insets modifier.
 *
 * `View.setPadding` is absolute, so a writer that wants to add its own share has to know what the
 * others contributed. Reading it back from the view (`paddingLeft + left`) instead reads this
 * writer's own previous output and inflates the padding by the insets amount on every re-apply.
 * Keeping the contributions here lets each writer emit the sum they imply, which is also how the
 * modifier chain adds them in Compose.
 *
 * A contribution lives on after the modifier that wrote it leaves the chain: attributes are diffed
 * per key and nothing tells this record that a key is gone. `Modifier.padding` already behaved that
 * way on its own, so this does not widen the gap.
 */
internal class PaddingParts {

    var left = 0
        private set
    var top = 0
        private set
    var right = 0
        private set
    var bottom = 0
        private set

    private val own = Edges()
    private val insetsParts = HashMap<Any, Edges>()

    fun setOwn(left: Int, top: Int, right: Int, bottom: Int): PaddingParts {
        own.set(left, top, right, bottom)
        return recompute()
    }

    /** [key] keeps two insets sources on one view apart, so status bars and navigation bars add up. */
    fun setInsets(key: Any, left: Int, top: Int, right: Int, bottom: Int): PaddingParts {
        val part = insetsParts[key] ?: Edges().also { insetsParts[key] = it }
        part.set(left, top, right, bottom)
        return recompute()
    }

    fun writeInto(view: View) {
        view.setPadding(left, top, right, bottom)
    }

    private fun recompute(): PaddingParts {
        var leftSum = own.left
        var topSum = own.top
        var rightSum = own.right
        var bottomSum = own.bottom
        for (part in insetsParts.values) {
            leftSum += part.left
            topSum += part.top
            rightSum += part.right
            bottomSum += part.bottom
        }
        left = leftSum
        top = topSum
        right = rightSum
        bottom = bottomSum
        return this
    }

    private class Edges {
        var left = 0
        var top = 0
        var right = 0
        var bottom = 0

        fun set(left: Int, top: Int, right: Int, bottom: Int) {
            this.left = left
            this.top = top
            this.right = right
            this.bottom = bottom
        }
    }
}

internal fun View.paddingParts(): PaddingParts =
    (getTag(paddingPartsTagId) as? PaddingParts) ?: PaddingParts().also { setTag(paddingPartsTagId, it) }
