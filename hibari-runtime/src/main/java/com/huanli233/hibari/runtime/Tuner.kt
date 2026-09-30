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
    scratch: ArrayList<String>,
) {
    if (owned.isEmpty()) {
        touched.clear()
        return
    }

    scratch.clear()
    for ((path, value) in owned) {
        if (path !in touched) scratch.add(path)
    }

    for (index in scratch.indices) {
        val path = scratch[index]
        val value = owned.remove(path)
        if (memory[path] === value) {
            memory.remove(path)
            (value as? Pair<*, *>)?.let { pair ->
                (pair.second as? RememberObserver)?.onForgotten()
            }
        }
    }

    touched.clear()
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

    private val nodeStack = ArrayDeque<MutableList<Node>>()
    val rootNodes: List<Node>
        get() = nodeStack.firstOrNull() ?: emptyList()

    val walker = Walker()
    var memory = tunation.tuneData?.memory?.toMutableMap() ?: hashMapOf()
    val localValueStacks = tunation.tuneData?.localValueStack ?: tunationLocalHashMapOf()

    /** The slots written by this tuner, with the value it wrote, so a shared map stays prunable. */
    private val ownedSlots = HashMap<String, Any?>()
    private val touchedSlots = HashSet<String>()
    private val forgottenScratch = ArrayList<String>()

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
        nodeStack.addFirst(mutableListOf())
    }

    fun endComposition(): List<Node> {
        if (HibariLog.enabled) {
            val nodeCount = nodeStack.firstOrNull()?.size ?: 0
            HibariLog.i(TAG) { "======== END COMPOSITION (Root nodes: $nodeCount) ========" }
        }
        if (nodeStack.size != 1) {
            hibariRuntimeError("Composition stack imbalance. Mismatched start/end calls.")
        }
        forgetUntouchedSlots(memory, ownedSlots, touchedSlots, forgottenScratch)
        census?.let {
            TuneStats.recordGroups(it.groupsSeen(), it.cleanGroupCount())
            census = null
        }
        roundChangedStates = emptySet()
        return nodeStack.removeFirst()
    }

    fun rememberedValue(): Any? {
        val path = walker.path()
        touchedSlots.add(path)
        return memory[path]
    }

    fun updateRememberedValue(value: Any?) {
        val path = walker.path()
        touchedSlots.add(path)
        ownedSlots[path] = value
        val oldValue = memory.put(path, value)
        if (oldValue != null && oldValue != value) {
            val oldRemembered = (oldValue as? Pair<*, *>)?.second
            (oldRemembered as? RememberObserver)?.onForgotten()
        }
        val newRemembered = (value as? Pair<*, *>)?.second
        (newRemembered as? RememberObserver)?.onRemembered()
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
        SnapshotManager.clearDependencies(tunation)
        val snapshot = Snapshot.takeMutableSnapshot(
            readObserver = {
                onReadState(it)
                census?.noteRead(walker.path(), it)
            }
        )
        snapshot.enter {
            runTunable(tunation.content)
        }
        snapshot.apply()
    }

    @Tunable
    fun emitNode(node: Node, content: @Tunable () -> Unit = {}): Node {
        val path = walker.path()
        node.key = path
        TuneStats.markNode(path)
        HibariLog.d(TAG) { "emitNode() at path '${node.key}'. Node: $node" }
        nodeStack.addFirst(mutableListOf())
        runTunable(content)
        val children = nodeStack.removeFirst()
        node.children = children
        nodeStack.firstOrNull()?.add(node) ?: hibariRuntimeError("Cannot emit node outside of a composition.")
        return node
    }
}