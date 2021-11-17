package net.oskarstrom.lucuma.instruction

import net.oskarstrom.lucuma.Fixture
import net.oskarstrom.lucuma.instruction.selector.Selector
import net.oskarstrom.lucuma.instruction.value.Value

@ExperimentalUnsignedTypes
class FadeOperation(
    private val values: List<Value>,
    private val fadeTime: Int,
    selector: Selector,
    fixtures: List<Fixture>,
    channels: Int
) : Operation {

    private val targetFixtures: List<Fixture> = findFixtures(selector, fixtures)
    private val fadeChannels = Array(values.size) { UByteArray((channels)) }
    private val channelFilter = BooleanArray(channels)
    private val stepAmount = 1.0 / (values.size - 1)
    private var startTime = 0L

    init {
        for (fixture in targetFixtures) {
            for (i in fixture.channelStart until fixture.channelStart + fixture.channels) {
                channelFilter[i] = true
            }
        }
    }

    override fun start(oldChannels: UByteArray) {
        for (i in oldChannels.indices) {
            for (fadeChannel in fadeChannels) {
                fadeChannel[i] = oldChannels[i]
            }
        }

        for (targetFixture in this.targetFixtures) {
            for ((i, value) in values.withIndex()) {
                value.apply(fadeChannels[i], targetFixture)
                value.tick()
            }
        }

        this.startTime = System.currentTimeMillis()
    }

    override fun render(channels: UByteArray, speed: Double) {

        val delta = Math.delta(startTime, fadeTime)
        for (i in channels.indices) {
            if (channelFilter[i]) {
                val pos = delta / stepAmount
                channels[i] = Math.blend(
                    fadeChannels[pos.toInt()][i].toInt(),
                    fadeChannels[(pos.toInt() + 1).coerceAtMost(values.size - 1)][i].toInt(),
                    (delta % stepAmount) / stepAmount
                ).toUByte()
            }
        }
    }
}