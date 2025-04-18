package ar.edu.itba.ss

data class CollisionEvent(
    val time: Double,
    val particle: Particle,
    val type: CollisionType,
    val collisionCount: Int,
    val other: Particle? = null, // only if we have particle collision
    val otherCollisionCount: Int? = null
) : Comparable<CollisionEvent> {
    override fun compareTo(other: CollisionEvent): Int {
        return time.compareTo(other.time)
    }
}
