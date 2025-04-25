package ar.edu.itba.ss

import ch.obermuhlner.math.big.DefaultBigDecimalMath.*
import ch.obermuhlner.math.big.kotlin.bigdecimal.*
import java.math.BigDecimal
import kotlin.random.Random

data class PolarCoordinates(
    val r: BigDecimal,
    val angle: BigDecimal
)

data class PolarVelocity(
    val normal: BigDecimal,
    val tangential: BigDecimal
)

data class Particle(
    val id: Int,
    val radius: BigDecimal,
    val mass: BigDecimal,
    val x: BigDecimal,
    val y: BigDecimal,
    val vx: BigDecimal,
    val vy: BigDecimal,
    val collisionCount: Int = 0
) {
    val polarCoordinates: PolarCoordinates by lazy { toPolar() }
    val polarVelocity: PolarVelocity by lazy { toVnVt() }

    companion object {
        fun randomVelocities(v0: BigDecimal, random: Random): Pair<BigDecimal, BigDecimal> {
            // v0 is the initial module of the velocities. We need to generate vx and vy
            val angle = BigDecimal.valueOf(random.nextDouble() * 2 * Math.PI)
            val vx = v0 * cos(angle)
            val vy = v0 * sin(angle)
            return Pair(vx, vy)
        }

        fun distanceSquared(p1: Particle, p2: Particle): BigDecimal {
            return pow(p1.x - p2.x, 2) + pow(p1.y - p2.y, 2)
        }

        fun areOverlapping(p1: Particle, p2: Particle): Boolean {
            val minDist = p1.radius + p2.radius
            return distanceSquared(p1, p2) < minDist.pow(2)
        }

        fun fromVnVt(p: Particle, vn: BigDecimal, vt: BigDecimal): Particle {
            if (p.polarCoordinates.r == BigDecimal.ZERO) return p.copy(
                vx = BigDecimal.ZERO,
                vy = BigDecimal.ZERO
            )
            val nx = p.x / p.polarCoordinates.r
            val ny = p.y / p.polarCoordinates.r

            val vx = vn * nx - vt * ny
            val vy = vn * ny + vt * nx

            return p.copy(vx = vx, vy = vy)
        }
    }

    fun advance(dt: BigDecimal): Particle {
        return this.copy(
            x = x + vx * dt,
            y = y + vy * dt
        )
    }

    private fun toVnVt(): PolarVelocity {
        if (polarCoordinates.r == BigDecimal.ZERO) return PolarVelocity(BigDecimal.ZERO, BigDecimal.ZERO)
        val nx = x / polarCoordinates.r
        val ny = y / polarCoordinates.r

        val vn = vx * nx + vy * ny
        val vt = -vx * ny + vy * nx

        return PolarVelocity(normal = vn, tangential = vt)
    }

    private fun toPolar(): PolarCoordinates =
        if (x == BigDecimal.ZERO && y == BigDecimal.ZERO)
            PolarCoordinates(BigDecimal.ZERO, BigDecimal.ZERO)
        else PolarCoordinates(
            r = sqrt(x * x + y * y),
            angle = atan2(y, x)
        )

    override fun toString(): String {
        return "Particle(id=$id, position=($x, $y), radius=$radius, mass=$mass, velocity=(vx=$vx, vy=$vy))"
    }
}
