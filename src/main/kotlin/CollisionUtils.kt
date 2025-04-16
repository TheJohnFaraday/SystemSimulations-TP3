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

    fun timeToParticleCollision(p1: Particle, p2: Particle): Double {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val dvx = p2.vx - p1.vx
        val dvy = p2.vy - p1.vy

        // scalar product
        val dvdr = dx * dvx + dy * dvy
        if (dvdr >= 0) return Double.POSITIVE_INFINITY // are moving away

        // the collision takes place when the distance between the centers is equal to the sum of the radius
        val dvdv = dvx * dvx + dvy * dvy
        val drdr = dx * dx + dy * dy
        val sigma = p1.radius + p2.radius
        val d = dvdr * dvdr - dvdv * (drdr - sigma * sigma) // (b² - 4ac)

        if (d < 0) return Double.POSITIVE_INFINITY // no collision

        return -(dvdr + sqrt(d)) / dvdv
    }

    /* We only change the sign of the normal */
    fun reflectNormal(p: Particle): Particle {
        val (vn, vt) = p.toVnVt()
        return Particle.fromVnVt(p, -vn, vt).copy(collisionCount = p.collisionCount + 1)
    }

    fun resolveParticleCollision(p1: Particle, p2: Particle): Pair<Particle, Particle> {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val dvx = p2.vx - p1.vx
        val dvy = p2.vy - p1.vy
        val dvdr = dx * dvx + dy * dvy
        val dist = p1.radius + p2.radius

        val j = 2 * p1.mass * p2.mass * dvdr / ((p1.mass + p2.mass) * dist)
        val jx = j * dx / dist
        val jy = j * dy / dist

        val newP1 = p1.copy(
            vx = p1.vx + jx / p1.mass,
            vy = p1.vy + jy / p1.mass,
            collisionCount = p1.collisionCount + 1
        )
        val newP2 = p2.copy(
            vx = p2.vx - jx / p2.mass,
            vy = p2.vy - jy / p2.mass,
            collisionCount = p2.collisionCount + 1
        )

        return Pair(newP1, newP2)
    }
}

