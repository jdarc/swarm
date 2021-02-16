package swarm

class Ray(val origin: Vector3, direction: Vector3) {
    val direction = Vector3.normalize(direction)

    fun lerp(t: Double) = origin + direction * t
}
