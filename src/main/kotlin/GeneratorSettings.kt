package ar.edu.itba.ss

import java.math.BigDecimal
import kotlin.random.Random

data class GeneratorSettings(
    val random: Random,
    // Params
    val numberOfParticles: Int,
    val radius: BigDecimal,
    val mass: BigDecimal,
    val initialVelocity: BigDecimal,
    val seed: Long,
    // System configuration
    val obstacleRadius: BigDecimal,
    val obstacleMass: BigDecimal?,
    val containerRadius: BigDecimal
)
