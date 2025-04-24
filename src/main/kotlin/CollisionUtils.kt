package ar.edu.itba.ss

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

object CollisionUtils {
    private val logger = KotlinLogging.logger {}

    /* Using normal simplifies the problem. If vn>0 the particle is getting closer to the wall */
    fun timeToWallCollision(p: Particle, containerRadius: Double): Double {
        val velocity = p.polarVelocity
        if (velocity.normal <= 0) {
            return Double.POSITIVE_INFINITY
        }
        val distanceToWall = containerRadius - p.polarCoordinates.r - p.radius
        return distanceToWall / velocity.normal
    }

    /* Using normal simplifies the problem. If vn<0 the particle is getting closer to the obstacle */
    fun timeToObstacleCollision(p: Particle, obstacleRadius: Double): Double {
        val velocity = p.polarVelocity
        if (velocity.normal >= 0) {
            return Double.POSITIVE_INFINITY
        }
        val distanceToObstacle = p.polarCoordinates.r - obstacleRadius - p.radius
        return -distanceToObstacle / velocity.normal
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
        val velocity = p.polarVelocity
        return Particle.fromVnVt(p, -velocity.normal, velocity.tangential).copy(collisionCount = p.collisionCount + 1)
    }

    fun resolveParticleCollision(p1: Particle, p2: Particle): Pair<Particle, Particle> {
        // 1. Calcular vector distancia actual
        var dx = p2.x - p1.x
        var dy = p2.y - p1.y
        val currentDist = sqrt(dx * dx + dy * dy)
        val sumRadii = p1.radius + p2.radius

        // 2. Ajustar posiciones para garantizar contacto exacto (Opción A)
        if (abs(currentDist - sumRadii) > 1e-10) {  // Evitar ajuste innecesario si ya están bien
            val correctionFactor = sumRadii / currentDist
            dx *= correctionFactor
            dy *= correctionFactor
            // Actualizamos p2 para que esté exactamente a r1 + r2 de p1
            val correctedP2 = p2.copy(
                x = p1.x + dx,
                y = p1.y + dy
            )
            // Usamos p2 corregida en los cálculos siguientes
            return resolveCorrectedCollision(p1, correctedP2)
        } else {
            return resolveCorrectedCollision(p1, p2)
        }
    }

    // Función auxiliar que asume partículas ya en contacto exacto
    private fun resolveCorrectedCollision(p1: Particle, p2: Particle): Pair<Particle, Particle> {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val dvx = p2.vx - p1.vx
        val dvy = p2.vy - p1.vy
        val dvdr = dx * dvx + dy * dvy
        val dist = p1.radius + p2.radius

        val j = 2.0 * p1.mass * p2.mass * dvdr / ((p1.mass + p2.mass) * dist)
        val jx = j * dx / dist
        val jy = j * dy / dist

        return Pair(
            p1.copy(
                vx = p1.vx + jx / p1.mass,
                vy = p1.vy + jy / p1.mass,
                collisionCount = p1.collisionCount + 1
            ),
            p2.copy(
                vx = p2.vx - jx / p2.mass,
                vy = p2.vy - jy / p2.mass,
                collisionCount = p2.collisionCount + 1
            )
        )
    }

    fun areParticlesWithinBorders(
        particles: Map<Int, Particle>,
        generatorSettings: GeneratorSettings
    ): Boolean {
        val withinContainer = particles.values.filter { p ->
            p.polarCoordinates.r + p.radius >= generatorSettings.containerRadius
        }
        if (withinContainer.isNotEmpty()) {
            logger.error { "Outside of container: ${withinContainer.first()}" }
        }
        val outsideObstacle = particles.values.filter { p ->
            sqrt((p.x - 0.0).pow(2) + (p.y - 0.0).pow(2)) <= p.radius + generatorSettings.obstacleRadius
        }
        if (outsideObstacle.isNotEmpty()) {
            logger.error { "Inside obstacle: ${outsideObstacle.first()}" }
        }

        return withinContainer.isEmpty() && outsideObstacle.isEmpty();
    }
}

