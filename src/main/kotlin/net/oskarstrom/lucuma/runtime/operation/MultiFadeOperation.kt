package net.oskarstrom.lucuma.runtime.operation

import net.oskarstrom.lucuma.Fixture
import net.oskarstrom.lucuma.runtime.Math
import net.oskarstrom.lucuma.runtime.RuntimeUtils
import net.oskarstrom.lucuma.runtime.target.Target
import net.oskarstrom.lucuma.runtime.value.Value

@ExperimentalUnsignedTypes
class MultiFadeOperation(
    fixtures: List<Fixture>,
    target: Target,
    private val duration: Int,
    private val values: List<Value>
) : Operation(fixtures, target) {
    private var startTime = 0L
    private val targetChannels: Array<RuntimeUtils.ChannelData>
    private val fadeChannels: Array<UByteArray>
    private val stepAmount = 1.0 / (values.size - 1)


    init {
        // only from, we should prob throw an error if from and to target different channels
        targetChannels = RuntimeUtils.getChannels(targets, target, values[0])
        fadeChannels = Array(values.size) { UByteArray(targetChannels.size) }
    }

    override fun start(channels: UByteArray) {
        for ((valueIndex, value) in values.withIndex()) {
            val fadeArray = fadeChannels[valueIndex]
            for ((channelIndex, local, raw) in targetChannels) {
                fadeArray[channelIndex] = value.apply(channels[raw], local)
            }
        }

        startTime = System.currentTimeMillis()
    }

    override fun render(channels: UByteArray, speed: Double) {
        val delta = Math.delta(startTime, duration.toDouble())
        val fadePos = (delta / stepAmount).toInt()
        val ratio = (delta % stepAmount) / stepAmount

        for ((index, _, raw) in targetChannels) {
            channels[raw] = Math.blend(
                fadeChannels[fadePos][index].toInt(),
                fadeChannels[(fadePos + 1).coerceAtMost(fadeChannels.size - 1)][index].toInt(),
                ratio
            ).toUByte()
        }
    }

    override fun isDone(): Boolean = startTime + duration < System.currentTimeMillis()

    override fun stop(channels: UByteArray) {
        for ((index, _, raw) in targetChannels) {
            channels[raw] = fadeChannels[fadeChannels.size - 1][index]
        }
    }
}