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
 * optimising arguments are numbers rather than my reading of the code. Each case prints microseconds
 * per round and asserts only a loose ceiling: these are here to catch an order-of-magnitude
 * regression, not to win a beauty contest against the JIT.
 */
class CompositionHotPathBenchmark {

    private object Applier : AttributeApplier<View, Any> {
        override fun apply(target: View, value: Any) = Unit
    }

    private fun measure(label: String, rounds: Int, ceilingMicros: Long, body: () -> Unit): Long {
        repeat(rounds / 5 + 1) { body() }
        val beganAt = System.nanoTime()
        repeat(rounds) { body() }
        val micros = (System.nanoTime() - beganAt) / rounds / 1000
        println("HibariBench  $label: ${micros}us per round over $rounds rounds")
        assertTrue("$label took ${micros}us per round", micros < ceilingMicros)
        return micros
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
        val keys = IntArray(40) { it }

        measure("walker 40 groups x depth 3", 5_000, ceilingMicros = 400) {
            walker.clear()
            for (key in keys) {
                walker.start(key)
                walker.start(key + 1)
                walker.start(key + 2)
                walker.path()
                walker.end()
                walker.end()
                walker.end()
            }
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
        for (index in 0 until 1000) {
            val path = "7-9-$index"
            val value = Pair(arrayOf<Any?>(), index)
            memory[path] = value
            owned[path] = value
        }
        // Built once, outside the measured body: interpolating a thousand paths per round measures
        // the string builder, not the pruning pass.
        val touchedPaths = (0 until 900).map { "7-9-$it" }
        val stalePaths = (900 until 1000).map { "7-9-$it" }

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
