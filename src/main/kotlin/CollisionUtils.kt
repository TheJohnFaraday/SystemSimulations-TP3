package ar.edu.itba.ss

import kotlin.math.sqrt

object CollisionUtils {

    fun timeToWallCollision(p: Particle, containerRadius: Double): Double {
        val r = sqrt(p.x * p.x + p.y * p.y)
        val vn = p.vn
        val distanceToWall = containerRadius - p.radius - r
        return if (vn <= 0) Double.POSITIVE_INFINITY else distanceToWall / vn
    }

    fun timeToObstacleCollision(p: Particle, obstacleRadius: Double): Double {
        val r = sqrt(p.x * p.x + p.y * p.y)
        val vn = p.vn
        val distanceToObstacle = r - obstacleRadius - p.radius
        return if (vn >= 0) Double.POSITIVE_INFINITY else -distanceToObstacle / vn
    }
}
