package net.oskarstrom.lucuma

import net.oskarstrom.lucuma.insn.Instruction

class Group(val instructions: ArrayList<Instruction>, val delay: Int) {

    fun start(oldChannels: UByteArray) {
        for (instruction in instructions) {
            instruction.start(oldChannels)
        }
    }

    fun render(channels: UByteArray, speed: Double) {
        for (instruction in instructions) {
            instruction.render(channels, speed)
        }
    }

    fun end() {
        instructions.forEach { it.stop() }
    }
}