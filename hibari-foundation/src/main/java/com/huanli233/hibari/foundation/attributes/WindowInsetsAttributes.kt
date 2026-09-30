package com.huanli233.hibari.foundation.attributes

import android.view.View
import com.huanli233.hibari.foundation.layout.InsetsValues
import com.huanli233.hibari.foundation.layout.WindowInsets
import com.huanli233.hibari.foundation.layout.navigationBars
import com.huanli233.hibari.foundation.layout.statusBars
import com.huanli233.hibari.foundation.layout.systemBars
import com.huanli233.hibari.runtime.Tunable
import com.huanli233.hibari.runtime.locals.LocalDensity
import com.huanli233.hibari.runtime.locals.LocalLayoutDirection
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.thenViewAttribute
import com.huanli233.hibari.ui.uniqueKey

/**
 * Add [insets] to the padding of the view, on top of whatever `Modifier.padding` asks for.
 *
 * The resolved edges are the attribute value, so a change in the insets — or in the layout direction
 * they are resolved against — is visible to the patch diff. They are resolved here rather than in the
 * applier because the density and direction come from the composition.
 */
@Tunable
internal fun Modifier.insetsPaddingOf(insets: WindowInsets, key: Any): Modifier {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val edges = InsetsValues(
        left = insets.getLeft(density, layoutDirection),
        top = insets.getTop(density),
        right = insets.getRight(density, layoutDirection),
        bottom = insets.getBottom(density)
    )
    return this.thenViewAttribute<View, InsetsValues>(key, edges) { part ->
        paddingParts().setInsets(key, part.left, part.top, part.right, part.bottom).writeInto(this)
    }
}

@Tunable
fun Modifier.windowInsetsPadding(insets: WindowInsets): Modifier = insetsPaddingOf(insets, uniqueKey)

// Each of these asks for its own attribute key: `uniqueKey` is one constant per call site, so had
// they shared the one inside windowInsetsPadding, two of them on a chain would have compared as a
// single attribute and only the last would ever have been diffed — or summed.
@Tunable
fun Modifier.statusBarsPadding(): Modifier = insetsPaddingOf(WindowInsets.statusBars, uniqueKey)

@Tunable
fun Modifier.navigationBarsPadding(): Modifier = insetsPaddingOf(WindowInsets.navigationBars, uniqueKey)

@Tunable
fun Modifier.systemBarsPadding(): Modifier = insetsPaddingOf(WindowInsets.systemBars, uniqueKey)
