package ar.edu.itba.ss

import java.math.BigDecimal

data class CollisionEvent(
    val time: BigDecimal,
    val particle: Particle,
    val type: CollisionType,
    val collisionCount: Int,
    val other: Particle? = null, // only if we have particle collision
    val otherCollisionCount: Int? = null
)