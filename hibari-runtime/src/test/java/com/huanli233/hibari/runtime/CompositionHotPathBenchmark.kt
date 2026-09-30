package com.huanli233.hibari.runtime

import android.view.View
import com.huanli233.hibari.ui.AttributeApplier
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.ViewAttribute
import com.huanli233.hibari.ui.ViewClassAttribute
import com.huanli233.hibari.ui.node.Node
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The runtime pieces a round actually pays for, measured at a scale a real host reaches, so the
 * optimising arguments are numbers rather than my reading of the code.
 *
 * Two things make a number here worth acting on. The shapes are the ones production has: a group key
 * comes out of a seeded counter, so a real slot path is three ten-digit numbers joined and a round
 * takes hundreds of them. And each case is timed over several windows with the best kept, because one
 * window on one JVM run moved by half either way - enough to "measure" any change I had wanted to
 * believe in.
 */
class CompositionHotPathBenchmark {

    private object Applier : AttributeApplier<View, Any> {
        override fun apply(target: View, value: Any) = Unit
    }

    private val outerKeys = IntArray(8) { 1_770_000_000 + it }
    private val middleKeys = IntArray(5) { 1_770_100_000 + it }
    private val leafKeys = IntArray(20) { 1_770_200_000 + it }

    /** µs per round, best of three windows, printed so the number outlives the assertion. */
    private fun measure(label: String, rounds: Int, ceilingMicros: Long, body: () -> Unit): Long {
        repeat(rounds / 2 + 1) { body() }

        var best = Long.MAX_VALUE
        repeat(3) {
            val beganAt = System.nanoTime()
            repeat(rounds) { body() }
            val micros = (System.nanoTime() - beganAt) / rounds / 1000
            if (micros < best) best = micros
        }
        println("HibariBench  $label: ${best}us per round over $rounds rounds")
        assertTrue("$label took ${best}us per round", best < ceilingMicros)
        return best
    }

    private fun leaf(key: String, value: Any) = Node(
        modifier = Modifier
            .then(ViewClassAttribute(View::class.java))
            .then(ViewAttribute("alpha", Applier, value)),
        key = key
    )

    /** A round that walks 40 groups three levels deep and asks for the slot path of each. */
    @Test
    fun `walker path bookkeeping`() {
        val walker = Walker()

        measure("walker 40 groups x depth 3", 5_000, ceilingMicros = 400) {
            walker.clear()
            for (index in 0 until 40) {
                walker.start(outerKeys[index and 7])
                walker.start(middleKeys[index % 5])
                walker.start(leafKeys[index % 20])
                walker.path()
                walker.end()
                walker.end()
                walker.end()
            }
        }
    }

    /**
     * What a round really does with a path: take it, then use it as the key of a slot lookup, at the
     * same four hundred positions, over and over. The walk by itself is cheap; what a round pays is a
     * string per position and the hash of a string the slot maps have never seen.
     */
    @Test
    fun `slot path use over repeated rounds`() {
        val walker = Walker()
        val memory = HashMap<String, Any?>()
        val touched = HashSet<String>()
        val slotValue = Any()

        fun walk() {
            walker.clear()
            for (outer in outerKeys) {
                walker.start(outer)
                for (middle in middleKeys) {
                    walker.start(middle)
                    for (index in 0 until 10) {
                        walker.start(leafKeys[index])
                        val path = walker.path()
                        touched.add(path)
                        memory[path] = slotValue
                        walker.end()
                    }
                    walker.end()
                }
                walker.end()
            }
            touched.clear()
        }

        // Two rounds fill the maps, so every measured round pays lookups rather than insertions.
        walk()
        walk()

        measure("400 deep slot paths walked and looked up", 2_000, ceilingMicros = 800) {
            walk()
        }
    }

    /** What a round spends rebuilding and re-comparing modifier chains. */
    @Test
    fun `modifier chain rebuild compare and flatten`() {
        val first = leaf("0", 1f)

        measure("chain build + equals + flatten x 40", 3_000, ceilingMicros = 400) {
            for (index in 0 until 40) {
                val chain = Modifier
                    .then(ViewClassAttribute(View::class.java))
                    .then(ViewAttribute("alpha", Applier, 1f))
                    .then(ViewAttribute("scale", Applier, 1f))
                // The diff's first question for every node in every round.
                chain == first.modifier
                Node(chain).flattened
            }
        }
    }

    /** The positional fast path over a long list with one changed child - the animation case. */
    @Test
    fun `diff of one changed leaf in five hundred`() {
        val count = 500
        val oldList = (0 until count).map { leaf("$it", 1f) }
        val changed = oldList.toMutableList().also { it[250] = leaf("250", 0.5f) }

        measure("contents questions over 500 leaves", 300, ceilingMicros = 4_000) {
            val callback = HibariDiffCallback(oldList, changed)
            var changedFound = 0
            for (position in 0 until count) {
                if (!callback.areContentsTheSame(position, position)) changedFound++
            }
            assertTrue(changedFound == 1)
        }
    }

    /** The pruning pass at the end of a round, over the slot count a large host holds. */
    @Test
    fun `slot pruning over a thousand slots`() {
        val memory = HashMap<String, Any?>()
        val owned = HashMap<String, Any?>()
        val touched = HashSet<String>()
        val scratch = ArrayList<String>()
        val paths = (0 until 1000).map {
            "${outerKeys[it and 7]}-${middleKeys[it % 5]}-${leafKeys[it % 20]}#$it"
        }
        for (path in paths) {
            val value = Pair(arrayOf<Any?>(), path)
            memory[path] = value
            owned[path] = value
        }
        // Built once, outside the measured body: building them per round would measure the string
        // builder instead of the pruning pass.
        val touchedPaths = paths.take(900)
        val stalePaths = paths.drop(900)

        measure("prune 100 of 1000 slots", 500, ceilingMicros = 400) {
            for (path in touchedPaths) touched.add(path)
            forgetUntouchedSlots(memory, owned, touched, scratch)
            // Re-arm the maps so each round has the same amount of work to do.
            for (path in stalePaths) {
                val value = memory[path] ?: owned[path] ?: Unit
                owned[path] = value
                memory[path] = value
            }
        }
    }
}
