package ar.edu.itba.ss

import java.io.File
import java.util.*

class Simulation {
    fun simulate(
        particles: Map<Int, Particle>,
        finalTime: Double,
        containerRadius: Double,
        obstacleRadius: Double,
        outputCsv: File,
        enableInternalCollisions: Boolean
    ) {
        var currentTime = 0.0
        /* The priority queue has the lowest t values first */
        val eventQueue = PriorityQueue<CollisionEvent>()
        val particleMap = particles.toMutableMap()

        // First events
        for ((_, p1) in particleMap) {
            // Collisions with wall or obstacle
            val tWall = CollisionUtils.timeToWallCollision(p1, containerRadius)
            val tObstacle = CollisionUtils.timeToObstacleCollision(p1, obstacleRadius)
            if (tWall.isFinite()) eventQueue.add(
                CollisionEvent(currentTime + tWall, p1, CollisionType.WALL, p1.collisionCount)
            )
            if (tObstacle.isFinite()) eventQueue.add(
                CollisionEvent(currentTime + tObstacle, p1, CollisionType.OBSTACLE, p1.collisionCount)
            )

            // Collisions with particles
            if (enableInternalCollisions) {
                for ((_, p2) in particleMap) {
                    if (p1.id >= p2.id) continue // Avoid (A,B) and (B,A)
                    val tCollision = CollisionUtils.timeToParticleCollision(p1, p2)
                    if (tCollision.isFinite()) {
                        eventQueue.add(
                            CollisionEvent(
                                time = currentTime + tCollision,
                                particle = p1,
                                type = CollisionType.PARTICLE,
                                collisionCount = p1.collisionCount,
                                other = p2,
                                otherCollisionCount = p2.collisionCount
                            )
                        )
                    }
                }
            }
        }

        val writer = outputCsv.bufferedWriter()
        writer.write("time,id,x,y,vx,vy\n")

        while (eventQueue.isNotEmpty() && currentTime < finalTime) {
            val event = eventQueue.poll()

            val p1 = particleMap[event.particle.id]!!

            // check if is a valid event
            if (event.type == CollisionType.PARTICLE) {
                val p2 = particleMap[event.other!!.id]!!
                if (event.collisionCount != p1.collisionCount || event.otherCollisionCount != p2.collisionCount) {
                    continue
                }
            } else {
                if (event.collisionCount != p1.collisionCount) continue
            }

            val dt = event.time - currentTime
            currentTime = event.time

            /* Advance all the particles (only position)*/
            particleMap.replaceAll { _, p -> p.advance(dt) }

            // Save state
            for (p in particleMap.values) {
                writer.write("${"%.6f".format(currentTime)},${p.id},${p.x},${p.y},${p.vx},${p.vy}\n")
            }

            when (event.type) {
                /* Reflect the velocities and then recalculate collisions */
                CollisionType.WALL, CollisionType.OBSTACLE -> {
                    // Reflect the velocities
                    val updated = reflectNormal(p1)
                    particleMap[updated.id] = updated

                    // Recalculate Collisions only for this particle
                    val tWall = CollisionUtils.timeToWallCollision(updated, containerRadius)
                    val tObstacle = CollisionUtils.timeToObstacleCollision(updated, obstacleRadius)
                    if (tWall.isFinite()) eventQueue.add(
                        CollisionEvent(currentTime + tWall, updated, CollisionType.WALL, updated.collisionCount)
                    )
                    if (tObstacle.isFinite()) eventQueue.add(
                        CollisionEvent(currentTime + tObstacle, updated, CollisionType.OBSTACLE, updated.collisionCount)
                    )

                    if (enableInternalCollisions) {
                        for ((_, other) in particleMap) {
                            if (other.id == updated.id) continue
                            val t = CollisionUtils.timeToParticleCollision(updated, other)
                            if (t.isFinite()) {
                                eventQueue.add(
                                    CollisionEvent(
                                        currentTime + t,
                                        updated,
                                        CollisionType.PARTICLE,
                                        updated.collisionCount,
                                        other,
                                        other.collisionCount
                                    )
                                )
                            }
                        }
                    }
                }

                /* Reflect the velocities and then recalculate collisions */
                CollisionType.PARTICLE -> {
                    val p2 = particleMap[event.other!!.id]!!

                    // Reflect the velocities
                    val (newP1, newP2) = resolveParticleCollision(p1, p2)
                    particleMap[newP1.id] = newP1
                    particleMap[newP2.id] = newP2

                    // Recalculate Collisions only for both particles
                    for (p in listOf(newP1, newP2)) {
                        val tWall = CollisionUtils.timeToWallCollision(p, containerRadius)
                        val tObstacle = CollisionUtils.timeToObstacleCollision(p, obstacleRadius)

                        if (tWall.isFinite()) eventQueue.add(
                            CollisionEvent(currentTime + tWall, p, CollisionType.WALL, p.collisionCount)
                        )
                        if (tObstacle.isFinite()) eventQueue.add(
                            CollisionEvent(currentTime + tObstacle, p, CollisionType.OBSTACLE, p.collisionCount)
                        )
                    }

                    for ((_, other) in particleMap) {
                        if (other.id == newP1.id || other.id == newP2.id) continue

                        for (p in listOf(newP1, newP2)) {
                            val t = CollisionUtils.timeToParticleCollision(p, other)
                            if (t.isFinite()) {
                                eventQueue.add(
                                    CollisionEvent(
                                        currentTime + t,
                                        p,
                                        CollisionType.PARTICLE,
                                        p.collisionCount,
                                        other,
                                        other.collisionCount
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        writer.close()
    }

    /* We only change the sign of the normal */
    private fun reflectNormal(p: Particle): Particle {
        val (vn, vt) = p.toVnVt()
        return Particle.fromVnVt(p, -vn, vt).copy(collisionCount = p.collisionCount + 1)
    }

    private fun resolveParticleCollision(p1: Particle, p2: Particle): Pair<Particle, Particle> {
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