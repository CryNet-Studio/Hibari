package com.huanli233.hibari.ui.text

import android.graphics.Typeface

/**
 * A named font family. [Default] leaves resolution to the platform, so [typeface] returns null for
 * it and callers fall back to the view's own typeface.
 */
data class FontFamily(val names: List<String>) {

    fun typeface(style: Int = Typeface.NORMAL): Typeface? =
        names.firstOrNull()?.let { Typeface.create(it, style) }

    companion object {
        val Default = FontFamily(emptyList())
        val SansSerif = FontFamily(listOf("sans-serif"))
        val Serif = FontFamily(listOf("serif"))
        val Monospace = FontFamily(listOf("monospace"))

        fun named(name: String) = FontFamily(listOf(name))
    }
}
