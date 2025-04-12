package ar.edu.itba.ss

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.path
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path

class Cli : CliktCommand() {

    private val logger = KotlinLogging.logger{}

    private val numberOfParticles: Int by option("-N", "--number-of-particles")
        .int()
        .default(250)
        .help("Total number of particles")
        .check("Must be greater than 0") { it > 0 }

    private val radius: Double by option("-r", "--radius")
        .double()
        .default(0.0005)
        .help("Radius of the particles [m]")
        .check("Must be greater than 0") { it > 0.0 }

    private val mass: Double by option("-m", "--mass")
        .double()
        .default(1.0)
        .help("Mass of the particles [kg])")
        .check("Must be greater than 0") { it > 0.0 }

    private val initialVelocity: Double by option("-v", "--v0", "--initial-velocity")
        .double()
        .default(1.0)
        .help("Initial velocity of the particles [m/s]")
        .check("Must be non-negative") { it >= 0.0 }

    private val finalTime: Double? by option("-t", "--t-final")
        .double()
        .help("Total simulation time")
        .check("Must be greater than 0") { it > 0.0 }

    private val enableInternalCollisions: Boolean by option("--enable-internal-collisions")
        .flag(default = false)
        .help("Whether internal particles can collide with each other")

    private val seed: Long by option("-s", "--seed")
        .long()
        .default(System.currentTimeMillis())
        .help("[Optional] Seed for the RND")
        .check("Seed must be greater or equal to 0.") { it > 0 }

    private val outputDirectory: Path by option().path(
        canBeFile = false,
        canBeDir = true,
        mustExist = true,
        mustBeReadable = true,
        mustBeWritable = true
    ).required().help("Path to the output directory.")

    override fun run() {
        logger.info { "Starting simulation with the following parameters:" }
        logger.info { "Number of particles: $numberOfParticles" }
        logger.info { "Particle radius: $radius [m]" }
        logger.info { "Particle mass: $mass [kg]" }
        logger.info { "Initial velocity: $initialVelocity [m/s]" }
        logger.info { "Final time: $finalTime [s]" }
        logger.info { "Enable internal collisions: $enableInternalCollisions" }
        logger.info { "Seed: $seed" }
        logger.info { "Output directory: $outputDirectory" }

        val generatorSettings = GeneratorSettings(
            numberOfParticles = numberOfParticles,
            radius = radius,
            mass = mass,
            initialVelocity = initialVelocity,
            seed = seed,
            obstacleRadius = 0.005,
            containerRadius = 0.05
        )

        val particles = ParticleGenerator(generatorSettings).generate()

        //particles.forEach { particle ->
        //    logger.info { "Particle: $particle" }
        //}
    }
}