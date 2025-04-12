package ar.edu.itba.ss

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val id: Int,
    val radius: Double,
    val mass: Double,
    val x: Double,
    val y: Double,
    val vn: Double,
    val vt: Double
) {
    companion object {
        fun randomVelocities(v0: Double, random: Random): Pair<Double, Double> {
            // v0 is the initial module of the velocities. We need to generate vn and vt
            val angle = random.nextDouble() * 2 * Math.PI
            val vn = v0 * cos(angle)
            val vt = v0 * sin(angle)
            return Pair(vn, vt)
        }

        fun distanceSquared(p1: Particle, p2: Particle): Double {
            return (p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2)
        }

        fun areOverlapping(p1: Particle, p2: Particle): Boolean {
            val minDist = p1.radius + p2.radius
            return distanceSquared(p1, p2) < minDist.pow(2)
        }
    }

    override fun toString(): String {
        return "Particle(id=$id, position=($x, $y), radius=$radius, mass=$mass, velocity=(vn=$vn, vt=$vt))"
    }
}
