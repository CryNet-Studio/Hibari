package com.huanli233.hibari.runtime

class Walker {

    /**
     * How many times each group key has been entered at one level. The compiler hands every call site
     * a constant key, so a loop body reports the same key once per iteration and the siblings have to
     * be told apart here — otherwise every iteration shares one `remember` slot and one node key.
     */
    private class Level {
        private var keys = IntArray(4)
        private var counts = IntArray(4)
        private var size = 0

        fun reset() {
            size = 0
        }

        /** Records one entry of [key] and returns which occurrence of it this was. */
        fun enter(key: Int): Int {
            for (index in 0 until size) {
                if (keys[index] == key) return ++counts[index]
            }
            if (size == keys.size) {
                keys = keys.copyOf(size * 2)
                counts = counts.copyOf(size * 2)
            }
            keys[size] = key
            counts[size] = 1
            size++
            return 1
        }
    }

    private var keys = IntArray(8)
    private var occurrences = IntArray(8)
    private var depth = 0

    /** One [Level] per depth, allocated up to the deepest walk ever taken and reused from there. */
    private val levels = ArrayList<Level>()

    /**
     * The joined path is the slot key for every `remember` and the node key for every emitted node,
     * so it is asked several times per position and cached until the walk actually moves.
     */
    private var cachedPath: String? = null

    fun path(): String {
        cachedPath?.let { return it }

        val builder = StringBuilder(depth * 4)
        for (index in 0 until depth) {
            if (index > 0) builder.append('-')
            builder.append(keys[index])
            if (occurrences[index] > 1) {
                builder.append('#').append(occurrences[index])
            }
        }
        return builder.toString().also { cachedPath = it }
    }

    fun start(endKey: Int) {
        while (levels.size <= depth) levels.add(Level())
        val occurrence = levels[depth].enter(endKey)

        // The group being entered has not emitted children yet, and this level may still be holding
        // the counts of whichever sibling occupied this depth last.
        levels.getOrNull(depth + 1)?.reset()

        if (depth == keys.size) {
            keys = keys.copyOf(depth * 2)
            occurrences = occurrences.copyOf(depth * 2)
        }
        keys[depth] = endKey
        occurrences[depth] = occurrence
        depth++
        cachedPath = null
    }

    fun end() {
        if (depth == 0) {
            hibariRuntimeError("end() without a matching start(): the group calls are unbalanced.")
        }
        depth--
        cachedPath = null
    }

    fun clear() {
        depth = 0
        for (level in levels) level.reset()
        cachedPath = null
    }
}
