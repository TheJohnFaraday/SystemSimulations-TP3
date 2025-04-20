package ar.edu.itba.ss

import java.util.*

class CollisionProcessor(
    private val settings: Settings,
    private val eventQueue: PriorityQueue<CollisionEvent>,
    private val particleMap: MutableMap<Int, Particle>
) {
    fun processObstaclesCollision(particle: Particle, currentTime: Double) {
        val timeToWall = CollisionUtils.timeToWallCollision(particle, settings.generatorSettings.containerRadius)
        val timeToObstacle =
            CollisionUtils.timeToObstacleCollision(particle, settings.generatorSettings.obstacleRadius)
        if (timeToWall.isFinite()) {
            eventQueue.add(
                CollisionEvent(
                    currentTime + timeToWall,
                    particle,
                    CollisionType.WALL,
                    particle.collisionCount
                )
            )
        }
        if (timeToObstacle.isFinite()) {
            eventQueue.add(
                CollisionEvent(
                    currentTime + timeToObstacle,
                    particle,
                    CollisionType.OBSTACLE,
                    particle.collisionCount
                )
            )
        }
    }

    fun processParticlesCollision(particle: Particle, currentTime: Double) =
        particleMap
            .filter { (_, other) -> particle.id != other.id } // Avoid (A,B) and (B,A)
            .forEach { (_, other) ->
                val timeToParticle = CollisionUtils.timeToParticleCollision(particle, other)
                if (timeToParticle.isFinite()) {
                    eventQueue.add(
                        CollisionEvent(
                            time = currentTime + timeToParticle,
                            particle = particle,
                            type = CollisionType.PARTICLE,
                            collisionCount = particle.collisionCount,
                            other = other,
                            otherCollisionCount = other.collisionCount
                        )
                    )
                }
            }

    fun process(p1: Particle, event: CollisionEvent, currentTime: Double) =
        when (event.type) {
            /* Reflect the velocities and then recalculate collisions */
            CollisionType.WALL, CollisionType.OBSTACLE -> obstacleEvent(p1, currentTime)

            /* Reflect the velocities and then recalculate collisions */
            CollisionType.PARTICLE -> particleEvent(p1, event, currentTime)
        }

    private fun obstacleEvent(p1: Particle, currentTime: Double) {
        // Reflect the velocities
        val updated = CollisionUtils.reflectNormal(p1)
        particleMap[updated.id] = updated

        // Recalculate Collisions only for this particle
        processObstaclesCollision(updated, currentTime)

        if (settings.internalCollisions) {
            processParticlesCollision(updated, currentTime)
        }
    }

    private fun particleEvent(p1: Particle, event: CollisionEvent, currentTime: Double) {
        val p2 = particleMap[event.other?.id] ?: return

        // Reflect the velocities
        val (newP1, newP2) = CollisionUtils.resolveParticleCollision(p1, p2)
        particleMap[newP1.id] = newP1
        particleMap[newP2.id] = newP2

        // Recalculate Collisions only for both particles
        val newParticles = listOf(newP1, newP2)

        newParticles.forEach { processObstaclesCollision(it, currentTime) }
        newParticles.forEach { processParticlesCollision(it, currentTime) }
    }

}