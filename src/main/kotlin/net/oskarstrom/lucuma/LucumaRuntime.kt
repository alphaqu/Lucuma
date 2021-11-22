package net.oskarstrom.lucuma

import net.oskarstrom.lucuma.io.DmxIO
import net.oskarstrom.lucuma.runtime.OperationGroup
import net.oskarstrom.lucuma.runtime.operation.Operation
import java.util.concurrent.atomic.AtomicBoolean

@ExperimentalUnsignedTypes
class LucumaRuntime(
    private val programs: Map<String, Program>,
    private val fixtures: List<Fixture>,
    private val io: DmxIO,
    channels: Int
) {
    //TODO move this to a singleton
    private val emptyGroup = OperationGroup(ArrayList(), 30)

    private var currentProgram = programs.getOrDefault("main", Program("main", arrayOf(emptyGroup)))
    private var programPos = 0

    private val operations = java.util.ArrayDeque<Operation>()

    // TODO this is shit
    private val activeOperations = java.util.ArrayDeque<Operation>()
    private var groupDuration = emptyGroup.delay
    private var groupStartTime = 0L

    private var channels = UByteArray(channels)

    fun launch() {
        val active = AtomicBoolean(true)
        Thread() { while (active.get()) tick() }.start()

        while (true) {
            val readLine = readLine()
            if (readLine != null) {
                if (readLine.startsWith("p:")) {
                    setCurrentProgram(readLine)
                } else if (readLine == "q") {
                    active.set(false)
                    break
                }
            }
        }
    }

    private fun tick() {
        val time = System.currentTimeMillis()

        while (!operations.isEmpty()) {
            val poll = operations.poll()
            poll.render(channels, 1.0)
            if (!poll.isDone()) {
                activeOperations.push(poll)
            } else {
                // TODO lag compensation
                poll.stop(channels)
            }
        }

        while (!activeOperations.isEmpty()) {
            operations.push(activeOperations.poll())
        }

        if (groupStartTime + groupDuration < time) {
            nextInstructionGroup(time)
        }

        io.send(channels, fixtures)
    }


    private fun setCurrentProgram(name: String) {
        currentProgram = programs[name] ?: run {
            println("Could not find program $name")
            Program("main", arrayOf(emptyGroup))
        }
        programPos = 0
    }

    private fun nextInstructionGroup(time: Long) {
        val instructions = currentProgram.instructions

        // out of bounds check
        if (instructions.size <= programPos) programPos = 0

        val group = instructions[programPos++]

        // add all operations from group last. To have most priority
        for (item in group.operations) {
            item.start(channels)
            operations.add(item)
        }

        groupDuration = group.delay
        groupStartTime = time
    }
}
