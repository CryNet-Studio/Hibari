package com.huanli233.hibari.ui.text

import android.graphics.Typeface
import android.os.Build

/**
 * A font weight on the `wght` axis. Wear's type scale uses values that have no `Typeface` style
 * constant (450, 520, 550, 560, 580, 599), so [toAndroidStyle] quantises to the nearest of
 * NORMAL/MEDIUM/BOLD and the exact weight is only carried by [fontVariationSettings] on API 26+.
 */
@JvmInline
value class FontWeight(val value: Int) : Comparable<FontWeight> {

    override fun compareTo(other: FontWeight): Int = value.compareTo(other.value)

    override fun toString(): String = "FontWeight($value)"

    companion object {
        val W100 = FontWeight(100)
        val W200 = FontWeight(200)
        val W300 = FontWeight(300)
        val Normal = FontWeight(400)
        val Medium = FontWeight(500)
        val W600 = FontWeight(600)
        val SemiBold = FontWeight(600)
        val Bold = FontWeight(700)
        val W800 = FontWeight(800)
        val W900 = FontWeight(900)

        fun valueOf(weight: Int): FontWeight = FontWeight(weight)
    }
}

/**
 * The `fontVariationSettings` string carrying the exact weight plus Wear's `wdth` axis, which no
 * `Typeface` API can express. Returns null below API 26, where the platform ignores the setting.
 */
fun FontWeight.fontVariationSettings(width: Float? = null): String? {
    if (Build.VERSION.SDK_INT < 26) return null
    return buildString {
        append("'wght' ").append(value)
        if (width != null) append(", 'wdth' ").append(width)
    }
}

/**
 * Coarse `Typeface` style used on API 27 and below, where no weighted typeface API exists. It has
 * two outcomes only — `Typeface.MEDIUM` cannot be expressed, so weights between 400 and the bold
 * threshold collapse to `NORMAL` and the exact value survives only in
 * [fontVariationSettings]. On API 28+ [createTypeface] carries the weight directly and this is
 * never consulted.
 *
 * The bold threshold is ours, not a verified copy of Compose's internal `toTypefaceStyle`: that
 * function is `internal` to `compose.ui:ui-text`, so neither the pinned reference tree nor the
 * published API files expose its body. Wear's scale lands 520/550/560/580/599 below this cut and
 * 600/700/750 above it, so a different threshold from Compose's would show up as a weight jump one
 * step earlier or later on API 25-27 only.
 */
fun FontWeight.toAndroidStyle(): Int = when {
    value >= 700 -> Typeface.BOLD
    else -> Typeface.NORMAL
}

/**
 * Resolve [base] at this weight. API 28+ can express the weight directly; below that the value is
 * lost and only the [fontVariationSettings] string remains, which the platform ignores pre-26.
 */
fun FontWeight.createTypeface(base: Typeface? = null): Typeface {
    val tf = base ?: Typeface.DEFAULT
    return if (Build.VERSION.SDK_INT >= 28) {
        Typeface.create(tf, value, false)
    } else {
        Typeface.create(tf, toAndroidStyle())
    }
}

