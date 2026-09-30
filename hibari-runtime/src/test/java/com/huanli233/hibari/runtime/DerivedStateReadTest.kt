package com.huanli233.hibari.runtime

import com.huanli233.hibari.runtime.snapshots.Snapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A derived state is only as fresh as the reads it reports: the round that asked for one has to end up
 * subscribed to the states its calculation read, or a later write to one of them wakes nothing. Both
 * paths are pinned here - the one that computes the value and the one that answers from the cache and
 * replays the dependencies it recorded.
 */
class DerivedStateReadTest {

    @Test
    fun `the sources read while computing a derived value reach the snapshot observer`() {
        val source = mutableStateOf(1)
        val derived = derivedStateOf { source.value * 2 }
        val observed = mutableListOf<Any>()

        assertEquals(2, readInSnapshot(derived, observed))

        assertTrue(
            "the round that computes a derived value must subscribe to its sources, saw $observed",
            observed.any { it === source }
        )
    }

    @Test
    fun `a derived value recomputed after a write reports the source again`() {
        val source = mutableStateOf(1)
        val derived = derivedStateOf { source.value * 2 }

        val first = mutableListOf<Any>()
        assertEquals(2, readInSnapshot(derived, first))

        source.value = 5

        val second = mutableListOf<Any>()
        assertEquals(10, readInSnapshot(derived, second))

        assertTrue(second.any { it === source })
    }

    @Test
    fun `a derived value read twice in one round reports the source both times`() {
        val source = mutableStateOf(1)
        val derived = derivedStateOf { source.value * 2 }
        val observed = mutableListOf<Any>()

        val snapshot = Snapshot.takeMutableSnapshot(readObserver = { observed += it })
        try {
            snapshot.enter {
                assertEquals(2, derived.value)
                assertEquals(2, derived.value)
            }
        } finally {
            snapshot.dispose()
        }

        // The second read answers from the cache, which has to replay the dependencies the first one
        // collected; a round that only subscribed on the computing path would miss every host that
        // reads the same value twice.
        assertEquals(2, observed.count { it === source })
    }

    private fun <T> readInSnapshot(state: State<T>, into: MutableList<Any>): T {
        val snapshot = Snapshot.takeMutableSnapshot(readObserver = { into += it })
        try {
            return snapshot.enter { state.value }
        } finally {
            snapshot.dispose()
        }
    }
}
