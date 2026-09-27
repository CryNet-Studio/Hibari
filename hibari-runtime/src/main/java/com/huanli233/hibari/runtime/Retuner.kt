package com.huanli233.hibari.runtime

import com.huanli233.hibari.ui.HibariFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext

class Retuner(
    private val coroutineContext: CoroutineContext,
    private val factories: List<HibariFactory>
) {

    private val scope = CoroutineScope(coroutineContext + SupervisorJob())
    private val invalidations = ConcurrentLinkedQueue<Tunation>()

    /**
     * The queue alone cannot answer "is this already coming?": `ConcurrentLinkedQueue.add` always
     * succeeds, so the guard in [scheduleRetune] was decorative and a session invalidated five times
     * in one frame queued five nodes. Membership lives here, and drains with the node.
     */
    private val queued = Collections.newSetFromMap(ConcurrentHashMap<Tunation, Boolean>())
    private var isRunning = false

    fun scheduleRetune(session: Tunation) {
        if (!session.isValid) return
        if (queued.add(session)) {
            invalidations.add(session)
            startRetuneLoop()
        }
    }

    fun tuneNow(session: Tunation) {
        TuneController.tune(session, factories)
    }

    private fun startRetuneLoop() {
        if (!isRunning) {
            isRunning = true
            scope.launch {
                runRetuneLoop()
            }
        }
    }

    private suspend fun runRetuneLoop() {
        while (isRunning) {
            withFrameNanos {
                // Tuning right here, in the frame callback, rather than from a coroutine launched
                // out of it: the launch only ever added a trip through the looper, and the cancel
                // that went with it could drop every session a batch had not reached yet.
                drainInvalidations().forEach { session ->
                    TuneController.tune(session, factories)
                }

                if (invalidations.isEmpty()) {
                    isRunning = false
                }
            }
        }
    }

    private fun drainInvalidations(): Set<Tunation> {
        val sessions = mutableSetOf<Tunation>()
        while (invalidations.isNotEmpty()) {
            invalidations.poll()?.let {
                queued.remove(it)
                // Disposed since it was queued: drop it rather than build a tuner for a dead view.
                if (it.isValid) sessions.add(it)
            }
        }
        return sessions
    }
}
