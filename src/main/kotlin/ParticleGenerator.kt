package ar.edu.itba.ss

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ParticleGenerator(
    private val settings: GeneratorSettings
) {
    private val logger = KotlinLogging.logger {}

    fun generate(): Map<Int, Particle> {
        val obstacleRadius = settings.obstacleRadius
        val containerRadius = settings.containerRadius
        val particleRadius = settings.radius
        val particles = mutableMapOf<Int, Particle>()

        repeat(settings.numberOfParticles) { id ->
            var x: Double
            var y: Double
            var overlapping: Boolean
            var insideObstacle: Boolean
            var insideContainer: Boolean

            do {
                val angle = settings.random.nextDouble() * 2 * Math.PI
                val r = obstacleRadius + particleRadius + settings.random.nextDouble() * (containerRadius - obstacleRadius - 2 * particleRadius)

                x = r * cos(angle)
                y = r * sin(angle)

                insideObstacle = sqrt(x * x + y * y) < (obstacleRadius + particleRadius)
                insideContainer = sqrt(x * x + y * y) < (containerRadius - particleRadius)

                overlapping = particles.values.any {
                    Particle.areOverlapping(it, Particle(id, particleRadius, settings.mass, x, y, 0.0, 0.0))
                }

            } while (overlapping || insideObstacle || !insideContainer)

            val (vx, vy) = Particle.randomVelocities(settings.initialVelocity, settings.random)
            particles[id] = Particle(id, particleRadius, settings.mass, x, y, vx, vy)
        }

        // Special case: obstacle has mass and starts stationary in (0, 0)
        if (settings.obstacleMass != null && settings.obstacleMass > 0.0) {
            particles[settings.numberOfParticles] = Particle(
                settings.numberOfParticles,
                settings.obstacleRadius,
                settings.obstacleMass,
                0.0,
                0.0,
                0.0,
                0.0
            )
        }
        return particles
    }
}