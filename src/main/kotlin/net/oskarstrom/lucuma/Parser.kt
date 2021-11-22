package net.oskarstrom.lucuma

import net.oskarstrom.lucuma.error.LucumaParseException
import net.oskarstrom.lucuma.io.DmxIO
import net.oskarstrom.lucuma.runtime.OperationGroup
import net.oskarstrom.lucuma.runtime.operation.*
import net.oskarstrom.lucuma.runtime.target.*
import net.oskarstrom.lucuma.runtime.target.Target
import net.oskarstrom.lucuma.runtime.value.*
import kotlin.math.roundToInt

@ExperimentalUnsignedTypes
class Parser(code: String) {
    private val reader = CodeReader(code)
    private val fixtures = mutableListOf<Fixture>()
    private val programs = mutableMapOf<String, Program>()
    private var channels = 0
    private var bpm = -1


    fun parse(io: DmxIO): LucumaRuntime {
        while (true) {
            if (reader.hasMore()) break
            when (val read = reader.read()) {
                "option" -> parseOption(reader)
                "fixture" -> parseFixture(reader)
                "program" -> parseProgram(reader)
                else -> reader.exception("Could not parse $read")
            }
        }

        println(programs)
        return LucumaRuntime(programs, fixtures, io, channels)
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

        // TODO check order
        val currentFixtures = fixtures.size + 1

        when (target) {
            is RangeTarget -> {
                for (i in target.start..target.stop) {
                    this.channels += channels
                    fixtures.add(Fixture(i, channels, this.channels - channels, HashMap(variables)))
                }
            }
            is NumberTarget -> {
                this.channels += channels
                fixtures.add(Fixture(target.target, channels, this.channels - channels, HashMap(variables)))
            }
            is BooleanTarget -> reader.exception("Fixtures unknown.")
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
            if (target.testFixture(fixture)) {
                val oldValue = fixture.variables.put(property, value)
                if (oldValue != null) reader.exception("Property $property on fixture ${fixture.fixtureId} is already set to $oldValue")
            }
        }
    }

    private fun parseTarget(reader: CodeReader): Target {
        val read = reader.read()
        return if (read == "*") BuiltinTargets.ALL
        else if (read == "!") BuiltinTargets.NONE
        else if (read.contains("..")) {
            val split = read.split("..")
            RangeTarget(parseMs(reader, split[0]), parseMs(reader, split[1]))
        } else {
            val value = read.toIntOrNull()
            if (value == null) NameTarget(read)
            else NumberTarget(value)
        }
    }

    private fun parseProgram(reader: CodeReader) {
        val programName = reader.read()
        println("=========================== PROGRAM $programName ===========================")

        val instructions = parseGroups(reader.subReader("{", "}"), programName)

        programs[programName] = Program(programName, instructions.toTypedArray())
    }

    private fun parseGroups(
        reader: CodeReader,
        programName: String
    ): MutableList<OperationGroup> {
        var currentTime = 0
        val groups = mutableListOf<OperationGroup>()
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
                    groups.add(OperationGroup(ArrayList(operations), delay))
                    operations.clear()
                }
                "t" -> {
                    val targetTime = parseDelayValue(reader)
                    if (targetTime < currentTime)
                        reader.exception("Target time ${targetTime}ms is expected when track is already ${currentTime}ms in")

                    val delay = targetTime - currentTime
                    currentTime += delay
                    groups.add(OperationGroup(ArrayList(operations), delay))
                    operations.clear()
                }
                "repeat" -> {
                    groups.add(OperationGroup(ArrayList(operations), 0))
                    operations.clear()

                    reader.read() // peek
                    val repeatAmount = parseNumber(reader, reader.read())
                    val parseInstructions = parseGroups(reader.subReader("{", "}"), programName)
                    for (i in 0 until repeatAmount) {
                        for (parseInstruction in parseInstructions)
                            currentTime += parseInstruction.delay

                        groups.addAll(parseInstructions)
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
            if (groups.isEmpty()) throw LucumaParseException("No wait instruction found in $programName")

            groups[0].operations.addAll(operations)
        }
        return groups
    }

    private fun parseOperation(reader: CodeReader): Operation {
        val target = parseTarget(reader)
        reader.ensure("=")
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
                if (values.size >= 3) {
                    MultiFadeOperation(fixtures, target, parseFadeTime, values)
                } else {
                    FadeOperation(fixtures, target, parseFadeTime, values[0], values[1])
                }
            }
            "in" -> {
                reader.read() // peek
                TransitionOperation(fixtures, target, parseMs(reader, reader.read()), value)
            }
            else -> SetOperation(fixtures, target, value)
        }
    }

    private fun parseValue(reader: CodeReader): Value {
        val read = reader.read()
        return if (read == "[") {
            val values = ArrayList<Value>()
            do {
                values.add(parseValue(reader))
            } while (reader.read() == ",")
            SwitchingValue(values)
        } else if (read == "#") {
            HexValue(reader.read().chunked(2).map { it.toUByte(16) }.toUByteArray())
        } else if (reader.peek() == ":") {
            reader.read()
            ChannelValue(parseNumber(reader, read) - 1, parseValue(reader))
        } else {
            NumberValue(parseUByte(reader, read))
        }
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
            ((parseNumber(reader, string.split("b")[1]) * beatDuration * 1000) / 4).roundToInt()
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