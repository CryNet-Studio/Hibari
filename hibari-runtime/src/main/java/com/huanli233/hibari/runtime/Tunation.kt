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
        // The locals are what the parent has in scope for this content, so they are shared. Its
        // remembered values are not: a sub-session walks paths from its own root, and one map for
        // both lets a sub-session's first write land on a live slot of the parent and forget it.
        parent.tuneData?.let { tuneData = TuneData(it.localValueStack, emptyMap()) }
    }

    var lastTree: List<Node> = emptyList()
    var tuneData: TuneData? = null

    /**
     * The patcher only depends on the host view and the factories, both fixed for a session's life,
     * so it is built once instead of per tune — a tune otherwise costs a renderer, a patcher, a
     * copied factory list and an inflater wrapper.
     */
    internal var patcher: Patcher? = null

    /**
     * False from `dispose` until the host attaches again. A session that was already queued when it
     * got disposed would otherwise be revived a frame later — a fresh tuner, fresh effects and its
     * state reads re-registered against a view that is gone for good.
     */
    var isValid = true
        internal set

    fun dispose() {
        isValid = false
        SnapshotManager.clearDependencies(this)
        tuner?.dispose()
        tuner = null
        patcher = null
    }
}