package net.oskarstrom.lucuma.instruction

import kotlin.math.roundToInt

object Math {
    fun blend(a: Int, b: Int, ratio: Double): Int =
        if (ratio <= 0) a
        else if (ratio >= 1) b
        else (a + (b - a) * ratio).roundToInt()

    fun delta(startTime: Long, duration: Int): Double {
        val time = System.currentTimeMillis() - startTime
        val rawDelta = time.toDouble() / duration
        return rawDelta.coerceIn(0.0, 1.0)
    }
}