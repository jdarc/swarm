package swarm

import java.util.concurrent.ThreadLocalRandom
import kotlin.math.floor
import kotlin.math.min

class Color(red: Double, grn: Double, blu: Double) {
    private val r = red.coerceIn(0.0, 1.0)
    private val g = grn.coerceIn(0.0, 1.0)
    private val b = blu.coerceIn(0.0, 1.0)

    val red = (r * 255).toInt()
    val grn = (g * 255).toInt()
    val blu = (b * 255).toInt()

    constructor(color: java.awt.Color) : this(color.red / 256.0, color.green / 256.0, color.blue / 256.0)

    operator fun plus(color: Color) = Color(r + color.r, g + color.g, b + color.b)

    operator fun times(color: Color) = Color(r * color.r, g * color.g, b * color.b)

    operator fun times(factor: Double) = Color(r * factor, g * factor, b * factor)

    fun toArgb(): Int {
        val r = min(255, (r * 256.0).toInt())
        val g = min(255, (g * 256.0).toInt())
        val b = min(255, (b * 256.0).toInt())
        return argb(255, r, g, b)
    }

    private fun argb(alpha: Int, red: Int, grn: Int, blu: Int): Int {
        val a = alpha.coerceIn(0, 255)
        val r = red.coerceIn(0, 255)
        val g = grn.coerceIn(0, 255)
        val b = blu.coerceIn(0, 255)
        return a shl 24 or (r shl 16) or (g shl 8) or b
    }

    companion object {
        val BLACK = Color(0.0, 0.0, 0.0)

        fun red(argb: Int) = 255 and argb.shr(16)

        fun grn(argb: Int) = 255 and argb.shr(8)

        fun blu(argb: Int) = 255 and argb

        fun toArgb(red: Int, grn: Int, blu: Int): Int {
            val r = red.coerceIn(0, 255)
            val g = grn.coerceIn(0, 255)
            val b = blu.coerceIn(0, 255)
            return 255.shl(24) or r.shl(16) or g.shl(8) or b
        }

        fun toArgb(red: Double, grn: Double, blu: Double) =
            toArgb(floor(red).toInt(), floor(grn).toInt(), floor(blu).toInt())

        fun spread(v: Double) = Color(v, v, v)

        fun randomColor(min: Double = 0.0, max: Double = 1.0): Color {
            val red = ThreadLocalRandom.current().nextDouble(min, max).coerceIn(0.0, 1.0)
            val grn = ThreadLocalRandom.current().nextDouble(min, max).coerceIn(0.0, 1.0)
            val blu = ThreadLocalRandom.current().nextDouble(min, max).coerceIn(0.0, 1.0)
            return Color(red, grn, blu)
        }
    }
}
