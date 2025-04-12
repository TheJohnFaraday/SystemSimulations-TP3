package ar.edu.itba.ss

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class ParticleGenerator(
    private val settings: GeneratorSettings
) {

    fun generate(): List<Particle> {
        val random = Random(settings.seed)
        val obstacleRadius = settings.obstacleRadius
        val containerRadius = settings.containerRadius
        val particleRadius = settings.radius
        val particles = mutableListOf<Particle>()

        repeat(settings.numberOfParticles){ id ->
            var x: Double
            var y: Double
            var overlapping: Boolean
            var insideObstacle: Boolean
            var insideContainer: Boolean

            do {
                val angle = random.nextDouble() * 2 * Math.PI
                val r = obstacleRadius + particleRadius + random.nextDouble() * (containerRadius - obstacleRadius - 2 * particleRadius)

                x = r * cos(angle)
                y = r * sin(angle)

                insideObstacle = sqrt(x * x + y * y) < (obstacleRadius + particleRadius)
                insideContainer = sqrt(x * x + y * y) < (containerRadius - particleRadius)

                overlapping = particles.any {
                    Particle.areOverlapping(it, Particle(id, particleRadius, settings.mass, x, y, 0.0, 0.0))
                }

            } while (overlapping || insideObstacle || !insideContainer)

            val (vn, vt) = Particle.randomVelocities(settings.initialVelocity, random)
            val p = Particle(id, particleRadius, settings.mass, x, y, vn, vt)
            particles.add(p)
        }

        return particles
    }
}