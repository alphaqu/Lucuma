package net.oskarstrom.lucuma

import net.oskarstrom.lucuma.error.LucumaParseException
import net.oskarstrom.lucuma.instruction.*
import net.oskarstrom.lucuma.instruction.selector.*
import net.oskarstrom.lucuma.instruction.value.ChannelValue
import net.oskarstrom.lucuma.instruction.value.HexValue
import net.oskarstrom.lucuma.instruction.value.Value
import net.oskarstrom.lucuma.io.ConsoleDmxIO
import net.oskarstrom.lucuma.io.DmxIO
import kotlin.math.roundToInt

@ExperimentalUnsignedTypes
class Parser(code: String) {
    private val reader = CodeReader(code)
    private val fixtures = mutableListOf<Fixture>()
    private val programs = mutableMapOf<String, Program>()
    private var channels = 0
    private var bpm = -1


    fun parse(io: DmxIO): ProgramExecutor {
        while (true) {
            if (reader.hasMore()) break
            when (val read = reader.read()) {
                "option" -> parseOption(reader)
                "fixture" -> parseFixture(reader)
                "program" -> parseProgram(reader)
                else -> reader.exception("Could not parse $read")
            }
        }
        return ProgramExecutor(programs, fixtures, io, channels)
    }

    private fun parseFixture(reader: CodeReader) {
        val target = parseTarget(reader)
        println("=========================== FIXTURE $target ===========================")
        reader.ensure("set")

        val variables = mutableMapOf<String, String>()
        while (true) {
            val variableName = reader.read()
            reader.ensure("=")
            variables[variableName] = reader.read()
            if (reader.peek() == ",") {
                reader.read()
                continue
            } else break
        }

        val channels = parseMs(
            reader,
            variables["channels"] ?: reader.exception("Could not find channels declared").toString()
        );

        val currentFixtures = fixtures.size + 1
        when (target) {
            is RangeSelector -> {
                if (!target.testFixture(currentFixtures)) reader.exception(
                    "Cannot declare Fixtures out of order ${target.low} > current:${currentFixtures} < ${target.high}",
                    true
                )

                for (i in target.low..target.high) {
                    this.channels += channels
                    fixtures.add(Fixture(i, channels, this.channels - channels, HashMap(variables)))
                }
            }
            is SingleSelector -> {
                if (!target.testFixture(currentFixtures)) reader.exception(
                    "Cannot declare Fixtures out of order ${target.id} != $currentFixtures",
                    true
                )

                this.channels += channels
                fixtures.add(Fixture(target.id, channels, this.channels - channels, HashMap(variables)))
            }
            is GlobalSelector -> reader.exception("Fixture target is global")
        }
    }

    private fun parseOption(reader: CodeReader) {
        val target = parseTarget(reader)
        println("=========================== OPTION $target ===========================")
        reader.ensure("set")
        val property = reader.read()
        reader.ensure("=")

        val value = reader.read()
        for (fixture in fixtures) {
            if (target.testFixture(fixture.fixtureId)) {
                val oldValue = fixture.variables.put(property, value)
                if (oldValue != null) reader.exception("Property $property on fixture ${fixture.fixtureId} is already set to $oldValue")
            }
        }
    }

    private fun parseTarget(reader: CodeReader): Selector {
        val read = reader.read()
        return if (read == "*") GlobalSelector
        else if (read.contains("..")) {
            val split = read.split("..")
            RangeSelector(parseMs(reader, split[0]), parseMs(reader, split[1]))
        } else {
            val value = read.toIntOrNull()
            if (value == null) IdSelector(read, fixtures)
            else SingleSelector(value)
        }
    }

    private fun parseProgram(reader: CodeReader) {
        val programName = reader.read()
        println("=========================== PROGRAM $programName ===========================")

        val instructions = parseInstructions(reader.subReader("{", "}"), programName)

        programs[programName] = Program(programName, instructions.toTypedArray())
    }

    private fun parseInstructions(
        reader: CodeReader,
        programName: String
    ): MutableList<Instruction> {
        var currentTime = 0
        val instructions = mutableListOf<Instruction>()
        val operations = mutableListOf<Operation>()

        fun parseDelayValue(subReader: CodeReader): Int {
            subReader.read() // peek
            subReader.ensure(":")
            return parseMs(subReader, subReader.read())
        }

        while (!reader.hasMore()) {
            when (reader.peek()) {
                "w" -> {
                    val delay = parseDelayValue(reader)
                    currentTime += delay
                    instructions.add(Instruction(ArrayList(operations), delay))
                    operations.clear()
                }
                "t" -> {
                    val targetTime = parseDelayValue(reader)
                    if (targetTime < currentTime)
                        reader.exception("Target time ${targetTime}ms is expected when track is already ${currentTime}ms in")

                    val delay = targetTime - currentTime
                    currentTime += delay
                    instructions.add(Instruction(ArrayList(operations), delay))
                    operations.clear()
                }
                "repeat" -> {
                    instructions.add(Instruction(ArrayList(operations), 0))
                    operations.clear()

                    reader.read() // peek
                    val repeatAmount = parseNumber(reader, reader.read())
                    val parseInstructions = parseInstructions(reader.subReader("{", "}"), programName)
                    for (i in 0 until repeatAmount) {
                        for (parseInstruction in parseInstructions)
                            currentTime += parseInstruction.delay

                        instructions.addAll(parseInstructions)
                    }
                }
                "bpm" -> {
                    reader.read() // peek
                    reader.ensure("=")
                    bpm = parseMs(reader, reader.read())
                    println("set bpm to $bpm")
                }
                else -> {
                    operations.add(parseOperation(reader))
                }
            }
        }

        if (operations.isNotEmpty()) {
            if (instructions.isEmpty()) throw LucumaParseException("No wait instruction found in $programName")

            instructions[0].operations.addAll(operations)
        }
        return instructions
    }

    private fun parseOperation(reader: CodeReader): Operation {
        val target = parseTarget(reader)
        if (reader.read() != "=") reader.exception("Could not parse instruction target. = missing")
        val value = parseValue(reader)

        return when (reader.peek()) {
            ">" -> {
                val values = mutableListOf<Value>()
                values.add(value)
                while (reader.peek() == ">") {
                    reader.read() // peek
                    values.add(parseValue(reader))
                }

                val parseFadeTime = parseFadeTime(reader)
                FadeOperation(values, parseFadeTime, target, fixtures, channels)
            }
            "in" -> {
                reader.read() // peek
                TransitionOperation(target, value, parseMs(reader, reader.read()), channels, fixtures)
            }
            else -> AssignOperation(value, target, fixtures)
        }
    }

    private fun parseValue(reader: CodeReader): Value {
        val read = reader.read()
        if (read.startsWith("#")) {
            return HexValue(read.substring(1).chunked(2).map { it.toUByte(16) }.toUByteArray())
        } else if (reader.peek().contains(":")) {
            reader.read() // peek
            return ChannelValue(parseMs(reader, read), parseUByte(reader, reader.read()))
        } else {
            reader.exception("Could not parse value")
        }
        throw LucumaParseException("end")
    }


    private fun parseFadeTime(reader: CodeReader): Int {
        val read = reader.read()
        if (read != "in") reader.exception("Could not find \"in\" syntax in a fade instruction")
        return parseMs(reader, reader.read())
    }

    private fun parseMs(reader: CodeReader, string: String): Int {
        return if (string.startsWith("b")) {
            if (bpm == -1) reader.exception("Beats per minute has not been set in the program.")
            val beatDuration = 60 / bpm.toDouble()
            val roundToInt = ((parseNumber(reader, string.split("b")[1]) * beatDuration * 1000) / 4).roundToInt()
            println(roundToInt)
            roundToInt
        } else parseNumber(reader, string)
    }

    private fun parseNumber(reader: CodeReader, string: String): Int {
        val ms = string.toIntOrNull()
        if (ms == null) reader.exception("Could not parse number")
        // safe to assume that npe won't happen as it gets thrown above
        return ms!!
    }

    private fun parseUByte(reader: CodeReader, string: String): UByte {
        val ms = string.toUByteOrNull()
        if (ms == null) reader.exception("Could not parse number")
        // safe to assume that npe won't happen as it gets thrown above
        return ms!!
    }
}

@ExperimentalUnsignedTypes
fun main() {
    val io = ConsoleDmxIO()
    try {
        val parser = Parser(Parser::class.java.getResource("/net.oskarstrom.lucuma/test.luc")!!.readText())

        val executor = parser.parse(io)

        println("Hit enter to start")
        val readLine = readLine()
        executor.launch()
    } finally {
        //io.send(byteArrayOf(0, 0, 0))
    }
}