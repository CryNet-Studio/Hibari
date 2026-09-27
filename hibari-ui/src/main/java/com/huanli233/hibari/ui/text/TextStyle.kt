package com.huanli233.hibari.ui.text

import com.huanli233.hibari.ui.unit.TextUnit
import com.huanli233.hibari.ui.unit.takeOrElse

/**
 * The text properties Hibari can apply to a `TextView`. Sizes keep the original [TextUnit] so the
 * sp-to-em conversion done at apply time can use the font size it belongs to.
 */
data class TextStyle(
    val fontSize: TextUnit = TextUnit.Unspecified,
    val lineHeight: TextUnit = TextUnit.Unspecified,
    val letterSpacing: TextUnit = TextUnit.Unspecified,
    val fontWeight: FontWeight = FontWeight.Normal,
    val fontFamily: FontFamily = FontFamily.Default,
    /** Extra variation axes, e.g. Wear's `'wdth'`. Applied only on API 26+. */
    val widthAxis: Float? = null,
    val fontFeatureSettings: String? = null,
) {

    fun merge(other: TextStyle): TextStyle = TextStyle(
        fontSize = other.fontSize.takeOrElse { fontSize },
        lineHeight = other.lineHeight.takeOrElse { lineHeight },
        letterSpacing = other.letterSpacing.takeOrElse { letterSpacing },
        fontWeight = other.fontWeight,
        fontFamily = other.fontFamily.takeOrElse { fontFamily },
        widthAxis = other.widthAxis ?: widthAxis,
        fontFeatureSettings = other.fontFeatureSettings ?: fontFeatureSettings,
    )

    companion object {
        val Default = TextStyle()
    }
}

fun FontFamily.takeOrElse(block: () -> FontFamily): FontFamily =
    if (this != FontFamily.Default) this else block()
