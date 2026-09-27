package com.huanli233.hibari.runtime

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.huanli233.hibari.runtime.Renderer.Companion.hibariNodeKey
import com.huanli233.hibari.ui.ViewClassAttribute
import com.huanli233.hibari.ui.flattenToList
import com.huanli233.hibari.ui.node.Node

private const val TAG = "HibariDiff"

class Patcher(val renderer: Renderer) {

    /**
     * Matches a child by node key. [atPosition] is where the caller expects that key to live: a
     * parent whose views mirror its node list hits there, which turns a scan that DiffUtil would
     * otherwise pay once per dispatched operation into a single tag check. The scan stays as the
     * fallback for parents that hold views of their own.
     */
    private fun ViewGroup.findViewByKey(key: Any?, atPosition: Int = -1): View? {
        if (atPosition in 0 until childCount) {
            val child = getChildAt(atPosition)
            if (child.getTag(hibariNodeKey) == key) return child
        }
        return this.children.find { it.getTag(hibariNodeKey) == key }
    }

    /** Reached only from inside a log lambda, so the name is never built for a filtered tag. */
    private fun describeParent(parentView: ViewGroup): String =
        parentView.javaClass.simpleName + "@" +
                java.lang.Integer.toHexString(System.identityHashCode(parentView))

    fun patch(parentView: ViewGroup, oldChildren: List<Node>, newChildren: List<Node>) {
        HibariLog.i(TAG) {
            ">>> Starting patch for parent: ${describeParent(parentView)} | " +
                    "oldSize=${oldChildren.size}, newSize=${newChildren.size}"
        }

        if (oldChildren === newChildren) {
            HibariLog.i(TAG) { "<<< Patch skipped for ${describeParent(parentView)}, lists are identical." }
            return
        }

        val diffCallback = HibariDiffCallback(oldChildren, newChildren)

        if (pairsPositionally(oldChildren, newChildren)) {
            // Value-only updates are what a retune overwhelmingly produces. Walking the changed
            // slots directly keeps the Myers pass — its Dot[]/Snake[] arrays and id maps — off the
            // frame, and keeps the update callback object unbuilt for the same reason.
            var changedCount = 0
            for (position in oldChildren.indices) {
                if (diffCallback.areContentsTheSame(position, position)) continue
                changedCount++
                applyChange(parentView, newChildren, position, diffCallback.getChangePayload(position, position))
            }
            TuneStats.markPositionalPatch(changedCount)
            HibariLog.i(TAG) {
                "<<< Patched ${describeParent(parentView)} positionally: " +
                        "$changedCount of ${oldChildren.size} changed."
            }
            return
        }

        TuneStats.markMyersPatch()

        val updateCallback = object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {
                HibariLog.d(TAG) { "onInserted(pos=$position, count=$count) on parent ${describeParent(parentView)}" }
                for (i in 0 until count) {
                    val newNode = newChildren[position + i]
                    val newView = renderer.render(newNode, parentView)
                    parentView.addView(newView, position + i)
                    HibariLog.d(TAG) { "  -> Inserted view ${newView.javaClass.simpleName} for node with key ${newNode.key}" }
                    if (newView is ViewGroup && newNode.children.isNotEmpty()) {
                        patch(newView, emptyList(), newNode.children)
                    }
                }
            }

            override fun onRemoved(position: Int, count: Int) {
                HibariLog.d(TAG) { "onRemoved(pos=$position, count=$count) on parent ${describeParent(parentView)}" }
                for (i in 0 until count) {
                    val oldNode = oldChildren[position + i]
                    // Every removal shifts the remaining children back into `position`, so that is
                    // the slot to check for each of them.
                    val viewToRemove = parentView.findViewByKey(oldNode.key, position)
                    if (viewToRemove != null) {
                        parentView.removeView(viewToRemove)
                        HibariLog.d(TAG) { "  -> Removed view ${viewToRemove.javaClass.simpleName} with key ${oldNode.key}" }
                    } else {
                        HibariLog.e(TAG) { "  -> FAILED to find view to remove for key ${oldNode.key}" }
                    }
                }
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                HibariLog.d(TAG) { "onMoved(from=$fromPosition, to=$toPosition) on parent ${describeParent(parentView)}" }
                val nodeToMove = oldChildren[fromPosition]
                val viewToMove = parentView.findViewByKey(nodeToMove.key, fromPosition)

                if (viewToMove != null) {
                    parentView.removeView(viewToMove)
                    parentView.addView(viewToMove, toPosition)
                    HibariLog.d(TAG) { "  -> Moved view ${viewToMove.javaClass.simpleName} with key ${nodeToMove.key}" }
                } else {
                    HibariLog.e(TAG) { "  -> FAILED to find view to move for key ${nodeToMove.key}" }
                }
            }

            override fun onChanged(position: Int, count: Int, payload: Any?) {
                HibariLog.d(TAG) { "onChanged(pos=$position, count=$count) on parent ${describeParent(parentView)}" }
                for (i in 0 until count) {
                    applyChange(parentView, newChildren, position + i, payload)
                }
            }
        }

        DiffUtil.calculateDiff(diffCallback, false).dispatchUpdatesTo(updateCallback)
    }

    /**
     * Brings the view sitting at [position] up to date with the node that owns that slot. Reached
     * both from a DiffUtil dispatch and from the positional path below, which is why it takes the
     * payload rather than recomputing it.
     */
    private fun applyChange(
        parentView: ViewGroup,
        newChildren: List<Node>,
        position: Int,
        payload: Any?,
    ) {
        val newNode = newChildren[position]
        val viewToUpdate = parentView.findViewByKey(newNode.key, position)

        if (viewToUpdate == null) {
            HibariLog.e(TAG) { "  -> FAILED to find view to update for key ${newNode.key}" }
            return
        }

        HibariLog.d(TAG) {
            val nodeViewClass = (newNode.modifier.flattenToList()
                .firstOrNull { it is ViewClassAttribute } as? ViewClassAttribute)
                ?.viewClass?.simpleName ?: "UnknownNode"
            "  -> Preparing to update view: ${viewToUpdate.javaClass.simpleName} " +
                    "[key=${newNode.key}] from $nodeViewClass | payload: ${payload != null}"
        }

        if (payload is HibariDiffCallback.ModifierChangePayload) {
            // A reused host keeps its measure policy's node, so the measurables and their
            // parent data would otherwise stay pointed at the previous tune.
            if (viewToUpdate is LayoutNodeHost) viewToUpdate.node = newNode
            if (payload.changedAttributes.isNotEmpty()) {
                renderer.applyAttributes(viewToUpdate, payload.changedAttributes)
            }
            if (payload.isChildrenChanged && viewToUpdate is ViewGroup) {
                patch(viewToUpdate, payload.oldNode.children, newNode.children)
            }
        } else {
            // Removed by reference: the key may not sit at `position` in a parent that owns extra
            // views, and removing by index would drop an unrelated child.
            parentView.removeView(viewToUpdate)

            val newView = renderer.render(newNode, parentView)
            parentView.addView(newView, position)
        }
    }
}

/**
 * Whether every old node still pairs with the new node sitting at its own index. The walker keys
 * nodes by position, so an unchanged subtree shape always answers true here; a false answer is a
 * genuine insertion, removal or reorder, which is exactly the case DiffUtil exists for.
 */
internal fun pairsPositionally(oldChildren: List<Node>, newChildren: List<Node>): Boolean {
    if (oldChildren.size != newChildren.size) return false
    return oldChildren.indices.all { oldChildren[it].key == newChildren[it].key }
}
