package net.oskarstrom.lucuma

import net.oskarstrom.lucuma.instruction.Instruction
import net.oskarstrom.lucuma.io.DmxIO
import java.util.*

@ExperimentalUnsignedTypes
class ProgramExecutor(
    private val programs: Map<String, Program>,
    private val fixtures: List<Fixture>,
    private val io: DmxIO,
    channels: Int
) {
    private val emptyInstruction = Instruction(ArrayList(), 30)
    private var currentProgram = programs.getOrDefault("main", Program("main", arrayOf(emptyInstruction)))
    private var groupPos = 0
    private var instructionQueue = ArrayDeque<Instruction>()
    private var currentInstructionQueue = ArrayDeque<Instruction>()

    private var paddingSize = 3
    private var programTime = 0L
    private var speed = 0.0

    private var channels = UByteArray(channels)
    private var readChannels = UByteArray(channels)

    fun launch() {
        programTime = System.currentTimeMillis()
        Thread() { while (true) tickReader() }.start()
        Thread() { while (true) tickDrawer() }.start()

        while (true) {
            readLine()?.let {
                if (it.startsWith("p:")) {
                    setCurrentProgram(it)
                }
            }
        }
    }

    private fun setCurrentProgram(name: String) {
        currentProgram = programs[name] ?: run {
            println("Could not find program $name")
            Program("main", arrayOf(emptyInstruction))
        }
        groupPos = 0
    }

    private fun tickDrawer() {
        Thread.sleep(40)
        while (currentInstructionQueue.size > 1) {
            currentInstructionQueue.poll().end(channels)
        }

        if (currentInstructionQueue.size > 0) {
            val current = currentInstructionQueue.peek()
            current.render(channels, speed)
            io.send(channels, fixtures)
        }
    }


    private fun tickReader() {
        if (instructionQueue.size < paddingSize) pushGroup()


        val inst = instructionQueue.poll()

        // align time
        val instructionDuration = inst.delay


        inst.start(readChannels)
        val speedMultiply = getSpeed(instructionDuration)
        speed = speedMultiply
        currentInstructionQueue.add(inst)
        Thread.sleep((inst.delay / speedMultiply).toLong())
        Thread.sleep((Math.random() * 100).toLong())
        inst.render(readChannels, speedMultiply)
        inst.end(readChannels)
        programTime += instructionDuration
    }

    private fun getSpeed(instructionDuration: Int): Double {
        val realTime = System.currentTimeMillis()
        var speedMultiply = 1.0
        if (realTime < programTime) {
            val l = programTime - realTime
            speedMultiply = 1 - (l / instructionDuration.toDouble())
        } else if (programTime < realTime) {
            val timeBehind = realTime - programTime
            speedMultiply = if (timeBehind > instructionDuration) 1000.0
            else instructionDuration / (instructionDuration - timeBehind.toDouble())
        }

        return speedMultiply
    }

    private fun pushGroup() {
        val groups = currentProgram.instructions


        while (instructionQueue.size < paddingSize) {
            if (groupPos >= groups.size)
                groupPos = 0
            instructionQueue.push(groups[groupPos++])
        }
    }
}
