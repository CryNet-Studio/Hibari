package com.huanli233.hibari.foundation.attributes

import android.os.Build
import android.view.View
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.thenViewAttribute
import com.huanli233.hibari.ui.uniqueKey
import com.huanli233.hibari.ui.unit.Dp
import com.huanli233.hibari.ui.unit.LayoutDirection
import com.huanli233.hibari.ui.unit.PaddingValues
import com.huanli233.hibari.ui.unit.dp
import com.huanli233.hibari.ui.unit.takeOrElse
import com.huanli233.hibari.ui.unit.toPx

fun Modifier.padding(paddingValues: PaddingValues): Modifier {
    return this.thenViewAttribute<View, PaddingValues>(uniqueKey, paddingValues) { pv ->
        val layoutDirection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            when (this.layoutDirection) {
                View.LAYOUT_DIRECTION_LTR -> LayoutDirection.Ltr
                View.LAYOUT_DIRECTION_RTL -> LayoutDirection.Rtl
                else -> LayoutDirection.Ltr
            }
        } else {
            LayoutDirection.Ltr
        }
        paddingParts().setOwn(
            pv.calculateLeftPadding(layoutDirection).toPx(this),
            pv.calculateTopPadding().toPx(this),
            pv.calculateRightPadding(layoutDirection).toPx(this),
            pv.calculateBottomPadding().toPx(this)
        ).writeInto(this)
    }
}

fun Modifier.padding(all: Dp): Modifier {
    return padding(PaddingValues(all = all))
}

fun Modifier.padding(vertical: Dp = Dp.Unspecified, horizontal: Dp = Dp.Unspecified): Modifier {
    return padding(PaddingValues(vertical = vertical, horizontal = horizontal))
}


fun Modifier.padding(
    left: Dp = Dp.Unspecified,
    top: Dp = Dp.Unspecified,
    right: Dp = Dp.Unspecified,
    bottom: Dp = Dp.Unspecified
): Modifier {
    // Absolute (RTL-unaware) overload: route through PaddingValues.Absolute, NOT start/end, or the
    // left/right edges would swap once the layout direction is RTL. PaddingValues.Absolute rejects
    // NaN, so an unspecified edge is coerced to 0.dp (its meaning everywhere else here).
    return padding(
        PaddingValues.Absolute(
            left = left.takeOrElse { 0.dp },
            top = top.takeOrElse { 0.dp },
            right = right.takeOrElse { 0.dp },
            bottom = bottom.takeOrElse { 0.dp }
        )
    )
}

fun Modifier.paddingRelative(
    start: Dp = Dp.Unspecified,
    top: Dp = Dp.Unspecified,
    end: Dp = Dp.Unspecified,
    bottom: Dp = Dp.Unspecified
): Modifier {
    return padding(PaddingValues(start = start, top = top, end = end, bottom = bottom))
}