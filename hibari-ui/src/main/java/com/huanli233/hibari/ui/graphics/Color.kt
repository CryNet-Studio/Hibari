package com.huanli233.hibari.ui.graphics

import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import kotlin.math.pow

/**
 * Four 8-bit sRGB components packed into the high 32 bits of a [ULong], mirroring Compose's
 * encoding: alpha at bit 56, red 48, green 40, blue 32. The low 6 bits hold the color space id,
 * zero for sRGB and [unspecifiedColorSpaceId] for [Color.Unspecified].
 *
 * Only the sRGB branch of Compose's Color is implemented, so the component getters, [toArgb] and
 * [compositeOver] are bit-for-bit equivalent to upstream for sRGB colors, which is the entire
 * subset design tokens use. The `Color(...)` factories are top-level functions rather than
 * secondary constructors because the Int and Long forms clash on the JVM once erased.
 */
@JvmInline
value class Color(val value: ULong) {

    val alpha: Float
        get() = ((value shr 56) and 0xffUL).toFloat() / 255.0f

    val red: Float
        get() = ((value shr 48) and 0xffUL).toFloat() / 255.0f

    val green: Float
        get() = ((value shr 40) and 0xffUL).toFloat() / 255.0f

    val blue: Float
        get() = ((value shr 32) and 0xffUL).toFloat() / 255.0f

    val isUnspecified: Boolean
        get() = (value and 0x3fUL) == unspecifiedColorSpaceId

    val isSpecified: Boolean
        get() = !isUnspecified

    fun copy(
        alpha: Float = this.alpha,
        red: Float = this.red,
        green: Float = this.green,
        blue: Float = this.blue,
    ): Color = Color(red = red, green = green, blue = blue, alpha = alpha)

    @ColorInt
    fun toArgb(): Int = (value shr 32).toInt()

    fun compositeOver(background: Color): Color {
        val bgA = background.alpha
        val fgA = alpha
        val a = fgA + (bgA * (1f - fgA))
        if (a == 0f) return Transparent
        return Color(
            compositeComponent(red, background.red, fgA, bgA, a),
            compositeComponent(green, background.green, fgA, bgA, a),
            compositeComponent(blue, background.blue, fgA, bgA, a),
            a,
        )
    }

    override fun toString(): String = "Color($red, $green, $blue, $alpha, sRGB IEC61966-2.1)"

    companion object {
        val Black = Color(0xFF000000)
        val DarkGray = Color(0xFF444444)
        val Gray = Color(0xFF888888)
        val LightGray = Color(0xFFCCCCCC)
        val White = Color(0xFFFFFFFF)
        val Red = Color(0xFFFF0000)
        val Green = Color(0xFF00FF00)
        val Blue = Color(0xFF0000FF)
        val Yellow = Color(0xFFFFFF00)
        val Cyan = Color(0xFF00FFFF)
        val Magenta = Color(0xFFFF00FF)
        val Transparent = Color(0x00000000)
        val Unspecified = Color(value = unspecifiedColorSpaceId)
    }
}

@PublishedApi
internal const val unspecifiedColorSpaceId = 0x10UL

fun Color(@ColorInt color: Int): Color = Color(value = color.toULong() shl 32)

/** Accepts an ARGB literal whose alpha exceeds 0x80, e.g. `Color(0xFFE9DDFF)`. */
fun Color(color: Long): Color = Color(value = (color shl 32).toULong())

fun Color(
    @IntRange(from = 0, to = 0xFF) red: Int,
    @IntRange(from = 0, to = 0xFF) green: Int,
    @IntRange(from = 0, to = 0xFF) blue: Int,
    @IntRange(from = 0, to = 0xFF) alpha: Int = 0xFF,
): Color = Color(
    value = (((alpha and 0xFF) shl 24) or
        ((red and 0xFF) shl 16) or
        ((green and 0xFF) shl 8) or
        (blue and 0xFF)).toULong() shl 32
)

fun Color(
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float = 1f,
): Color = Color(
    value = (((alpha.coerceIn(0f, 1f) * 255f + 0.5f).toInt() shl 24) or
        ((red.coerceIn(0f, 1f) * 255f + 0.5f).toInt() shl 16) or
        ((green.coerceIn(0f, 1f) * 255f + 0.5f).toInt() shl 8) or
        (blue.coerceIn(0f, 1f) * 255f + 0.5f).toInt()).toULong() shl 32
)

private fun compositeComponent(
    fgC: Float,
    bgC: Float,
    fgA: Float,
    bgA: Float,
    a: Float,
) = (fgC * fgA) + ((bgC * bgA) * (1f - fgA)) / a

/**
 * Per-channel interpolation in linear light. Compose's own color `lerp` switched to Oklab in 1.11,
 * but Wear animates colors through `SrgbToLinearTwoWayConverter`, so linear light reproduces the
 * value Wear renders mid-animation.
 */
fun lerp(
    start: Color,
    stop: Color,
    @FloatRange(from = 0.0, to = 1.0) fraction: Float,
): Color {
    val t = fraction.coerceIn(0f, 1f)
    // Interpolate in linear light, then convert back: the Float constructor stores sRGB bytes, so
    // handing it linear values directly would darken every midpoint.
    return Color(
        linearToSrgb(lerpComponent(srgbToLinear(start.red), srgbToLinear(stop.red), t)),
        linearToSrgb(lerpComponent(srgbToLinear(start.green), srgbToLinear(stop.green), t)),
        linearToSrgb(lerpComponent(srgbToLinear(start.blue), srgbToLinear(stop.blue), t)),
        lerpComponent(start.alpha, stop.alpha, t),
    )
}

private fun lerpComponent(start: Float, stop: Float, fraction: Float): Float =
    (1f - fraction) * start + fraction * stop

fun Color.takeOrElse(block: () -> Color): Color = if (isSpecified) this else block()

fun srgbToLinear(x: Float): Float = when {
    x <= 0.04045f -> x / 12.92f
    x < 0.97381f -> ((x + 0.055f) / 1.055f).pow(2.4f)
    else -> x
}

fun linearToSrgb(x: Float): Float = when {
    x <= 0.0031308f -> x * 12.92f
    x < 0.9695377f -> 1.055f * x.pow(1f / 2.4f) - 0.055f
    else -> x
}

private fun relativeLuminance(r: Float, g: Float, b: Float): Float =
    0.2126f * r + 0.7152f * g + 0.0722f * b

/**
 * Return this colour scaled in linear light until its relative luminance reaches [luminance]
 * (`0..1`).
 *
 * Deviation from upstream, stated plainly: Wear reaches for this when deriving a `*Dim` token from
 * a container colour, and its own path runs through Oklab's `L` (via the private `Cam`/`CamUtils`
 * in ColorAppearanceModel). Oklab `L` is a cube-root response, so matching relative luminance in
 * linear sRGB is close but not identical — most visible on very dark colours, where this has to
 * lift harder than Oklab does. Hue and saturation are preserved either way.
 */
fun Color.setLuminance(luminance: Float): Color {
    val target = luminance.coerceIn(0f, 1f)
    val r = srgbToLinear(red)
    val g = srgbToLinear(green)
    val b = srgbToLinear(blue)
    val current = relativeLuminance(r, g, b)
    if (current == 0f) return Color(red = target, green = target, blue = target, alpha = alpha)
    val k = target / current
    return Color(
        linearToSrgb((r * k).coerceIn(0f, 1f)),
        linearToSrgb((g * k).coerceIn(0f, 1f)),
        linearToSrgb((b * k).coerceIn(0f, 1f)),
        alpha,
    )
}
