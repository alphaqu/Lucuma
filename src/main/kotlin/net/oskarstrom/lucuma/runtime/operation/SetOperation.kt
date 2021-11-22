package net.oskarstrom.lucuma.runtime.operation

import net.oskarstrom.lucuma.Fixture
import net.oskarstrom.lucuma.runtime.RuntimeUtils
import net.oskarstrom.lucuma.runtime.target.Target
import net.oskarstrom.lucuma.runtime.value.Value

@ExperimentalUnsignedTypes
class SetOperation(
    fixtures: List<Fixture>,
    target: Target,
    private val value: Value
) : Operation(fixtures, target) {

    private val targetChannels: Array<RuntimeUtils.ChannelData>

    init {
        targetChannels = RuntimeUtils.getChannels(targets, target, value)
    }

    override fun start(channels: UByteArray) {
        for ((index, local, raw) in targetChannels) {
            channels[raw] = value.apply(channels[raw], local)
        }
    }

    override fun render(channels: UByteArray, speed: Double) = Unit
    override fun isDone(): Boolean = true
    override fun stop(channels: UByteArray) = Unit
}