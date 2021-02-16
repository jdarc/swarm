package swarm

import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import kotlin.math.min

class Canvas(val width: Int, val height: Int, val pixels: IntArray) {
    private val blendTasks = ArrayList<Callable<Any>>()
    private val blurTasks = ArrayList<Callable<Any>>()
    private val scratch = IntArray(pixels.size)

    fun blend(color: Color) {
        if (blendTasks.size == 0) buildBlendTasks(color)
        ForkJoinPool.commonPool().invokeAll(blendTasks)
    }

    fun blur() {
        if (blurTasks.size == 0) buildBlurTasks()
        ForkJoinPool.commonPool().invokeAll(blurTasks)
        System.arraycopy(scratch, 0, pixels, 0, pixels.size)
    }

    private fun buildBlendTasks(color: Color) {
        repeat((0 until height).count()) {
            blendTasks.add(Executors.callable {
                for (x in 0 until width) {
                    val rgb = pixels[it * width + x]
                    val red = (9 * Color.red(rgb) + 7 * color.red) shr 4
                    val grn = (9 * Color.grn(rgb) + 7 * color.grn) shr 4
                    val blu = (9 * Color.blu(rgb) + 7 * color.blu) shr 4
                    pixels[it * width + x] = Color.toArgb(red, grn, blu)
                }
            })
        }
    }

    private fun buildBlurTasks() {
        var chunkY = 0
        while (chunkY < height) {
            val oy = chunkY
            var chunkX = 0
            while (chunkX < width) {
                val ox = chunkX
                blurTasks.add(Executors.callable {
                    val maxY = min(height, oy + BLOCK_SIZE)
                    val maxX = min(width, ox + BLOCK_SIZE)
                    for (cy in oy until maxY) {
                        for (cx in ox until maxX) {
                            var red = 0.0
                            var grn = 0.0
                            var blu = 0.0
                            for (row in -1..1) {
                                val y = (cy + row).coerceIn(0, height - 1)
                                for (col in -1..1) {
                                    val x = (cx + col).coerceIn(0, width - 1)
                                    val element = filter[row + 1][col + 1]
                                    val argb = pixels[x + y * width]
                                    red += element * Color.red(argb)
                                    grn += element * Color.grn(argb)
                                    blu += element * Color.blu(argb)
                                }
                            }
                            scratch[cx + cy * width] = Color.toArgb(red / 16.0, grn / 16.0, blu / 16.0)
                        }
                    }
                })
                chunkX += BLOCK_SIZE
            }
            chunkY += BLOCK_SIZE
        }
    }

    companion object {
        private const val BLOCK_SIZE = 16
        private val filter = arrayOf(doubleArrayOf(1.0, 2.0, 1.0), doubleArrayOf(2.0, 4.0, 2.0), doubleArrayOf(1.0, 2.0, 1.0))
    }
}
