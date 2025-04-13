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
        /* The mutableSet avoid add to the queue a duplicate event*/
        val scheduledEvents = mutableSetOf<Pair<Int, CollisionType>>()
        val particleMap = particles.toMutableMap()

        fun scheduleEvent(time: Double, particle: Particle, type: CollisionType) {
            val key = particle.id to type
            if (key !in scheduledEvents) {
                scheduledEvents.add(key)
                eventQueue.add(CollisionEvent(time, particle, type))
            }
        }

        // First events
        for (p in particleMap.values) {
            val tWall = CollisionUtils.timeToWallCollision(p, containerRadius)
            val tObstacle = CollisionUtils.timeToObstacleCollision(p, obstacleRadius)
            if (tWall.isFinite()) scheduleEvent(currentTime + tWall, p, CollisionType.WALL)
            if (tObstacle.isFinite()) scheduleEvent(currentTime + tObstacle, p, CollisionType.OBSTACLE)
        }

        val writer = outputCsv.bufferedWriter()
        writer.write("time,id,x,y,vx,vy\n")

        while (eventQueue.isNotEmpty() && currentTime < finalTime) {
            val event = eventQueue.poll()
            scheduledEvents.remove(event.particle.id to event.type)

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
            if (tWall.isFinite()) scheduleEvent(currentTime + tWall, updatedParticle, CollisionType.WALL)
            if (tObstacle.isFinite()) scheduleEvent(currentTime + tObstacle, updatedParticle, CollisionType.OBSTACLE)
        }

        writer.close()
    }


    /* We only change the sign of the normal */
    private fun reflectNormal(p: Particle): Particle {
        val (vn, vt) = p.toVnVt()
        return Particle.fromVnVt(p, -vn, vt)
    }
}