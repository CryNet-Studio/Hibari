package com.huanli233.hibari.runtime

import androidx.recyclerview.widget.DiffUtil
import com.huanli233.hibari.runtime.attribute.RuntimeAttrsAttribute
import com.huanli233.hibari.ui.Attribute
import com.huanli233.hibari.ui.AttrsAttribute
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.ViewClassAttribute
import com.huanli233.hibari.ui.flattenToList
import com.huanli233.hibari.ui.layoutAttributes
import com.huanli233.hibari.ui.node.Node
import com.huanli233.hibari.ui.viewAttributes
import java.util.IdentityHashMap
import java.util.Objects

class HibariDiffCallback(
    private val oldList: List<Node>,
    private val newList: List<Node>
) : DiffUtil.Callback() {

    /**
     * [oldNode] travels with the payload because it is the exact node this change was computed for:
     * recovering it in the patcher would mean another scan of the old list per changed item.
     */
    data class ModifierChangePayload(
        val changedAttributes: List<Attribute<*>>,
        val isChildrenChanged: Boolean,
        val oldNode: Node
    )

    /**
     * One node's diffable facts, flattened once per patch rather than once per question: both
     * [areContentsTheSame] and [getChangePayload] used to flatten the same two modifiers and rebuild
     * the same four collections.
     */
    private class NodeFacts(node: Node) {
        val flattened: List<Modifier.Element> = node.modifier.flattenToList()
        val viewAttributes: Map<Any, Attribute<*>> = flattened.viewAttributes().associateBy { it.key }
        val layoutAttributes: Map<Any, Attribute<*>> = flattened.layoutAttributes().associateBy { it.key }
        val viewClassAttribute: ViewClassAttribute? =
            flattened.firstOrNull { it is ViewClassAttribute } as? ViewClassAttribute
        val attrsAttribute: AttrsAttribute? =
            flattened.firstOrNull { it is AttrsAttribute } as? AttrsAttribute
        val runtimeAttrsAttribute: RuntimeAttrsAttribute? =
            flattened.firstOrNull { it is RuntimeAttrsAttribute } as? RuntimeAttrsAttribute

        fun equivalentContents(other: NodeFacts): Boolean =
            flattened.size == other.flattened.size &&
                    viewAttributes == other.viewAttributes &&
                    layoutAttributes == other.layoutAttributes &&
                    viewClassAttribute == other.viewClassAttribute &&
                    attrsAttribute == other.attrsAttribute &&
                    runtimeAttrsAttribute == other.runtimeAttrsAttribute
    }

    private val oldFacts = HashMap<Int, NodeFacts>()
    private val newFacts = HashMap<Int, NodeFacts>()

    /** Keyed by the new node, paired with the old node the verdict was computed against. */
    private val childrenVerdicts = IdentityHashMap<Node, Pair<Node, Boolean>>()

    private fun oldFactsAt(position: Int): NodeFacts =
        oldFacts.getOrPut(position) { NodeFacts(oldList[position]) }

    private fun newFactsAt(position: Int): NodeFacts =
        newFacts.getOrPut(position) { NodeFacts(newList[position]) }

    /**
     * Whether a node's children are equal, mirroring the data-class equality the callers used, which
     * walks the entire subtree. DiffUtil asks this per item, so every node below a container was
     * re-walked once per ancestor — O(items * depth) per patch. The verdict of a pair depends only on
     * that pair, so memoizing it makes the whole comparison O(items).
     */
    private fun childrenTheSame(oldNode: Node, newNode: Node): Boolean {
        childrenVerdicts[newNode]?.let { (memoOldNode, verdict) ->
            if (memoOldNode === oldNode) return verdict
        }

        val oldChildren = oldNode.children
        val newChildren = newNode.children
        val same = oldChildren.size == newChildren.size &&
                oldChildren.indices.all { index ->
                    val oldChild = oldChildren[index]
                    val newChild = newChildren[index]
                    oldChild.key == newChild.key &&
                            oldChild.measurePolicy == newChild.measurePolicy &&
                            oldChild.modifier == newChild.modifier &&
                            childrenTheSame(oldChild, newChild)
                }

        childrenVerdicts[newNode] = oldNode to same
        return same
    }

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldKey = oldList[oldItemPosition].key
        val newKey = newList[newItemPosition].key
        val result = Objects.equals(oldKey, newKey)
        return result
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldNode = oldList[oldItemPosition]
        val newNode = newList[newItemPosition]

        if (!childrenTheSame(oldNode, newNode)) {
            return false
        }

        // The chain compares by value, so an unchanged modifier never reaches the deep comparison.
        if (oldNode.modifier == newNode.modifier) {
            return true
        }

        return oldFactsAt(oldItemPosition).equivalentContents(newFactsAt(newItemPosition))
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldNode = oldList[oldItemPosition]
        val newNode = newList[newItemPosition]
        val oldNodeFacts = oldFactsAt(oldItemPosition)
        val newNodeFacts = newFactsAt(newItemPosition)

        if (oldNodeFacts.viewClassAttribute?.viewClass != newNodeFacts.viewClassAttribute?.viewClass) {
            return null
        }

        // Runtime attrs are consumed by the (Context, AttributeSet) constructor only, so a change
        // cannot be applied via a reusable-attribute payload: force a recreate.
        if (oldNodeFacts.runtimeAttrsAttribute != newNodeFacts.runtimeAttrsAttribute) {
            return null
        }

        val changedReusableMods = mutableListOf<Attribute<*>>()
        val allOldAttrs = oldNodeFacts.viewAttributes + oldNodeFacts.layoutAttributes
        val allNewAttrs = newNodeFacts.viewAttributes + newNodeFacts.layoutAttributes
        val allKeys = allOldAttrs.keys + allNewAttrs.keys

        for (key in allKeys) {
            val oldAttr = allOldAttrs[key]
            val newAttr = allNewAttrs[key]

            if (oldAttr == newAttr) continue

            if (newAttr != null) {
                if (newAttr.reuseSupported) {
                    changedReusableMods.add(newAttr)
                } else {
                    return null
                }
            } else {
                return null
            }
        }

        val isChildrenChanged = !childrenTheSame(oldNode, newNode)

        return if (changedReusableMods.isNotEmpty() || isChildrenChanged) {
            ModifierChangePayload(changedReusableMods, isChildrenChanged, oldNode)
        } else {
            null
        }
    }
}
