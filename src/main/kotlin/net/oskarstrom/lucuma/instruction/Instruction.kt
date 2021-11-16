package net.oskarstrom.lucuma.instruction

@ExperimentalUnsignedTypes
class Instruction(val operations: MutableList<Operation>, val delay: Int) {
    fun start(oldChannels: UByteArray) {
        operations.forEach {
            it.start(oldChannels)
        }
    }

    fun render(channels: UByteArray, speed: Double) {
        operations.forEach {
            it.render(channels, speed)
        }
    }

    fun end() {
        operations.forEach { it.stop() }
    }
}