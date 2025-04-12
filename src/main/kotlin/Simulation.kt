package ar.edu.itba.ss

import java.io.File
import java.util.*

class Simulation {
    fun simulate(
        particles: List<Particle>,
        finalTime: Double,
        containerRadius: Double,
        obstacleRadius: Double,
        outputCsv: File
    ) {
        var currentTime = 0.0
        val eventQueue = PriorityQueue<CollisionEvent>()
        val particleMap = particles.associateBy { it.id }.toMutableMap()

        for (p in particles) {
            val tWall = CollisionUtils.timeToWallCollision(p, containerRadius)
            val tObstacle = CollisionUtils.timeToObstacleCollision(p, obstacleRadius)

            if (tWall.isFinite()) eventQueue.add(CollisionEvent(currentTime + tWall, p, CollisionType.WALL))
            if (tObstacle.isFinite()) eventQueue.add(CollisionEvent(currentTime + tObstacle, p, CollisionType.OBSTACLE))
        }

        val writer = outputCsv.bufferedWriter()
        writer.write("time,id,x,y,vn,vt\n")

        while (eventQueue.isNotEmpty() && currentTime < finalTime) {
            val event = eventQueue.poll()
            val dt = event.time - currentTime
            currentTime = event.time

            // Avanzar todas las partículas
            // particleMap.replaceAll { _, p -> p.advanceTo(currentTime - dt, currentTime) }

            // Guardar estado
            for (p in particleMap.values) {
                writer.write("${"%.6f".format(currentTime)},${p.id},${p.x},${p.y},${p.vn},${p.vt}\n")
            }

            // Calcular nueva velocidad
            val updatedParticle = when (event.type) {
                CollisionType.WALL -> reflectAgainstWall(event.particle)
                CollisionType.OBSTACLE -> reflectAgainstObstacle(event.particle)
            }

            particleMap[updatedParticle.id] = updatedParticle

            // Agregar nuevas colisiones
            val tWall = CollisionUtils.timeToWallCollision(updatedParticle, containerRadius)
            val tObstacle = CollisionUtils.timeToObstacleCollision(updatedParticle, obstacleRadius)

            if (tWall.isFinite()) eventQueue.add(CollisionEvent(currentTime + tWall, updatedParticle, CollisionType.WALL))
            if (tObstacle.isFinite()) eventQueue.add(CollisionEvent(currentTime + tObstacle, updatedParticle, CollisionType.OBSTACLE))
        }

        writer.close()
    }

    private fun reflectAgainstWall(p: Particle): Particle {
        return p.copy(vn = -p.vn)
    }

    private fun reflectAgainstObstacle(p: Particle): Particle {
        return p.copy(vn = -p.vn)
    }

}