package com.huanli233.hibari.runtime

import android.os.Handler
import android.os.Looper
import com.huanli233.hibari.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

object SnapshotManager {
    private val stateToTunationsMap = mutableMapOf<Any, MutableSet<Tunation>>()
    private val tunationToStatesMap = mutableMapOf<Tunation, MutableSet<Any>>()

    val tuneSnapshots = mutableSetOf<Snapshot>()

    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        GlobalSnapshotManager.ensureStarted()
        Snapshot.registerApplyObserver { stateObjects, snapshot ->
            if (snapshot !in tuneSnapshots) {
                // Apply observers run on whichever thread applied the snapshot, while everything that
                // touches these maps otherwise runs on the main thread, where tuning happens. A write
                // that applied its own snapshot off-thread would otherwise reshape both dependency
                // maps while a tune was reading them, and `markChanged` would land in a set the next
                // tune is walking.
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    invalidateNow(stateObjects)
                } else {
                    // The notified set is a view over the snapshot's own modified collection, which is
                    // only guaranteed to stay put for the duration of the observer call, so it is
                    // copied rather than captured.
                    val changed = stateObjects.toMutableSet()
                    mainHandler.post { invalidateNow(changed) }
                }
            }
        }

    }

    private fun invalidateNow(stateObjects: Set<Any>) {
        val tunationsToInvalidate = mutableSetOf<Tunation>()
        stateObjects.forEach { stateObject ->
            stateToTunationsMap[stateObject]?.let { observers ->
                tunationsToInvalidate.addAll(observers)
                observers.forEach { it.markChanged(stateObject) }
            }
        }

        if (tunationsToInvalidate.isNotEmpty()) {
            tunationsToInvalidate.forEach {
                GlobalRetuner.retuner.scheduleRetune(it)
            }
        }
    }


    fun recordRead(tunation: Tunation, stateObject: Any) {
        stateToTunationsMap.getOrPut(stateObject) { mutableSetOf() }.add(tunation)
        tunationToStatesMap.getOrPut(tunation) { mutableSetOf() }.add(stateObject)
    }

    fun clearDependencies(tunation: Tunation) {
        tunationToStatesMap[tunation]?.forEach { stateObject ->
            stateToTunationsMap[stateObject]?.let { observers ->
                observers.remove(tunation)
                // Drop the key once it has no readers left, otherwise a disposed state that was
                // only ever read by this tunation stays strongly reachable (as a map key) forever.
                if (observers.isEmpty()) stateToTunationsMap.remove(stateObject)
            }
        }
        tunationToStatesMap.remove(tunation)
    }
}

internal object GlobalSnapshotManager {
    private val started = AtomicBoolean(false)

    fun ensureStarted() {
        if (started.compareAndSet(false, true)) {
            val channel = Channel<Unit>(Channel.CONFLATED)
            val scope = CoroutineScope(Dispatchers.Main.immediate)

            scope.launch {
                channel.consumeEach {
                    Snapshot.sendApplyNotifications()
                }
            }

            Snapshot.registerGlobalWriteObserver {
                channel.trySend(Unit)
            }
        }
    }
}