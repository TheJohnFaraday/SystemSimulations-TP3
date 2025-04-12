package ar.edu.itba.ss

data class CollisionEvent(
    val time: Double,
    val particle: Particle,
    val type: CollisionType
) : Comparable<CollisionEvent> {
    override fun compareTo(other: CollisionEvent): Int {
        return time.compareTo(other.time)
    }
}