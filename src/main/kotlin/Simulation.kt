package ar.edu.itba.ss

import java.io.File
import java.util.*

class Simulation {
    fun simulate(
        particles: Map<Int, Particle>,
        finalTime: Double,
        containerRadius: Double,
        obstacleRadius: Double,
        outputCsv: File
    ) {
        var currentTime = 0.0
        /* The priority queue has the lowest t values first */
        val eventQueue = PriorityQueue<CollisionEvent>()
        val particleMap = particles.toMutableMap()

        // First events
        for (p in particleMap.values) {
            val tWall = CollisionUtils.timeToWallCollision(p, containerRadius)
            val tObstacle = CollisionUtils.timeToObstacleCollision(p, obstacleRadius)
            if (tWall.isFinite()) eventQueue.add(CollisionEvent(currentTime + tWall, p,
                CollisionType.WALL, p.collisionCount))
            if (tObstacle.isFinite()) eventQueue.add(CollisionEvent(currentTime + tObstacle, p,
                CollisionType.OBSTACLE, p.collisionCount))
        }

        val writer = outputCsv.bufferedWriter()
        writer.write("time,id,x,y,vx,vy\n")

        while (eventQueue.isNotEmpty() && currentTime < finalTime) {
            val event = eventQueue.poll()

            val particle = particleMap[event.particle.id]!!

            // Check if is a valid event
            if (event.collisionCount != particle.collisionCount) {
                continue
            }

            val dt = event.time - currentTime
            currentTime = event.time

            /* Advance all the particles (only position)*/
            particleMap.replaceAll { _, p -> p.advance(dt) }

            for (p in particleMap.values) {
                // Save state
                writer.write("${"%.6f".format(currentTime)},${p.x},${p.y},${p.vx},${p.vy}\n")
            }

            //  Reflect velocity
            val updatedParticle = when (event.type) {
                CollisionType.WALL -> reflectNormal(event.particle)
                CollisionType.OBSTACLE -> reflectNormal(event.particle)
            }

            particleMap[updatedParticle.id] = updatedParticle

            // Recalculate collisions
            val tWall = CollisionUtils.timeToWallCollision(updatedParticle, containerRadius)
            val tObstacle = CollisionUtils.timeToObstacleCollision(updatedParticle, obstacleRadius)
            if (tWall.isFinite()) eventQueue.add(CollisionEvent(currentTime + tWall, updatedParticle,
                CollisionType.WALL, updatedParticle.collisionCount))
            if (tObstacle.isFinite()) eventQueue.add(CollisionEvent(currentTime + tObstacle, updatedParticle,
                CollisionType.OBSTACLE, updatedParticle.collisionCount))
        }

        writer.close()
    }


    /* We only change the sign of the normal */
    private fun reflectNormal(p: Particle): Particle {
        val (vn, vt) = p.toVnVt()
        return Particle.fromVnVt(p, -vn, vt).copy(collisionCount = p.collisionCount + 1)
    }
}