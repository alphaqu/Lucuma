package net.oskarstrom.lucuma.runtime.operation

import net.oskarstrom.lucuma.Fixture
import net.oskarstrom.lucuma.runtime.RuntimeUtils
import net.oskarstrom.lucuma.runtime.target.Target

@ExperimentalUnsignedTypes
abstract class Operation(
    fixtures: List<Fixture>,
    target: Target,
    protected val targets: Array<Fixture> = RuntimeUtils.getTargetFixtures(fixtures, target),
) {
    abstract fun start(channels: UByteArray)
    abstract fun render(channels: UByteArray, speed: Double)

    abstract fun isDone(): Boolean

    abstract fun stop(channels: UByteArray)
}