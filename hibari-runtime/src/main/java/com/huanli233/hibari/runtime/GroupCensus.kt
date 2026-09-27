package com.huanli233.hibari.runtime

/**
 * One composition's worth of "could a group have been skipped": which root groups were entered at
 * all, and which of them read a state that actually changed this round.
 *
 * This is a census, not a decision - it deliberately ignores arguments a group received from its
 * caller, because nothing can compare those without the compiler plugin passing them down. The number
 * it produces is therefore the *upper bound* on what group level skipping could win: if few groups are
 * clean per round, the plugin work is not worth paying for.
 *
 * Callers hand over the walk path; everything is attributed to its first segment, since that is the
 * subtree a skip would cover, with the loop occurrence suffix folded back onto the group that
 * repeated. A read before any group was entered belongs to no subtree and is dropped.
 */
internal class GroupCensus(private val changed: Set<Any>) {

    private val seen = HashSet<String>()
    private val dirty = HashSet<String>()

    val isEmpty: Boolean get() = seen.isEmpty()

    fun noteGroup(path: String) {
        val root = rootOf(path)
        if (root.isNotEmpty()) seen.add(root)
    }

    fun noteRead(path: String, stateObject: Any) {
        val root = rootOf(path)
        if (root.isNotEmpty() && stateObject in changed) dirty.add(root)
    }

    /** Groups entered this round that touched nothing which changed. */
    fun cleanGroupCount(): Int = seen.count { it !in dirty }

    fun groupsSeen(): Int = seen.size

    private fun rootOf(path: String): String = path.substringBefore('-').substringBefore('#')
}
