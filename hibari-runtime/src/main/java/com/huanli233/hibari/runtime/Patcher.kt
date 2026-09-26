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

    private fun ViewGroup.findViewByKey(key: Any?): View? {
        return this.children.find { it.getTag(hibariNodeKey) == key }
    }

    fun patch(parentView: ViewGroup, oldChildren: List<Node>, newChildren: List<Node>) {
        // Built only when verbose logging is on; identity hash keeps it allocation-cheap otherwise.
        val parentId by lazy {
            parentView.javaClass.simpleName + "@" + System.identityHashCode(parentView).toString(16)
        }
        HibariLog.i(TAG) { ">>> Starting patch for parent: $parentId | oldSize=${oldChildren.size}, newSize=${newChildren.size}" }

        if (oldChildren === newChildren) {
            HibariLog.i(TAG) { "<<< Patch skipped for $parentId, lists are identical." }
            return
        }

        val diffCallback = HibariDiffCallback(oldChildren, newChildren)
        val diffResult = DiffUtil.calculateDiff(diffCallback, false)

        val updateCallback = object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {
                HibariLog.d(TAG) { "onInserted(pos=$position, count=$count) on parent $parentId" }
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
                HibariLog.d(TAG) { "onRemoved(pos=$position, count=$count) on parent $parentId" }
                for (i in 0 until count) {
                    val oldNode = oldChildren[position + i]
                    val viewToRemove = parentView.findViewByKey(oldNode.key)
                    if (viewToRemove != null) {
                        parentView.removeView(viewToRemove)
                        HibariLog.d(TAG) { "  -> Removed view ${viewToRemove.javaClass.simpleName} with key ${oldNode.key}" }
                    } else {
                        HibariLog.e(TAG) { "  -> FAILED to find view to remove for key ${oldNode.key}" }
                    }
                }
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                HibariLog.d(TAG) { "onMoved(from=$fromPosition, to=$toPosition) on parent $parentId" }
                val nodeToMove = oldChildren[fromPosition]
                val viewToMove = parentView.findViewByKey(nodeToMove.key)

                if (viewToMove != null) {
                    parentView.removeView(viewToMove)
                    parentView.addView(viewToMove, toPosition)
                    HibariLog.d(TAG) { "  -> Moved view ${viewToMove.javaClass.simpleName} with key ${nodeToMove.key}" }
                } else {
                    HibariLog.e(TAG) { "  -> FAILED to find view to move for key ${nodeToMove.key}" }
                }
            }

            override fun onChanged(position: Int, count: Int, payload: Any?) {
                HibariLog.d(TAG) { "onChanged(pos=$position, count=$count) on parent $parentId | payload: ${payload != null}" }
                for (i in 0 until count) {
                    val currentPos = position + i
                    val newNode = newChildren[position + i]

                    val viewToUpdate = parentView.findViewByKey(newNode.key)

                    if (viewToUpdate == null) {
                        continue
                    }

                    val oldNode = oldChildren.find { it.key == newNode.key }

                    HibariLog.d(TAG) {
                        val nodeViewClass = (newNode.modifier.flattenToList()
                            .firstOrNull { it is ViewClassAttribute } as? ViewClassAttribute)
                            ?.viewClass?.simpleName ?: "UnknownNode"
                        "  -> Preparing to update view: ${viewToUpdate.javaClass.simpleName} [key=${newNode.key}] with data from node for: $nodeViewClass"
                    }

                    if (payload != null && payload is HibariDiffCallback.ModifierChangePayload) {
                        if (payload.changedAttributes.isNotEmpty()) {
                            renderer.applyAttributes(viewToUpdate, payload.changedAttributes)
                        }
                        if (payload.isChildrenChanged && viewToUpdate is ViewGroup && oldNode != null) {
                            patch(viewToUpdate, oldNode.children, newNode.children)
                        }
                    } else {
                        parentView.removeViewAt(currentPos)

                        val newView = renderer.render(newNode, parentView)
                        parentView.addView(newView, currentPos)
                    }
                }
            }
        }

        diffResult.dispatchUpdatesTo(updateCallback)
    }
}
