package net.oskarstrom.lucuma

import net.oskarstrom.lucuma.instruction.Instruction

@ExperimentalUnsignedTypes
class Program(val name: String, val instructions: Array<Instruction>) {
    override fun toString(): String {
        return "Program(name='$name', instructions=${instructions.contentToString()})"
    }
}