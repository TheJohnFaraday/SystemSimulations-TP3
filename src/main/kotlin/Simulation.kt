package ar.edu.itba.ss

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import java.util.*

class Simulation(
    private val settings: Settings,
    private val outputChannel: Channel<String>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val logger = KotlinLogging.logger {}

    private var currentTime = 0.0

    /* The priority queue has the lowest t values first */
    private val eventQueue = PriorityQueue<CollisionEvent>()
    private val particleMap = settings.particles.toMutableMap();
    private val eventsProcessor = CollisionProcessor(settings, eventQueue, particleMap)

    private fun processInitialEvents() = particleMap.forEach { (_, particle) ->
        eventsProcessor.processObstaclesCollision(particle, currentTime)
        // Collisions with particles
        if (settings.internalCollisions) {
            eventsProcessor.processParticlesCollision(particle, currentTime)
        }
    }

    suspend fun simulate() = withContext(dispatcher) {
        outputChannel.send("time,id,x,y,vx,vy\n")

        // First events
        processInitialEvents()

        while (eventQueue.isNotEmpty() && currentTime < settings.finalTime) {
            val event = eventQueue.poll()

            val p1 = particleMap[event.particle.id] ?: continue

            // check if is a valid event
            if (event.type == CollisionType.PARTICLE) {
                val p2 = particleMap[event.other?.id] ?: continue
                if (event.collisionCount != p1.collisionCount || event.otherCollisionCount != p2.collisionCount) {
                    continue
                }
            } else {
                if (event.collisionCount != p1.collisionCount) continue
            }

            val dt = event.time - currentTime
            currentTime = event.time

            // Advance all the particles (only position)
            particleMap.replaceAll { _, p -> p.advance(dt) }

            // Save state
            particleMap.values.forEach { p ->
                outputChannel.send("${"%.6f".format(currentTime)},${p.id},${p.x},${p.y},${p.vx},${p.vy}\n")
            }

            // Next step
            eventsProcessor.process(p1, event, currentTime)
        }

        logger.info { "Finished" }
    }
}