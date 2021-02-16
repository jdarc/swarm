package swarm

import java.lang.Math.fma
import kotlin.math.sqrt

class Vector3(val x: Double, val y: Double, val z: Double) {

    val length = sqrt(fma(x, x, fma(y, y, z * z)))

    operator fun plus(rhs: Vector3) = Vector3(x + rhs.x, y + rhs.y, z + rhs.z)

    operator fun minus(rhs: Vector3) = Vector3(x - rhs.x, y - rhs.y, z - rhs.z)

    operator fun times(scalar: Double): Vector3 = Vector3(x * scalar, y * scalar, z * scalar)

    operator fun div(scalar: Double): Vector3 = Vector3(x / scalar, y / scalar, z / scalar)

    operator fun unaryMinus() = Vector3(-x, -y, -z)

    companion object {
        val ZERO = Vector3(0.0, 0.0, 0.0)

        fun normalize(vec: Vector3) = vec / vec.length

        fun dot(lhs: Vector3, rhs: Vector3): Double = fma(lhs.x, rhs.x, fma(lhs.y, rhs.y, lhs.z * rhs.z))

        fun reflect(lhs: Vector3, rhs: Vector3): Vector3 {
            val s = dot(lhs, rhs) * 2.0
            return Vector3(fma(rhs.x, s, -lhs.x), fma(rhs.y, s, -lhs.y), fma(rhs.z, s, -lhs.z))
        }
    }
}
