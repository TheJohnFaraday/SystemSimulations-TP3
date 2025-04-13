package ar.edu.itba.ss

import kotlin.math.sqrt

object CollisionUtils {

    /* Using normal simplifies the problem. If vn>0 the particle is getting closer to the wall */
    fun timeToWallCollision(p: Particle, containerRadius: Double): Double {
        val r = sqrt(p.x * p.x + p.y * p.y)
        val vn = p.toVnVt().first
        val distanceToWall = containerRadius - p.radius - r
        return if (vn <= 0) Double.POSITIVE_INFINITY else distanceToWall / vn
    }

    /* Using normal simplifies the problem. If vn<0 the particle is getting closer to the obstacle */
    fun timeToObstacleCollision(p: Particle, obstacleRadius: Double): Double {
        val r = sqrt(p.x * p.x + p.y * p.y)
        val vn = p.toVnVt().first
        val distanceToObstacle = r - obstacleRadius - p.radius
        return if (vn >= 0) Double.POSITIVE_INFINITY else -distanceToObstacle / vn
    }
}

