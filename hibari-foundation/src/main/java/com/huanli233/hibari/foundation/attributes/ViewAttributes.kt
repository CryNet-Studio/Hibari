package com.huanli233.hibari.foundation.attributes

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.thenLayoutAttribute
import com.huanli233.hibari.ui.thenUnitLayoutAttribute
import com.huanli233.hibari.ui.thenViewAttribute
import com.huanli233.hibari.ui.uniqueKey
import com.huanli233.hibari.ui.unit.Dp
import com.huanli233.hibari.ui.unit.DpSize
import com.huanli233.hibari.ui.unit.toPx

fun matchParentSize(): Modifier = Modifier.matchParentSize()
fun matchParentWidth(): Modifier = Modifier.matchParentWidth()
fun matchParentHeight(): Modifier = Modifier.matchParentHeight()

fun Modifier.matchParentWidth(): Modifier {
    return thenUnitLayoutAttribute<ViewGroup.LayoutParams>(uniqueKey) {
        width = ViewGroup.LayoutParams.MATCH_PARENT
    }
}
fun Modifier.matchParentHeight(): Modifier {
    return thenUnitLayoutAttribute<ViewGroup.LayoutParams>(uniqueKey) {
        height = ViewGroup.LayoutParams.MATCH_PARENT
    }
}
fun Modifier.matchParentSize(): Modifier {
    return thenUnitLayoutAttribute<ViewGroup.LayoutParams>(uniqueKey) {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
    }
}
/**
 * The size arrives as the attribute value rather than inside the applier closure: a unit-valued
 * attribute compares equal to its own successor, so a dynamic `width` captured by the closure would
 * be diffed as unchanged and the layout params would freeze at the first value ever applied.
 */
fun Modifier.width(width: Dp): Modifier {
    return this.thenLayoutAttribute<ViewGroup.LayoutParams, Dp>(uniqueKey, width) { dp, view ->
        this.width = dp.toPx(view)
    }
}

fun Modifier.height(height: Dp): Modifier {
    return this.thenLayoutAttribute<ViewGroup.LayoutParams, Dp>(uniqueKey, height) { dp, view ->
        this.height = dp.toPx(view)
    }
}

fun Modifier.size(size: DpSize): Modifier {
    return this.thenLayoutAttribute<ViewGroup.LayoutParams, DpSize>(uniqueKey, size) { dpSize, view ->
        this.width = dpSize.width.toPx(view)
        this.height = dpSize.height.toPx(view)
    }
}

fun Modifier.onClick(onClick: () -> Unit): Modifier {
    return this.thenViewAttribute<View, () -> Unit>(uniqueKey, onClick) {
        setOnClickListener { onClick() }
    }
}

fun Modifier.fitsSystemWindows(value: Boolean): Modifier {
    return this.thenViewAttribute<View, Boolean>(uniqueKey, value) { this.fitsSystemWindows = it }
}

fun Modifier.alpha(value: Float): Modifier {
    return this.thenViewAttribute<View, Float>(uniqueKey, value) { this.alpha = it }
}

fun Modifier.scaleX(value: Float): Modifier {
    return this.thenViewAttribute<View, Float>(uniqueKey, value) { this.scaleX = it }
}

fun Modifier.scaleY(value: Float): Modifier {
    return this.thenViewAttribute<View, Float>(uniqueKey, value) { this.scaleY = it }
}

fun Modifier.translationX(value: Float): Modifier {
    return this.thenViewAttribute<View, Float>(uniqueKey, value) { this.translationX = it }
}

fun Modifier.translationY(value: Float): Modifier {
    return this.thenViewAttribute<View, Float>(uniqueKey, value) { this.translationY = it }
}

fun Modifier.elevation(value: Float): Modifier {
    return this.thenViewAttribute<View, Float>(uniqueKey, value) { ViewCompat.setElevation(this, it) }
}

/**
 * Floor for `wrap_content` measurement, the Views equivalent of Compose's `Modifier.heightIn`.
 * A fixed `height` on the layout params wins, exactly as an explicit size overrides `heightIn`.
 */
fun Modifier.minHeight(minHeight: Dp): Modifier {
    return this.thenViewAttribute<View, Dp>(uniqueKey, minHeight) {
        this.minimumHeight = it.toPx(this)
    }
}