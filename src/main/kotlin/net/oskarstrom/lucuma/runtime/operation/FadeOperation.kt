package net.oskarstrom.lucuma.runtime.operation

import net.oskarstrom.lucuma.Fixture
import net.oskarstrom.lucuma.runtime.Math
import net.oskarstrom.lucuma.runtime.RuntimeUtils
import net.oskarstrom.lucuma.runtime.target.Target
import net.oskarstrom.lucuma.runtime.value.Value

@ExperimentalUnsignedTypes
class FadeOperation(
    fixtures: List<Fixture>,
    target: Target,
    private val duration: Int,
    private val from: Value,
    private val to: Value,

    ) : Operation(fixtures, target) {
    private var startTime = 0L
    private val targetChannels: Array<RuntimeUtils.ChannelData>
    private val fromChannels: UByteArray
    private val toChannels: UByteArray

    init {
        // only from, we should prob throw an error if from and to target different channels
        targetChannels = RuntimeUtils.getChannels(targets, target, from)
        fromChannels = UByteArray(targetChannels.size)
        toChannels = UByteArray(targetChannels.size)
    }

    override fun start(channels: UByteArray) {
        for ((index, local, raw) in targetChannels) {
            val startValue = channels[raw]

            fromChannels[index] = from.apply(startValue, local)
            toChannels[index] = to.apply(startValue, local)
        }


        startTime = System.currentTimeMillis()
    }

    override fun render(channels: UByteArray, speed: Double) {
        val delta = Math.delta(startTime, duration.toDouble())
        for ((index, _, raw) in targetChannels) {
            channels[raw] = Math.blend(fromChannels[index].toInt(), toChannels[index].toInt(), delta).toUByte()
        }
    }

    override fun isDone(): Boolean = startTime + duration < System.currentTimeMillis()

    override fun stop(channels: UByteArray) {
        for ((index, _, raw) in targetChannels) {
            channels[raw] = toChannels[index]
        }
    }
}