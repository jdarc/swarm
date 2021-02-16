package swarm

import kotlin.math.sqrt

class Sphere(var position: Vector3, radius: Double = 1.0, val color: Color = Color.BLACK) {
    private var velocity = Vector3.ZERO
    private var acceleration = Vector3.ZERO

    var radius = radius
        set(radius) {
            field = radius.coerceAtLeast(1.0)
        }

    fun intersect(ray: Ray): Double {
        val oc = ray.origin - position
        val a = Vector3.dot(ray.direction, ray.direction)
        val b = 2.0 * Vector3.dot(oc, ray.direction)
        val c = Vector3.dot(oc, oc) - radius * radius
        val discriminant = b * b - 4 * a * c
        return if (discriminant < 0) -1.0 else (-b - sqrt(discriminant)) / (2.0 * a)
    }

    fun applyForce(x: Double, y: Double, z: Double) {
        val strength = 400.0
        val ax = if (position.x < x) strength else -strength
        val ay = if (position.y < y) strength else -strength
        val az = if (position.z < z) strength else -strength
        acceleration = Vector3(ax, ay, az)
    }

    fun integrate(delta: Double) {
        velocity += acceleration * delta
        position += velocity * delta
    }
}
