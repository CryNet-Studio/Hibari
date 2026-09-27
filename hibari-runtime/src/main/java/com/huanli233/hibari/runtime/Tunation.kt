package com.huanli233.hibari.runtime

import android.view.ViewGroup
import com.huanli233.hibari.ui.node.Node

class Tunation(
    val hostView: ViewGroup,
    var content: @Tunable () -> Unit,
    var tuner: Tuner? = null
) {

    constructor(
        hostView: ViewGroup,
        content: @Tunable () -> Unit,
        tuner: Tuner? = null,
        parent: Tunation
    ) : this(hostView, content, tuner) {
        this.tuneData = parent.tuneData
    }

    var lastTree: List<Node> = emptyList()
    var tuneData: TuneData? = null

    /**
     * The patcher only depends on the host view and the factories, both fixed for a session's life,
     * so it is built once instead of per tune — a tune otherwise costs a renderer, a patcher, a
     * copied factory list and an inflater wrapper.
     */
    internal var patcher: Patcher? = null

    fun dispose() {
        SnapshotManager.clearDependencies(this)
        tuner?.dispose()
        tuner = null
        patcher = null
    }
}