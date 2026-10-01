package com.huanli233.hibari.runtime

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import androidx.core.view.ViewCompat
import com.huanli233.hibari.runtime.snapshots.Snapshot
import com.huanli233.hibari.ui.node.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.reflect.Method

fun hibariRuntimeError(message: String, cause: Throwable? = null): Nothing = throw HibariRuntimeError(message, cause)

class HibariRuntimeError(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Releases the `remember` slots this tuner owned in an earlier composition but neither read nor wrote
 * in the one that just finished — the slots of a conditional branch that stopped rendering. Until now
 * those entries stayed in [memory] with their observers never forgotten, which is how a
 * `LaunchedEffect` behind an `if` kept its coroutine after the `if` went false, and how a finished
 * `Transition` kept pumping frames (and recomposing its host) forever.
 *
 * An entry is only released while the value this tuner wrote is still the one in [memory]: a
 * sub-tunation shares the map, and a slot another tuner has since taken over is not ours to drop.
 */
internal fun forgetUntouchedSlots(
    memory: MutableMap<String, Any?>,
    owned: HashMap<String, Any?>,
    touched: HashSet<String>,
) {
    // One pass over the table with its own iterator: the paths to drop used to be collected into a
    // scratch list and removed one by one afterwards, which re-hashed every one of them and walked
    // the table twice to release a handful.
    val entries = owned.entries.iterator()
    while (entries.hasNext()) {
        val entry = entries.next()
        val path = entry.key
        if (path in touched) continue
        val value = entry.value
        entries.remove()
        if (memory[path] === value) {
            memory.remove(path)
            (value as? Pair<*, *>)?.let { pair ->
                (pair.second as? RememberObserver)?.onForgotten()
            }
        }
    }

    touched.clear()
}

/**
 * Writes one `remember` slot and owns the observer lifecycle that goes with it: the value the slot
 * gave up is forgotten, the value it takes is remembered, once each. `Tunables.cache` used to run the
 * same pair around this call, which doubled both - a `DisposableEffect` ran its body twice and kept
 * only the second cleanup handle, so the first subscription could never be released.
 */
internal fun putRememberedSlot(
    memory: MutableMap<String, Any?>,
    owned: HashMap<String, Any?>,
    touched: HashSet<String>,
    path: String,
    value: Any?,
) {
    touched.add(path)
    owned[path] = value
    val oldValue = memory.put(path, value)
    if (oldValue != null && oldValue != value) {
        val oldRemembered = (oldValue as? Pair<*, *>)?.second
        (oldRemembered as? RememberObserver)?.onForgotten()
    }
    val newRemembered = (value as? Pair<*, *>)?.second
    (newRemembered as? RememberObserver)?.onRemembered()
}

val hibariViewId = R.id.hibari_view_tag

val subcomposeLayoutId = R.id.sub_compose_layout

val paddingPartsTagId = R.id.hibari_padding_parts

/**
 * The hidden `View.setKeyedTag` is looked up once instead of once per tagged write: every view
 * creation writes two keyed tags, and listener-carrying attributes are re-applied on every
 * reconfigure, so the lookup was a per-frame reflection cost.
 */
private val setKeyedTagMethod: Method? by lazy {
    runCatching {
        val viewClass = View::class.java
        val method = if (Build.VERSION.SDK_INT >= 28) {
            HiddenApiBypass.getDeclaredMethod(viewClass, "setKeyedTag", Int::class.java, Any::class.java)
        } else {
            viewClass.getDeclaredMethod("setKeyedTag", Int::class.java, Any::class.java)
        }
        method.isAccessible = true
        method
    }.getOrElse {
        HibariLog.e("HibariTuner") { "Keyed tags are unavailable, view lookups by node key will fail: ${it.message}" }
        null
    }
}

@SuppressLint("PrivateApi")
fun invokeSetKeyedTag(view: View, key: Int, tag: Any?) {
    val method = setKeyedTagMethod ?: return
    try {
        method.invoke(view, key, tag)
    } catch (e: Exception) {
        HibariLog.e("HibariTuner", { "Error invoking setKeyedTag: ${e.message}" }, e)
    }
}

class ProvidedValue<T> internal constructor(
    /**
     * The composition local that is provided by this value. This is the left-hand side of the
     * [ProvidableTunationLocal.provides] infix operator.
     */
    val tunationLocal: TunationLocal<T>,
    value: T?,
    private val explicitNull: Boolean,
    internal val mutationPolicy: SnapshotMutationPolicy<T>?,
    internal val state: MutableState<T>?,
    internal val compute: (TunationLocalAccessorScope.() -> T)?,
    internal val isDynamic: Boolean
) {
    private val providedValue: T? = value
    @Suppress("UNCHECKED_CAST")
    val value: T get() = providedValue as T
    @get:JvmName("getCanOverride")
    var canOverride: Boolean = true
        private set
    @Suppress("UNCHECKED_CAST")
    internal val effectiveValue: T
        get() = when {
            explicitNull -> null as T
            state != null -> state.value
            providedValue != null -> providedValue
            else -> hibariRuntimeError("Unexpected form of a provided value")
        }
    internal val isStatic get() = (explicitNull || value != null) && !isDynamic
    internal fun ifNotAlreadyProvided() = this.also { canOverride = false }
}

data class TuneData(
    val localValueStack: TunationLocalsHashMap,
    val memory: Map<String, Any?>,
)

open class Tuner(
    val tunation: Tunation,
    val onReadState: (Any) -> Unit = {
        SnapshotManager.recordRead(tunation, it)
    }
) {
    private val TAG = "HibariTuner"

    /**
     * A scope with no [Job] in its context cannot be cancelled at all — `cancel()` on it is a silent
     * no-op — so every effect that outlives disposal kept running.
     */
    val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    /**
     * One slot per level being built, the composition's root at index 0 and the level being emitted
     * last. A slot stays empty until that level is handed its first child: a leaf then costs no list
     * at all, and a level with two children costs a list sized for two instead of the ten a fresh
     * ArrayList fills on its first add.
     */
    private val nodeStack = ArrayList<MutableList<Node>?>()

    val rootNodes: List<Node>
        get() = nodeStack.firstOrNull() ?: emptyList()

    val walker = Walker()
    var memory = tunation.tuneData?.memory?.toMutableMap() ?: hashMapOf()
    val localValueStacks = tunation.tuneData?.localValueStack ?: tunationLocalHashMapOf()

    /** A sub-tunation shares its parent's stacks, so only the owner may drain them. */
    private val ownsLocalValueStacks = tunation.tuneData?.localValueStack == null

    /** The slots written by this tuner, with the value it wrote, so a shared map stays prunable. */
    private val ownedSlots = HashMap<String, Any?>()
    private val touchedSlots = HashSet<String>()

    /** The writes that woke the round about to run; empty when the round was forced, not woken. */
    internal var roundChangedStates: Set<Any> = emptySet()

    private var census: GroupCensus? = null

    fun getTuneData(): TuneData {
        return TuneData(localValueStacks, memory)
    }

    fun dispose() {
        for ((path, value) in ownedSlots) {
            if (memory[path] === value) memory.remove(path)
            (value as? Pair<*, *>)?.let { pair ->
                (pair.second as? RememberObserver)?.onForgotten()
            }
        }
        ownedSlots.clear()
        touchedSlots.clear()
        memory.clear()
        coroutineScope.cancel()
    }

    fun startGroup(key: Int) {
        walker.start(key)
        census?.noteGroup(walker.path())
    }

    fun endGroup(key: Int) {
        walker.end()
    }

    fun startComposition() {
        HibariLog.i(TAG) { "======== START COMPOSITION ========" }
        walker.clear()
        // A round with no known writes (a bind, an attach, a first tune) cannot say anything about
        // what was skippable, so it is left out of the census rather than counted as all clean.
        census = if (TuneStats.enabled && roundChangedStates.isNotEmpty()) {
            GroupCensus(roundChangedStates)
        } else {
            null
        }
        // Drained before use rather than assumed empty: a round that threw left its root slot on the
        // stack, and every round after it would have ended with `size != 1` and reported an imbalance
        // instead of tuning.
        nodeStack.clear()
        nodeStack.add(null)
    }

    fun endComposition(): List<Node> {
        if (HibariLog.enabled) {
            val nodeCount = nodeStack.firstOrNull()?.size ?: 0
            HibariLog.i(TAG) { "======== END COMPOSITION (Root nodes: $nodeCount) ========" }
        }
        if (nodeStack.size != 1) {
            hibariRuntimeError("Composition stack imbalance. Mismatched start/end calls.")
        }
        forgetUntouchedSlots(memory, ownedSlots, touchedSlots)
        census?.let {
            TuneStats.recordGroups(it.groupsSeen(), it.cleanGroupCount())
            census = null
        }
        roundChangedStates = emptySet()
        return nodeStack.removeAt(0) ?: emptyList()
    }

    fun rememberedValue(): Any? {
        val path = walker.path()
        touchedSlots.add(path)
        return memory[path]
    }

    fun updateRememberedValue(value: Any?) {
        putRememberedSlot(memory, ownedSlots, touchedSlots, walker.path(), value)
    }

    @Suppress("UNCHECKED_CAST")
    fun startProviders(values: Array<out ProvidedValue<*>>) {
        values.forEach { providedValue ->
            val local = providedValue.tunationLocal as ProvidableTunationLocal<Any?>
            val stack = localValueStacks.getOrPut(local) { ArrayDeque() }
            val previousHolder = stack.firstOrNull()

            val newHolder = local.updatedStateOf(
                providedValue as ProvidedValue<Any?>,
                previousHolder as? ValueHolder<Any?>
            )
            stack.addFirst(newHolder)
        }
    }

    fun endProviders(values: Array<out ProvidedValue<*>>) {
        values.forEach { providedValue ->
            val stack = localValueStacks[providedValue.tunationLocal as TunationLocal<Any?>]
            stack?.removeFirstOrNull()
        }
    }

    fun <T> consume(tunationLocal: TunationLocal<T>): T {
        return with(localValueStacks) { tunationLocal.currentValue }
    }

    fun <T> runTunable(content: @Tunable () -> T) = content()

    fun runTunable(tunation: Tunation) {
        HibariLog.i(TAG) { ">>>>>> runTunable for [$tunation] <<<<<<" }
        walker.clear()
        // A tune that threw halfway left every provider it started still on its stack, and the next
        // tune would have read the value that dead tune put there. They are always empty by the time
        // a tune starts, so draining them costs nothing on the normal path.
        if (ownsLocalValueStacks) localValueStacks.values.forEach { it.clear() }
        // A round that threw also left the paths it had already marked as touched behind, and the next
        // round's sweep reads that set as this round's record: the slot belonging to whatever the crash
        // removed would look touched, keep its value, and never be forgotten. Empty on the normal path,
        // where the sweep at the end of the round drains it.
        touchedSlots.clear()
        SnapshotManager.clearDependencies(tunation)
        val snapshot = Snapshot.takeMutableSnapshot(
            readObserver = {
                onReadState(it)
                census?.noteRead(walker.path(), it)
            }
        )
        val applyResult = try {
            snapshot.enter {
                runTunable(tunation.content)
            }
            snapshot.apply()
        } finally {
            // An open snapshot keeps this tunation alive through its read observer, and apply() does
            // not close the snapshot when it fails, so every path out has to dispose it.
            snapshot.dispose()
        }
        if (!applyResult.succeeded) {
            // The writes this tune made were dropped, so the view still shows the previous tune.
            // Something else wrote the same state from another thread; ask for another pass, which is
            // also what the previous code forgot to do, leaving the host quiet until the next change.
            HibariLog.e(TAG) { "Snapshot apply failed for [$tunation], scheduling another tune" }
            GlobalRetuner.retuner.scheduleRetune(tunation)
        }
    }

    @Tunable
    fun emitNode(node: Node, content: @Tunable () -> Unit = {}): Node {
        val path = walker.path()
        node.key = path
        TuneStats.markNode(path)
        HibariLog.d(TAG) { "emitNode() at path '${node.key}'. Node: $node" }
        nodeStack.add(null)
        runTunable(content)
        val children = nodeStack.removeAt(nodeStack.lastIndex)
        node.children = children ?: emptyList()
        val parentIndex = nodeStack.lastIndex
        if (parentIndex < 0) hibariRuntimeError("Cannot emit node outside of a composition.")
        val siblings = nodeStack[parentIndex]
        if (siblings == null) {
            nodeStack[parentIndex] = ArrayList<Node>(2).apply { add(node) }
        } else {
            siblings.add(node)
        }
        return node
    }
}