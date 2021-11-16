package net.oskarstrom.lucuma.insn

import net.oskarstrom.lucuma.Fixture
import net.oskarstrom.lucuma.insn.target.Target
import net.oskarstrom.lucuma.insn.value.Value
import kotlin.math.roundToInt

class FadeInstruction(
    private val values: List<Value>,
    private val fadeTime: Int,
    target: Target,
    fixtures: List<Fixture>,
    channels: Int
) : Instruction {

    private val targetFixtures: List<Fixture> = findFixtures(target, fixtures)
    private val fadeChannels = Array(values.size) { UByteArray((channels)) }
    private val channelFilter = BooleanArray(channels)
    private val stepAmount = 1.0 / (values.size - 1)


    init {
        for (fixture in targetFixtures) {
            for (i in fixture.channelStart until fixture.channelStart + fixture.channels) {
                channelFilter[i] = true
            }
        }
    }

    private var startTime = 0L

    override fun start(oldChannels: UByteArray) {
        for (i in oldChannels.indices) {
            for (fadeChannel in fadeChannels) {
                fadeChannel[i] = oldChannels[i]
            }
        }

        for (targetFixture in this.targetFixtures) {
            for ((i, value) in values.withIndex())
                value.apply(fadeChannels[i], targetFixture)
        }

        this.startTime = getTimeMillis()
    }

    override fun render(channels: UByteArray, speed: Double) {
        fun blend(a: Int, b: Int, ratio: Double): Int =
            if (ratio <= 0) a
            else if (ratio >= 1) b
            else (a + (b - a) * ratio).roundToInt()

        val time = getTimeMillis() - startTime
        val rawDelta = time.toDouble() / fadeTime
        val delta = rawDelta.coerceIn(0.0, 1.0)
        for (i in channels.indices) {
            if (channelFilter[i]) {
                val pos = delta / stepAmount
                channels[i] = blend(
                    fadeChannels[pos.toInt()][i].toInt(),
                    fadeChannels[(pos.toInt() + 1).coerceAtMost(values.size - 1)][i].toInt(),
                    (delta % stepAmount) / stepAmount
                ).toUByte()
            }
        }
    }

    override fun getDuration(): Int = 0
}