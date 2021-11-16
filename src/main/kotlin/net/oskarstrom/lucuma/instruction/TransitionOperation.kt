package net.oskarstrom.lucuma.instruction

import net.oskarstrom.lucuma.Fixture
import net.oskarstrom.lucuma.instruction.selector.Selector
import net.oskarstrom.lucuma.instruction.value.Value

@ExperimentalUnsignedTypes
class TransitionOperation(
    selector: Selector,
    private val value: Value,
    private val transitionTime: Int,
    channels: Int,
    fixtures: List<Fixture>
) : Operation {
    private val targetFixtures: List<Fixture> = findFixtures(selector, fixtures)
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
        val delta = Math.delta(startTime, transitionTime)
        for (i in channels.indices) {
            val start = startChannels[i].toInt()
            val end = stopChannels[i].toInt()
            channels[i] = Math.blend(start, end, delta).toUByte()
        }
    }

}