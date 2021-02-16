package swarm

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import kotlin.math.min
import kotlin.math.pow

class Tracer(private val canvas: Canvas) {
    private val blockSize = 64
    private val tasks = mutableListOf<Callable<Any>>()
    private val depthBuffer = DoubleArray(canvas.width * canvas.height)
    val lights = mutableListOf<Light>()

    fun trace(eye: Vector3, spheres: List<Sphere>, backColor: Color) {
        buildTasks(eye, spheres, backColor)
        ForkJoinPool.commonPool().invokeAll(tasks)
    }

    private fun buildTasks(eye: Vector3, spheres: List<Sphere>, backColor: Color) {
        if (tasks.isEmpty()) {
            var chunkY = 0
            while (chunkY < canvas.height) {
                val oy = chunkY
                var chunkX = 0
                while (chunkX < canvas.width) {
                    val ox = chunkX
                    tasks.add(Executors.callable { renderBlock(eye, ox, oy, spheres, backColor) })
                    chunkX += blockSize
                }
                chunkY += blockSize
            }
        }
    }

    private fun renderBlock(eye: Vector3, startX: Int, startY: Int, spheres: List<Sphere>, backColor: Color) {
        val halfWidth = canvas.width / 2.0
        val halfHeight = canvas.height / 2.0
        val maxY = canvas.height.coerceAtMost(startY + blockSize)
        val maxX = canvas.width.coerceAtMost(startX + blockSize)
        for (y in startY until maxY) {
            val offset = y * canvas.width
            for (x in startX until maxX) {
                depthBuffer[offset + x] = Double.MAX_VALUE
                val ray = Ray(eye, Vector3(x - halfWidth, halfHeight - y, -400.0))
                for (sphere in spheres) {
                    val distance = sphere.intersect(ray)
                    if (distance > 0.0) {
                        if (distance < depthBuffer[offset + x]) {
                            depthBuffer[offset + x] = distance
                            val intersection = ray.lerp(distance)
                            val normal = (intersection - sphere.position) / sphere.radius
                            var diffuse = Color.BLACK
                            var specular = Color.BLACK
                            for (light in lights) {
                                val toLight = Ray(intersection, light.position - intersection)
                                diffuse += sphere.color * (Vector3.dot(toLight.direction, normal)).coerceAtLeast(0.0)
                                if (light.specularColor != Color.BLACK) {
                                    val vertexToEye = -ray.direction
                                    val lightReflect = Vector3.normalize(Vector3.reflect(toLight.direction, normal))
                                    val specFactor = Vector3.dot(vertexToEye, lightReflect)
                                    if (specFactor > 0.0) {
                                        specular += light.specularColor * Color.spread(min(specFactor, 1.0).pow(4.0))
                                    }
                                }
                            }
                            val depthFade = (distance / 250.0).pow(6).coerceIn(0.0, 1.0)
                            val fadeColor = backColor * depthFade
                            val color = (diffuse + specular) * (1.0 - depthFade)
                            canvas.pixels[offset + x] = (color + fadeColor).toArgb()
                        }
                    }
                }
            }
        }
    }
}
