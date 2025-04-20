package ar.edu.itba.ss

import java.io.File

data class Settings(
    val outputFile: File,
    val generatorSettings: GeneratorSettings,
    val particles: Map<Int, Particle>,
    val finalTime: Double,
    val internalCollisions: Boolean,
    val eventDensity: Int?
)
