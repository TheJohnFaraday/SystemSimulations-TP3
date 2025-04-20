package ar.edu.itba.ss

import kotlin.random.Random

data class GeneratorSettings(
    val random: Random,
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
