package ar.edu.itba.ss

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class Particle(
    val id: Int,
    val radius: Double,
    val mass: Double,
    val x: Double,
    val y: Double,
    val vx: Double,
    val vy: Double
) {
    companion object {
        fun randomVelocities(v0: Double, random: Random): Pair<Double, Double> {
            // v0 is the initial module of the velocities. We need to generate vn and vt
            val angle = random.nextDouble() * 2 * Math.PI
            val vx = v0 * cos(angle)
            val vy = v0 * sin(angle)
            return Pair(vx, vy)
        }

        fun distanceSquared(p1: Particle, p2: Particle): Double {
            return (p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2)
        }

        fun areOverlapping(p1: Particle, p2: Particle): Boolean {
            val minDist = p1.radius + p2.radius
            return distanceSquared(p1, p2) < minDist.pow(2)
        }

        fun fromVnVt(p: Particle, vn: Double, vt: Double): Particle {
            val r = sqrt(p.x * p.x + p.y * p.y)
            if (r == 0.0) return p.copy(vx = 0.0, vy = 0.0)
            val nx = p.x / r
            val ny = p.y / r

            val vx = vn * nx - vt * ny
            val vy = vn * ny + vt * nx

            return p.copy(vx = vx, vy = vy)
        }
    }

    fun toVnVt(): Pair<Double, Double> {
        val r = sqrt(x * x + y * y)
        if (r == 0.0) return Pair(0.0, 0.0)
        val nx = x / r
        val ny = y / r

        val vn = vx * nx + vy * ny
        val vt = -vx * ny + vy * nx

        return Pair(vn, vt)
    }

    override fun toString(): String {
        return "Particle(id=$id, position=($x, $y), radius=$radius, mass=$mass, velocity=(vx=$vx, vy=$vy))"
    }
}
