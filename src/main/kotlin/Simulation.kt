package ar.edu.itba.ss

import ch.obermuhlner.math.big.DefaultBigDecimalMath.createLocalMathContext
import io.github.oshai.kotlinlogging.KotlinLogging
import ch.obermuhlner.math.big.kotlin.bigdecimal.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.*

class Simulation(
    private val settings: Settings,
    private val outputChannel: Channel<String>,
    private val outputChannelClone: Channel<String>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val logger = KotlinLogging.logger {}

    private var currentTime = BigDecimal.ZERO
    private var eventsCounter = 0

    /* The priority queue has the lowest t values first */
    private val eventQueue = PriorityQueue<CollisionEvent>(compareBy { it.time })
    private val particleMap = settings.particles.toMutableMap();
    private val eventsProcessor = CollisionProcessor(settings, eventQueue, particleMap)

    suspend fun simulate() = withContext(dispatcher) {
        // Params
        outputChannel.send("L/2,n,R,m_R,seed\n")
        outputChannel.send(
            listOf(
                "%.8f".format(settings.generatorSettings.containerRadius),
                settings.generatorSettings.numberOfParticles,
                "%.8f".format(settings.generatorSettings.obstacleRadius),
                "%.8f".format(settings.generatorSettings.obstacleMass),
                settings.generatorSettings.seed,
            ).joinToString(separator = ",", postfix = "\n")
        )

        // Header
        outputChannel.send("time,id,x,y,vx,vy,radius,m,r,v_n\n")
        outputChannelClone.send("time,type,d_obstacle,d_wall\n")

        // First events
        eventsProcessor.calculateEventsTime(currentTime)

        createLocalMathContext(16).use {
            while (eventQueue.isNotEmpty() && currentTime < settings.finalTime) {
                val event = eventQueue.poll()
                val p1 = particleMap[event.particle.id] ?: continue

                if (!isValidEvent(p1, event)) continue

                val dt = event.time - currentTime
                if (dt == BigDecimal.ZERO) continue

                currentTime = event.time
                // Advance all the particles (only position)
                particleMap.replaceAll { _, p -> p.advance(dt) }

                // Save current state to file
                saveState()
                // Update particle speed after collision
                val updatedP1 = particleMap[event.particle.id] ?: continue
                cloneState(event, updatedP1)
                eventsProcessor.executeParticleCollision(updatedP1, event)

                eventQueue.clear()
                eventsProcessor.calculateEventsTime(currentTime)
            }

        }

        logger.info { "Finished" }
    }

    private fun isValidEvent(p1: Particle, event: CollisionEvent): Boolean =
        if (event.type == CollisionType.PARTICLE) {
            val p2 = particleMap[event.other?.id]
            !(event.collisionCount != p1.collisionCount
                    || p2 == null
                    || event.otherCollisionCount != p2.collisionCount)
        } else if (event.collisionCount != p1.collisionCount) {
            false
        } else {
            true
        }

    private suspend fun saveState() {
        settings.eventDensity?.let { eventDensity ->
            if (eventsCounter < eventDensity) {
                eventsCounter++

                if (eventQueue.size > 1 && currentTime > BigDecimal.ZERO && currentTime < settings.finalTime) {
                    return;
                }
            }
        }
        particleMap.values.forEach { p ->
            val output = listOf(
                "%.8f".format(currentTime),
                p.id,
                p.x,
                p.y,
                "%.8f".format(p.vx),
                "%.8f".format(p.vy),
                "%.8f".format(p.radius),
                "%.8f".format(p.mass),
                p.polarCoordinates.r,
                "%.8f".format(p.polarVelocity.normal)
            ).joinToString(separator = ",", postfix = "\n")
            outputChannel.send(output)
        }
        eventsCounter = 0
    }

    private suspend fun cloneState(event: CollisionEvent, particle: Particle) {
    val distanceToObstacle =
         particle.polarCoordinates.r - (particle.radius + settings.generatorSettings.obstacleRadius)
        val distanceToWall =
            settings.generatorSettings.containerRadius - (particle.polarCoordinates.r + particle.radius)

        val output = listOf(
            "%.8f".format(currentTime),
            event.type,
            distanceToObstacle,
            distanceToWall
        ).joinToString(separator = ",", postfix = "\n")
        outputChannelClone.send(output)
    }
}