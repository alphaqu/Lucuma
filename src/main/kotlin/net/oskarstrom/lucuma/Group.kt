package net.oskarstrom.lucuma

import net.oskarstrom.lucuma.insn.Instruction

class Group(val instructions: List<Instruction>, val delay: Int) {
    fun start(oldChannels: UByteArray) {
        instructions.forEach {
            it.start(oldChannels)
        }
    }

    fun render(channels: UByteArray, speed: Double) {
        instructions.forEach {
            it.render(channels, speed)
        }
    }

    fun end() {
        instructions.forEach { it.stop() }
    }
}