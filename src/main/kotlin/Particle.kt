package ar.edu.itba.ss

import kotlin.math.*
import kotlin.random.Random

data class PolarCoordinates(
    val r: Double,
    val angle: Double
)

data class PolarVelocity(
    val normal: Double,
    val tangential: Double
)

data class Particle(
    val id: Int,
    val radius: Double,
    val mass: Double,
    val x: Double,
    val y: Double,
    val vx: Double,
    val vy: Double,
    val collisionCount: Int = 0
) {
    val polarCoordinates: PolarCoordinates by lazy { toPolar() }
    val polarVelocity: PolarVelocity by lazy { toVnVt() }

    companion object {
        fun randomVelocities(v0: Double, random: Random): Pair<Double, Double> {
            // v0 is the initial module of the velocities. We need to generate vx and vy
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
            if (p.polarCoordinates.r == 0.0) return p.copy(vx = 0.0, vy = 0.0)
            val nx = p.x / p.polarCoordinates.r
            val ny = p.y / p.polarCoordinates.r

            val vx = vn * nx - vt * ny
            val vy = vn * ny + vt * nx

            return p.copy(vx = vx, vy = vy)
        }
    }

    fun advance(dt: Double): Particle {
        return this.copy(
            x = x + vx * dt,
            y = y + vy * dt
        )
    }

    private fun toVnVt(): PolarVelocity {
        if (polarCoordinates.r == 0.0) return PolarVelocity(0.0, 0.0)
        val nx = x / polarCoordinates.r
        val ny = y / polarCoordinates.r

        val vn = vx * nx + vy * ny
        val vt = -vx * ny + vy * nx

        return PolarVelocity(normal = vn, tangential = vt)
    }

    private fun toPolar(): PolarCoordinates {
        return PolarCoordinates(
            r = sqrt(x * x + y * y),
            angle = atan2(y, x)
        )
    }

    override fun toString(): String {
        return "Particle(id=$id, position=($x, $y), radius=$radius, mass=$mass, velocity=(vx=$vx, vy=$vy))"
    }
}
