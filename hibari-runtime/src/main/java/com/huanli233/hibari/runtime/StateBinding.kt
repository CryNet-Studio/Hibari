package com.huanli233.hibari.runtime

import android.view.View
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.thenViewAttribute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

/**
 * Writes a [State] into the view as it changes, without composition ever reading it.
 *
 * Reading a state inside a tunable registers a dependency for the whole host view, so one animated
 * float re-tunes the entire tree every frame: every tunable re-runs and every node, attribute and
 * modifier chain is rebuilt, and the diff then decides that only one view property moved. A binding
 * takes the state as a value instead, subscribes to it, and writes the view directly when it changes,
 * so nothing is recomposed for that animation.
 *
 * [key] identifies the binding and should be the [com.huanli233.hibari.ui.uniqueKey] of the call
 * site, the same way the built-in attributes key themselves; two bindings sharing a key on one view
 * collapse to the last one, exactly as two attributes with one key do.
 *
 * The attribute's value is the [State] instance rather than its contents, which keeps the binding
 * stable across reconfigures: the diff sees an unchanged value and leaves the subscription alone,
 * while a lambda-valued attribute would be rebuilt and re-applied every tune.
 */
fun <T> Modifier.bindState(
    key: Any,
    state: State<T>,
    write: View.(T) -> Unit
): Modifier = thenViewAttribute<View, State<T>>(key, state) { bound ->
    val view = this
    view.stateBindings().bind(key, bound) { value ->
        @Suppress("UNCHECKED_CAST")
        view.write(value as T)
    }
}

private fun View.stateBindings(): ViewStateBindings =
    (getTag(R.id.hibari_state_bindings) as? ViewStateBindings) ?: ViewStateBindings(this)
        .also { setTag(R.id.hibari_state_bindings, it) }

/**
 * The bindings of one view. Subscriptions run only while the view is attached, so a recycled item in
 * a lazy list stops and restarts with the view and cannot keep a detached view reachable through a
 * live coroutine.
 */
private class ViewStateBindings(private val view: View) {

    private val bindings = HashMap<Any, Pair<State<Any?>, (Any?) -> Unit>>()
    private val jobs = HashMap<Any, Job>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = startAll()
            override fun onViewDetachedFromWindow(v: View) = stopAll()
        })
    }

    fun bind(key: Any, state: State<Any?>, write: (Any?) -> Unit) {
        // Re-binding the same key replaces the pair rather than adding a second writer, so swapping
        // the bound state does not leave the previous one writing into the view.
        bindings[key]?.let { if (it.first === state) return }
        bindings[key] = state to write
        jobs.remove(key)?.cancel()
        if (view.isAttachedToWindow) start(key, state, write)
    }

    private fun startAll() {
        bindings.forEach { (key, pair) ->
            if (jobs[key]?.isActive != true) start(key, pair.first, pair.second)
        }
    }

    private fun start(key: Any, state: State<Any?>, write: (Any?) -> Unit) {
        jobs[key] = scope.launch {
            snapshotFlow { state.value }.collect(write)
        }
    }

    private fun stopAll() {
        jobs.clear()
        scope.coroutineContext.cancelChildren()
    }
}
