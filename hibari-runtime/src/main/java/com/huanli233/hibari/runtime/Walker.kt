package com.huanli233.hibari.runtime

class Walker {

    private val ends = ArrayDeque<Int>()

    /**
     * The joined path is asked for twice per `remember` slot on every retune (once to read the slot,
     * once to write it back), and each build copies all group keys, so it is cached until the
     * position actually changes.
     */
    private var cachedPath: String? = null

    fun path(): String {
        cachedPath?.let { return it }
        return ends.joinToString("-").also { cachedPath = it }
    }

    fun start(endKey: Int) {
        ends.addLast(endKey)
        cachedPath = null
    }

    fun end() {
        ends.removeLast()
        cachedPath = null
    }

    fun clear() {
        ends.clear()
        cachedPath = null
    }

}