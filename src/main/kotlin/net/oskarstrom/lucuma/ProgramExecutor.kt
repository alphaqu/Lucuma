package net.oskarstrom.lucuma

import net.oskarstrom.lucuma.io.DmxIO
import java.util.*

class ProgramExecutor(
    private val programs: Map<String, Program>,
    private val fixtures: List<Fixture>,
    private val io: DmxIO,
    channels: Int
) {
    private val emptyGroup = Group(ArrayList(), 30)
    private var currentProgram = programs.getOrDefault("main", Program("main", arrayOf(emptyGroup)))
    private var groupPos = 0
    private var groupQueue = ArrayDeque<Group>()
    private var currentGroup: Group = emptyGroup

    private var paddingSize = 3
    private var programTime = System.currentTimeMillis()

    private var channels = UByteArray(channels)
    private var readChannels = UByteArray(channels)

    fun launch() {
        Thread() { while (true) tickReader() }.start()
        Thread() { while (true) tickDrawer() }.start()

        while (true) {
            readLine()?.let { setCurrentProgram(it) }
        }
    }

    private fun setCurrentProgram(name: String) {
        currentProgram = programs[name] ?: run {
            println("Could not find program $name")
            Program("main", arrayOf(emptyGroup))
        }
        groupPos = 0
    }

    private fun tickDrawer() {
        Thread.sleep(50)

        currentGroup.render(channels, 1.0)
        io.send(channels, fixtures)
    }


    private fun tickReader() {
        if (groupQueue.size < paddingSize) pushGroup()

        val group = groupQueue.poll()

        // align time
        val realTime = System.currentTimeMillis()
        val instructionDuration = group.delay

        var speedMultiply = 1.0
        // if (realTime > programTime) {
        //     val timeAhead = realTime - programTime
        //     Thread.sleep(timeAhead)
        // } else if (programTime > realTime) {
        //     val timeBehind = realTime - programTime

        //     if (timeBehind > instructionDuration) {
        //         // skip instruction
        //         programTime += instructionDuration
        //         return
        //     } else speedMultiply = instructionDuration / timeBehind.toDouble()
        // }

        group.start(readChannels)
        currentGroup = group
        Thread.sleep(group.delay.toLong())
        currentGroup.render(readChannels, 1.0)
        group.end()

        programTime += instructionDuration
    }

    private fun pushGroup() {
        val groups = currentProgram.groups
        if (groupPos >= groups.size)
            groupPos = 0

        groupQueue.push(groups[groupPos++])
    }
}
