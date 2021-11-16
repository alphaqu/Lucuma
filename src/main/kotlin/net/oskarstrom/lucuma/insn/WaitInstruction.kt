package net.oskarstrom.lucuma.insn

class WaitInstruction(private val time: Int) : Instruction {
    override fun getDuration(): Int = time
}