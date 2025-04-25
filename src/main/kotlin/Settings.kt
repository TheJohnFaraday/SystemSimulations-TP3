package ar.edu.itba.ss

import java.io.File
import java.math.BigDecimal

data class Settings(
    val outputFile: File,
    val outputFile2: File,
    val generatorSettings: GeneratorSettings,
    val particles: Map<Int, Particle>,
    val finalTime: BigDecimal,
    val internalCollisions: Boolean,
    val eventDensity: Int?
)
