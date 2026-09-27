package com.huanli233.hibari.ui.geometry

import com.huanli233.hibari.ui.unit.Dp
import com.huanli233.hibari.ui.unit.dp
import com.huanli233.hibari.ui.unit.isUnspecified
import kotlin.math.min

/**
 * A corner geometry that can be rasterised into the eight radii floats `Canvas` expects.
 * Mirrors Compose's `Shape` but only as far as a `Drawable` needs it.
 */
sealed interface Shape {
    /**
     * Fill [outRadii] with the 8 corner radii in **pixels** (x,y per corner, clockwise from
     * top-left). [density] converts the stored [Dp] and is supplied by the caller because a
     * [android.graphics.drawable.Drawable] has no context of its own.
     */
    fun toRadii(width: Float, height: Float, density: Float, outRadii: FloatArray)
}

/**
 * Wear's shapes are [CornerBasedShape]s: a requested radius that is additionally clamped so no
 * corner exceeds [MaxCornerPercentage] of the shorter side, which is what keeps a 36.dp "large"
 * shape from turning a small chip into a pill.
 */
data class CornerBasedShape(
    val topStart: Dp = 0.dp,
    val topEnd: Dp = 0.dp,
    val bottomEnd: Dp = 0.dp,
    val bottomStart: Dp = 0.dp,
) : Shape {

    override fun toRadii(width: Float, height: Float, density: Float, outRadii: FloatArray) {
        val max = min(width, height) * MaxCornerPercentage
        fun Dp.clamped(): Float =
            if (this == Dp.Infinity) max else (value * density).coerceAtMost(max)
        val ts = topStart.clamped()
        val te = topEnd.clamped()
        val be = bottomEnd.clamped()
        val bs = bottomStart.clamped()
        // Canvas order: top-left, top-right, bottom-right, bottom-left.
        outRadii[0] = ts; outRadii[1] = ts
        outRadii[2] = te; outRadii[3] = te
        outRadii[4] = be; outRadii[5] = be
        outRadii[6] = bs; outRadii[7] = bs
    }

    companion object {
        /** A corner may not exceed this fraction of the shorter side, keeping chips from becoming pills. */
        const val MaxCornerPercentage = 0.9f / 2f
    }
}

fun RoundedCornerShape(size: Dp): CornerBasedShape =
    CornerBasedShape(size, size, size, size)

fun RoundedCornerShape(
    topStart: Dp = 0.dp,
    topEnd: Dp = 0.dp,
    bottomEnd: Dp = 0.dp,
    bottomStart: Dp = 0.dp,
): CornerBasedShape = CornerBasedShape(topStart, topEnd, bottomEnd, bottomStart)

data object CircleShape : Shape {
    override fun toRadii(width: Float, height: Float, density: Float, outRadii: FloatArray) {
        val r = min(width, height) / 2f
        for (i in 0 until 8) outRadii[i] = r
    }
}

data object RectangleShape : Shape {
    override fun toRadii(width: Float, height: Float, density: Float, outRadii: FloatArray) {
        for (i in 0 until 8) outRadii[i] = 0f
    }
}

fun Dp.orZero(): Dp = if (isUnspecified || this == Dp.Infinity) 0.dp else this
