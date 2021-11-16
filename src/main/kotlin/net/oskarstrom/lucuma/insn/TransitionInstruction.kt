package net.oskarstrom.lucuma.insn

import net.oskarstrom.lucuma.Fixture
import net.oskarstrom.lucuma.insn.target.Target
import net.oskarstrom.lucuma.insn.value.Value
import kotlin.math.roundToInt

class TransitionInstruction(
    target: Target,
    private val value: Value,
    private val transitionTime: Int,
    channels: Int,
    fixtures: List<Fixture>
) : Instruction {
    private val targetFixtures: List<Fixture> = findFixtures(target, fixtures)
    private val startChannels = UByteArray(channels)
    private val stopChannels = UByteArray(channels)
    private var startTime = 0L

    override fun start(oldChannels: UByteArray) {
        for (i in oldChannels.indices) {
            startChannels[i] = oldChannels[i]
            stopChannels[i] = oldChannels[i]
        }

        for (targetFixture in this.targetFixtures) {
            value.apply(this.stopChannels, targetFixture)
        }

        startTime = System.currentTimeMillis()
    }

    // TODO: deduplicate code that has in common with FadeInstruction
    override fun render(channels: UByteArray, speed: Double) {
        fun blend(a: Int, b: Int, ratio: Double): Int =
            if (ratio <= 0) a
            else if (ratio >= 1) b
            else (a + (b - a) * ratio).roundToInt()

        val time = System.currentTimeMillis() - startTime
        val rawDelta = time.toDouble() / transitionTime
        val delta = rawDelta.coerceIn(0.0, 1.0)
        for (i in channels.indices) {
            val start = startChannels[i].toInt()
            val end = stopChannels[i].toInt()
            channels[i] = blend(start, end, delta).toUByte()
        }
    }

    override fun getDuration(): Int = 0
}