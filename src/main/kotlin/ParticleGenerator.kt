package ar.edu.itba.ss

import ch.obermuhlner.math.big.DefaultBigDecimalMath.*
import ch.obermuhlner.math.big.kotlin.bigdecimal.minus
import ch.obermuhlner.math.big.kotlin.bigdecimal.plus
import ch.obermuhlner.math.big.kotlin.bigdecimal.times
import io.github.oshai.kotlinlogging.KotlinLogging
import java.math.BigDecimal

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
            var x: BigDecimal
            var y: BigDecimal
            var overlapping: Boolean
            var insideObstacle: Boolean
            var insideContainer: Boolean

            do {
                val angle = BigDecimal.valueOf(settings.random.nextDouble() * 2 * Math.PI)
                val r =
                    obstacleRadius + particleRadius + settings.random.nextDouble() * (containerRadius - obstacleRadius - 2 * particleRadius)

                x = r * cos(angle)
                y = r * sin(angle)

                insideObstacle = sqrt(x * x + y * y) < (obstacleRadius + particleRadius)
                insideContainer = sqrt(x * x + y * y) < (containerRadius - particleRadius)

                overlapping = particles.values.any {
                    Particle.areOverlapping(
                        it,
                        Particle(id, particleRadius, settings.mass, x, y, BigDecimal.ZERO, BigDecimal.ZERO)
                    )
                }

            } while (overlapping || insideObstacle || !insideContainer)

            val (vx, vy) = Particle.randomVelocities(settings.initialVelocity, settings.random)
            particles[id] = Particle(id, particleRadius, settings.mass, x, y, vx, vy)
        }

        // Special case: obstacle has mass and starts stationary in (0, 0)
        if (settings.obstacleMass != null && settings.obstacleMass > BigDecimal.ZERO) {
            particles[settings.numberOfParticles] = Particle(
                settings.numberOfParticles,
                settings.obstacleRadius,
                settings.obstacleMass,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
            )
        }
        return particles
    }
}