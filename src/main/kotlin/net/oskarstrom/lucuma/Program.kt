package net.oskarstrom.lucuma

import net.oskarstrom.lucuma.runtime.OperationGroup

@ExperimentalUnsignedTypes
class Program(val name: String, val instructions: Array<OperationGroup>) {
    override fun toString(): String {
        return "Program(name='$name', instructions=${instructions.contentToString()})"
    }
}