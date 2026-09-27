package com.huanli233.hibari.recyclerview

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.hibari.foundation.Node
import com.huanli233.hibari.foundation.layout.LayoutScopeMarker
import com.huanli233.hibari.runtime.Tunable
import com.huanli233.hibari.runtime.currentTuner
import com.huanli233.hibari.runtime.remember
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.ref
import com.huanli233.hibari.ui.viewClass


@LayoutScopeMarker
interface LazyListScope {
    fun item(
        key: Any? = null,
        contentType: Any? = null,
        content: @Tunable () -> Unit
    )

    fun pos(
        count: Int,
        key: (index: Int) -> Any? = { it },
        contentType: (index: Int) -> Any? = { null },
        content: @Tunable (index: Int) -> Unit
    )

    fun <T> items(
        items: List<T>,
        key: (item: T) -> Any? = { it },
        contentType: (item: T) -> Any? = { null },
        content: @Tunable (item: T) -> Unit
    )
}

/**
 * Public so Wear's lazy lists can build the same item model without duplicating the scope DSL.
 */
class LazyListScopeImpl : LazyListScope {

    private val _items = mutableListOf<LazyListItem>()
    val items: List<LazyListItem> get() = _items

    fun reset() {
        _items.clear()
    }

    override fun item(key: Any?, contentType: Any?, content: @Tunable () -> Unit) {
        _items.add(
            LazyListItem(
                key = key,
                contentType = contentType,
                content = content
            )
        )
    }

    override fun pos(
        count: Int,
        key: (index: Int) -> Any?,
        contentType: (index: Int) -> Any?,
        content: @Tunable (index: Int) -> Unit
    ) {
        for (index in 0 until count) {
            _items.add(
                LazyListItem(
                    key = key(index),
                    contentType = contentType(index),
                    content = { content(index) }
                )
            )
        }
    }

    override fun <T> items(
        items: List<T>,
        key: (item: T) -> Any?,
        contentType: (item: T) -> Any?,
        content: @Tunable (item: T) -> Unit
    ) {
        items.forEach { item ->
            _items.add(
                LazyListItem(
                    key = key(item),
                    contentType = contentType(item),
                    content = { content(item) },
                    // The item itself is what the lambda renders, so it is what has to be compared.
                    data = item
                )
            )
        }
    }
}

/**
 * `submitList` diffs on a background thread and swaps the list in on the main one, and the list it is
 * handed is its own — a host that recomposes every animation frame would otherwise pay a full item
 * diff per frame. An item compares by key, content type and data, which is exactly the work a rebind
 * would produce, so a list equal to the one already submitted is skipped.
 *
 * The copy that gets submitted is also the one kept for the next comparison, so nothing the caller
 * still mutates can be read while the differ walks it off-thread.
 */
internal class SubmittedItems {

    private var last: List<LazyListItem> = emptyList()

    /** Returns the list to submit, or null when it is the same list as the one already in place. */
    fun take(next: List<LazyListItem>): List<LazyListItem>? {
        if (next.size == last.size && next.indices.all { last[it] == next[it] }) return null

        val snapshot = next.toList()
        last = snapshot
        return snapshot
    }
}

@Tunable
fun LazyList(
    modifier: Modifier = Modifier,
    layoutManager: (Context) -> RecyclerView.LayoutManager,
    content: LazyListScope.() -> Unit,
) {
    val parentTunation = currentTuner.tunation
    val adapter = remember { HibariAdapter(parentTunation) }
    val submitted = remember { SubmittedItems() }
    val scope = LazyListScopeImpl().apply(content)
    submitted.take(scope.items)?.let { adapter.submitList(it) }

    Node(
        modifier = modifier
            .viewClass(RecyclerView::class.java)
            .ref {
                if (it is RecyclerView) {
                    it.layoutManager = layoutManager(it.context)
                    it.adapter = adapter
                }
            }
    )
}

@Tunable
fun LazyRow(
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    content: LazyListScope.() -> Unit,
) {
    LazyList(modifier, { context -> LinearLayoutManager(context, RecyclerView.HORIZONTAL, reverseLayout) }, content)
}

@Tunable
fun LazyColumn(
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    content: LazyListScope.() -> Unit,
) {
    LazyList(modifier, { context -> LinearLayoutManager(context, RecyclerView.VERTICAL, reverseLayout) }, content)
}