package ar.edu.itba.ss

import java.util.*

class CollisionProcessor(
    private val settings: Settings,
    private val eventQueue: PriorityQueue<CollisionEvent>,
    private val particleMap: MutableMap<Int, Particle>
) {
    fun executeParticleCollision(p1: Particle, event: CollisionEvent) =
        when (event.type) {
            /* Reflect the velocities and then recalculate collisions */
            CollisionType.WALL -> wallEvent(p1)
            CollisionType.OBSTACLE -> obstacleEvent(p1)
            /* Reflect the velocities and then recalculate collisions */
            CollisionType.PARTICLE -> particleEvent(p1, event)
        }

    fun calculateEventsTime(currentTime: Double) = particleMap.forEach { (_, particle) ->
        processWallCollision(particle, currentTime)
        processObstaclesCollision(particle, currentTime)
        // Collisions with particles
        if (settings.internalCollisions) {
            processParticlesCollision(particle, currentTime)
        }
    }


    private fun processWallCollision(particle: Particle, currentTime: Double) {
        val timeToWall =
            CollisionUtils.timeToWallCollision(particle, settings.generatorSettings.containerRadius)
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
    }

    private fun processObstaclesCollision(particle: Particle, currentTime: Double) {
        if (settings.generatorSettings.obstacleMass != null && settings.generatorSettings.obstacleMass > 0.0) {
            // Special case: will get calculated as another particle
            return;
        }
        val timeToObstacle =
            CollisionUtils.timeToObstacleCollision(particle, settings.generatorSettings.obstacleRadius)
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

    private fun processParticlesCollision(particle: Particle, currentTime: Double) =
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

    private fun wallEvent(p1: Particle) {
        // Reflect the velocities
        val updated = CollisionUtils.reflectNormal(p1)
        particleMap[updated.id] = updated
    }

    private fun obstacleEvent(p1: Particle) {
        // Reflect the velocities
        val updated = CollisionUtils.reflectNormal(p1)
        particleMap[updated.id] = updated
    }

    private fun particleEvent(p1: Particle, event: CollisionEvent) {
        val p2 = particleMap[event.other?.id] ?: return

        // Reflect the velocities
        val (newP1, newP2) = CollisionUtils.resolveParticleCollision(p1, p2)
        particleMap[newP1.id] = newP1
        particleMap[newP2.id] = newP2
    }

}