package com.huanli233.hibari.ui

interface Modifier {
    /**
     * Chains this modifier with another one.
     */
    fun then(other: Modifier): Modifier

    /**
     * Folds over the elements of this modifier.
     * `initial` is the starting value, and `operation` is applied to each element.
     */
    fun <R> foldIn(initial: R, operation: (R, Element) -> R): R

    // An element of a Modifier chain. Attribute is a specialized Element.
    interface Element : Modifier {
        override fun <R> foldIn(initial: R, operation: (R, Element) -> R): R = operation(initial, this)
        override fun then(other: Modifier): Modifier =
            if (other === Modifier) this else CombinedModifier(this, other)
    }

    /**
     * The companion object serves as the entry point for creating modifiers.
     * It is an empty modifier by itself.
     */
    companion object : Modifier {
        override fun <R> foldIn(initial: R, operation: (R, Element) -> R): R = initial
        override fun then(other: Modifier): Modifier = other
    }
}

private class CombinedModifier(
    private val outer: Modifier,
    private val inner: Modifier
) : Modifier {
    override fun <R> foldIn(initial: R, operation: (R, Modifier.Element) -> R): R =
        inner.foldIn(outer.foldIn(initial, operation), operation)

    override fun then(other: Modifier): Modifier =
        if (other === Modifier) this else CombinedModifier(this, other)

    /**
     * Value equality for the chain. Without it, comparing a retuned modifier was reference
     * equality and therefore never equal, so every diff comparison fell through to flattening both
     * chains into fresh lists and keying them into fresh maps. A true result here implies the
     * attribute-wise comparison agrees, because that one inspects a subset of what is compared
     * element by element below.
     */
    override fun equals(other: Any?): Boolean =
        this === other || (other is CombinedModifier && outer == other.outer && inner == other.inner)

    override fun hashCode(): Int = 31 * outer.hashCode() + inner.hashCode()

    override fun toString(): String {
        return "${outer}, $inner"
    }
}

inline fun <T> Modifier.runIfNotNull(value: T?, block: Modifier.(T) -> Modifier) = run {
    if (value != null) block(value) else this
}

fun Modifier.flattenToList(): List<Modifier.Element> {
    return this.foldIn(mutableListOf()) { acc, element ->
        acc += element
        acc
    }
}

fun List<Modifier.Element>.viewAttributes(): List<ViewAttribute<*, *>> {
    return this.mapNotNull {
        it as? ViewAttribute<*, *>
    }
}

fun List<Modifier.Element>.layoutAttributes(): List<LayoutAttribute<*, *>> {
    return this.mapNotNull {
        it as? LayoutAttribute<*, *>
    }
}