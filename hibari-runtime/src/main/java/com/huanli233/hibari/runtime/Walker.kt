package com.huanli233.hibari.runtime

class Walker {

    /**
     * One position of the walk, remembered for the life of the walker.
     *
     * A position is reached by the same route in every round, so its path string is built the first
     * time it is asked for and handed out from then on: a round that touches four hundred slots builds
     * no string at all, and the string stays the same object, so the slot maps keep the hash its first
     * round paid for instead of re-hashing a fresh copy of the same path every round.
     */
    private class Node(
        val parent: Node?,
        val key: Int,
        val occurrence: Int
    ) {
        /** Built on the first ask, because a position that is never read should not name itself. */
        var path: String? = null

        /** The positions taken directly under this one, by group key. Usually only a handful. */
        val children = ArrayList<Child>()

        /** Stamped when this node is entered, which is how its siblings start counting again. */
        var epoch = 0L
    }

    /** Every sibling a group key has produced under one position, and which number is next. */
    private class Child(val key: Int) {
        val nodes = ArrayList<Node>()
        var epoch = 0L
        var count = 0
    }

    private val root = Node(parent = null, key = 0, occurrence = 0).apply { path = "" }
    private var cursor = root

    /**
     * Increases with every entry, so an out-of-date sibling count is recognised without walking the
     * remembered positions to clear them. A [Long] because a session that wraps an [Int] here would
     * meet an old stamp again and keep counting the previous round's siblings into the new one.
     */
    private var epoch = 0L

    /**
     * The joined path is the slot key for every `remember` and the node key for every emitted node, so
     * it is asked several times per position.
     */
    fun path(): String = cursor.path ?: buildPath(cursor)

    fun start(endKey: Int) {
        val parent = cursor
        val child = childFor(parent, endKey)
        if (child.epoch != parent.epoch) {
            // The position above was entered afresh - a new round, or the next turn of the loop that
            // holds it - so this key's siblings start counting again.
            child.epoch = parent.epoch
            child.count = 0
        }
        child.count++

        val occurrence = child.count
        val next = child.nodes.getOrNull(occurrence - 1)
            ?: Node(parent, endKey, occurrence).also { child.nodes.add(it) }

        cursor = next
        next.epoch = ++epoch
    }

    fun end() {
        val parent = cursor.parent
        if (parent == null) {
            hibariRuntimeError("end() without a matching start(): the group calls are unbalanced.")
        }
        cursor = parent
    }

    fun clear() {
        cursor = root
        root.epoch = ++epoch
    }

    private fun buildPath(node: Node): String {
        val parent = node.parent
        val built = if (parent == null) {
            ""
        } else {
            val segment = if (node.occurrence > 1) "${node.key}#${node.occurrence}" else "${node.key}"
            val prefix = buildPath(parent)
            if (prefix.isEmpty()) segment else "$prefix-$segment"
        }
        node.path = built
        return built
    }

    private fun childFor(parent: Node, key: Int): Child {
        val children = parent.children
        for (index in children.indices) {
            val child = children[index]
            if (child.key == key) return child
        }
        return Child(key).also { children.add(it) }
    }
}
