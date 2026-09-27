package com.huanli233.hibari.runtime

import android.os.Build
import android.util.Log
import java.util.Locale

/**
 * Counters for what one tune actually costs: how long composition ran, how long the patch ran, how
 * many objects the process allocated in between, and how much view work the diff avoided.
 *
 * Everything is gated on [enabled] — with it off a call site is one static read plus a branch. When
 * it is on, every [reportInterval] tunes emits a [Log.i] line covering the window since the last
 * one, so logcat shows a rolling average rather than a lifetime number a warm-up frame gets buried in.
 *
 * The accumulators are plain fields on purpose: a tune only ever runs on the main thread.
 */
object TuneStats {

    const val TAG = "HibariStats"
    const val reportInterval = 60L

    @Volatile
    var enabled: Boolean = false

    var tunes = 0L
        private set
    var compositionNanos = 0L
        private set
    var patchNanos = 0L
        private set
    var allocations = 0L
        private set

    /** False while the platform allocation counter is unavailable, so a gap never reads as zero. */
    var allocCounterAvailable = false
        private set

    var nodesEmitted = 0L
        private set
    var viewsCreated = 0L
        private set
    var attributeWrites = 0L
        private set
    var positionalPatches = 0L
        private set
    var myersPatches = 0L
        private set
    var slotsChanged = 0L
        private set

    private fun globalAllocCount(): Long {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return -1L
        // The platform counter is an int here, so widen it explicitly rather than letting the two
        // numeric types meet inside runCatching.
        val sampled: Result<Long> = runCatching { android.os.Debug.getGlobalAllocCount().toLong() }
        return sampled.getOrDefault(-1L)
    }

    /** A timestamp to subtract from a later call; 0 keeps the arithmetic harmless when disabled. */
    internal fun now(): Long = if (enabled) System.nanoTime() else 0L

    internal fun allocNow(): Long = if (enabled) globalAllocCount() else -1L

    internal fun recordTune(compositionNanos: Long, patchNanos: Long, allocsBefore: Long, allocsAfter: Long) {
        if (!enabled) return
        tunes++
        this.compositionNanos += compositionNanos
        this.patchNanos += patchNanos
        // The platform counter is an int, so a wrap around 2^31 shows up as a negative delta: drop
        // that window's sample instead of reporting a nonsense allocation count.
        if (allocsBefore >= 0 && allocsAfter >= allocsBefore) {
            allocations += allocsAfter - allocsBefore
            allocCounterAvailable = true
        }
        if (tunes % reportInterval == 0L) {
            Log.i(TAG, report())
            startWindow()
        }
    }

    internal fun markNode() {
        if (enabled) nodesEmitted++
    }

    internal fun markViewCreated() {
        if (enabled) viewsCreated++
    }

    internal fun addAttributeWrites(count: Int) {
        if (enabled) attributeWrites += count
    }

    internal fun markPositionalPatch(changedSlots: Int) {
        if (!enabled) return
        positionalPatches++
        slotsChanged += changedSlots
    }

    internal fun markMyersPatch() {
        if (enabled) myersPatches++
    }

    /** Empties the current window. Read the fields first if lifetime numbers are what is wanted. */
    fun startWindow() {
        tunes = 0
        compositionNanos = 0
        patchNanos = 0
        allocations = 0
        allocCounterAvailable = false
        nodesEmitted = 0
        viewsCreated = 0
        attributeWrites = 0
        positionalPatches = 0
        myersPatches = 0
        slotsChanged = 0
    }

    fun report(): String {
        if (tunes == 0L) return "HibariStats: no tunes in this window."
        val allocPerTune = if (allocCounterAvailable) (allocations / tunes).toString() else "n/a"
        return "HibariStats window=" + tunes + " tunes | " +
                "composition=" + ms(compositionNanos / tunes) + "ms " +
                "patch=" + ms(patchNanos / tunes) + "ms " +
                "alloc/tune=" + allocPerTune + " | " +
                "nodes/tune=" + (nodesEmitted / tunes) + " " +
                "views/tune=" + (viewsCreated / tunes) + " " +
                "attrWrites=" + attributeWrites + " " +
                "positional=" + positionalPatches + " myers=" + myersPatches +
                " slotsChanged=" + slotsChanged
    }

    private fun ms(nanos: Long): String = String.format(Locale.US, "%.2f", nanos / 1_000_000.0)
}
