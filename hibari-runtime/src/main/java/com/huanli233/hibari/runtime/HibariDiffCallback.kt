package com.huanli233.hibari.runtime

import androidx.recyclerview.widget.DiffUtil
import com.huanli233.hibari.runtime.attribute.RuntimeAttrsAttribute
import com.huanli233.hibari.ui.Attribute
import com.huanli233.hibari.ui.AttrsAttribute
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.ViewClassAttribute
import com.huanli233.hibari.ui.flattenToList
import com.huanli233.hibari.ui.node.Node
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

        /**
         * The node's attributes in the order the chain holds them.
         *
         * A chain can carry the same key twice: every overload of a modifier helper builds its
         * attribute at one call site, and one call site gets one key. Keeping them as a list rather
         * than one slot per key is what lets the diff see a change to the earlier of the two, and the
         * order is what makes the patch write them the way the chain reads.
         */
        val attributes: List<Attribute<*>> = flattened.filterIsInstance<Attribute<*>>()

        /** Built only when the two chains differ beyond order, which is the uncommon answer. */
        val attributeGroups: Map<Any, List<Attribute<*>>> by lazy { attributes.groupBy { it.key } }
        val viewClassAttribute: ViewClassAttribute? =
            flattened.firstOrNull { it is ViewClassAttribute } as? ViewClassAttribute
        val attrsAttribute: AttrsAttribute? =
            flattened.firstOrNull { it is AttrsAttribute } as? AttrsAttribute
        val runtimeAttrsAttribute: RuntimeAttrsAttribute? =
            flattened.firstOrNull { it is RuntimeAttrsAttribute } as? RuntimeAttrsAttribute

        fun equivalentContents(other: NodeFacts): Boolean =
            flattened.size == other.flattened.size &&
                    (attributes == other.attributes || attributeGroups == other.attributeGroups) &&
                    viewClassAttribute == other.viewClassAttribute &&
                    attrsAttribute == other.attrsAttribute &&
                    runtimeAttrsAttribute == other.runtimeAttrsAttribute
    }

    /**
     * All three caches answer only questions a fully unchanged tree never asks, so none of them is
     * built until the first question: they used to cost three empty collections per container per
     * patch, and one float changing on a frame is the common case.
     */
    private var oldFacts: HashMap<Int, NodeFacts>? = null
    private var newFacts: HashMap<Int, NodeFacts>? = null

    /** Keyed by the new node, holding the old node the verdict was computed against. */
    private var childrenVerdicts: IdentityHashMap<Node, Pair<Node, Boolean>>? = null

    private fun oldFactsAt(position: Int): NodeFacts {
        val cache = oldFacts ?: HashMap<Int, NodeFacts>().also { oldFacts = it }
        return cache.getOrPut(position) { NodeFacts(oldList[position]) }
    }

    private fun newFactsAt(position: Int): NodeFacts {
        val cache = newFacts ?: HashMap<Int, NodeFacts>().also { newFacts = it }
        return cache.getOrPut(position) { NodeFacts(newList[position]) }
    }

    private fun verdicts(): IdentityHashMap<Node, Pair<Node, Boolean>> =
        childrenVerdicts ?: IdentityHashMap<Node, Pair<Node, Boolean>>().also { childrenVerdicts = it }

    /**
     * Whether a node's children are equal, mirroring the data-class equality the callers used, which
     * walks the entire subtree. DiffUtil asks this per item, so every node below a container was
     * re-walked once per ancestor — O(items * depth) per patch. The verdict of a pair depends only on
     * that pair, so memoizing it makes the whole comparison O(items).
     */
    private fun childrenTheSame(oldNode: Node, newNode: Node): Boolean {
        val oldChildren = oldNode.children
        val newChildren = newNode.children

        // Leaves are most of a tree and their verdict is free to recompute, so they are settled
        // before the memo: it would otherwise take a lookup plus a Pair for every one of them.
        if (oldChildren.isEmpty() && newChildren.isEmpty()) return true

        childrenVerdicts?.get(newNode)?.let { (memoOldNode, verdict) ->
            if (memoOldNode === oldNode) return verdict
        }

        val same = oldChildren.size == newChildren.size &&
                oldChildren.indices.all { index ->
                    val oldChild = oldChildren[index]
                    val newChild = newChildren[index]
                    oldChild.key == newChild.key &&
                            oldChild.measurePolicy == newChild.measurePolicy &&
                            oldChild.modifier == newChild.modifier &&
                            childrenTheSame(oldChild, newChild)
                }

        verdicts()[newNode] = oldNode to same
        return same
    }

    /** Whether the subtree memo was ever asked for a verdict, i.e. whether a container was compared. */
    internal fun verdictsMemoized(): Boolean = childrenVerdicts != null

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

        val oldGroups = oldNodeFacts.attributeGroups
        val newGroups = newNodeFacts.attributeGroups
        // An attribute that left the chain took its value with it while the view still shows it, so
        // the view has to be rebuilt. One that has only arrived needs nothing taken off the view, so
        // it can be patched on: that is what a conditional modifier turning on looks like.
        for ((key, oldGroup) in oldGroups) {
            val newGroup = newGroups[key] ?: return null
            if (newGroup.size < oldGroup.size) return null
        }

        val changedReusableMods = mutableListOf<Attribute<*>>()
        val occurrences = HashMap<Any, Int>()
        // Walked in chain order, because attributes can write the same property and the last of them
        // is the one the view should end up showing.
        for (attribute in newNodeFacts.attributes) {
            val occurrence = occurrences.getOrDefault(attribute.key, 0)
            occurrences[attribute.key] = occurrence + 1

            val oldAttr = oldGroups[attribute.key]?.getOrNull(occurrence)
            if (oldAttr == attribute) continue

            if (!attribute.reuseSupported) {
                return null
            } else {
                changedReusableMods.add(attribute)
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
