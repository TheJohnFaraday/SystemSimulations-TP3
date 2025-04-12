package ar.edu.itba.ss

import java.nio.file.Path

data class GeneratorSettings(
    // Params
    val numberOfParticles: Int,
    val radius: Double,
    val mass: Double,
    val initialVelocity: Double,
    val seed: Long,
    // System configuration
    val obstacleRadius: Double,
    val containerRadius: Double
)
