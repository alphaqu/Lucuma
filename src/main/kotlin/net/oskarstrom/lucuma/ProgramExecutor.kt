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
    private var currentInstruction: Instruction = emptyInstruction

    private var paddingSize = 3
    private var programTime = 0L

    private var channels = UByteArray(channels)
    private var readChannels = UByteArray(channels)

    fun launch() {
        programTime = System.currentTimeMillis()
        Thread() { while (true) tickReader() }.start()
        Thread() { while (true) tickDrawer() }.start()

        while (true) {
            readLine()?.let { setCurrentProgram(it) }
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

        currentInstruction.render(channels, 1.0)
        io.send(channels, fixtures)
    }


    private fun tickReader() {
        if (instructionQueue.size < paddingSize) pushGroup()


        val group = instructionQueue.poll()

        // align time
        val realTime = System.currentTimeMillis()
        val instructionDuration = group.delay

        var speedMultiply = 1.0
        if (realTime < programTime) {
            val timeAhead = programTime - realTime
            Thread.sleep(timeAhead)
        } else if (programTime < realTime) {
            val timeBehind = realTime - programTime
            speedMultiply = if (timeBehind > instructionDuration) 10.0
            else instructionDuration / (instructionDuration - timeBehind.toDouble())
        }

        group.start(readChannels)
        currentInstruction = group
        val toLong = (group.delay.toLong() / speedMultiply).toLong()
        println(toLong)
        Thread.sleep(toLong)
        currentInstruction.render(readChannels, speedMultiply)
        group.end()
        programTime += instructionDuration
    }

    private fun pushGroup() {
        val groups = currentProgram.instructions
        if (groupPos >= groups.size)
            groupPos = 0

        instructionQueue.push(groups[groupPos++])
    }
}
