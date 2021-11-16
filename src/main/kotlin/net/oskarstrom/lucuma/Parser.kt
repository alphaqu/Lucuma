package net.oskarstrom.lucuma

import net.oskarstrom.lucuma.error.LucumaParseException
import net.oskarstrom.lucuma.instruction.*
import net.oskarstrom.lucuma.instruction.selector.GlobalSelector
import net.oskarstrom.lucuma.instruction.selector.RangeSelector
import net.oskarstrom.lucuma.instruction.selector.Selector
import net.oskarstrom.lucuma.instruction.selector.SingleSelector
import net.oskarstrom.lucuma.instruction.value.ChannelValue
import net.oskarstrom.lucuma.instruction.value.HexValue
import net.oskarstrom.lucuma.instruction.value.Value
import net.oskarstrom.lucuma.io.ConsoleDmxIO
import net.oskarstrom.lucuma.io.DmxIO

@ExperimentalUnsignedTypes
class Parser(code: String) {
    private val reader = CodeReader(code)
    private val fixtures = mutableListOf<Fixture>()
    private val programs = mutableMapOf<String, Program>()
    private var channels = 0


    fun parse(io: DmxIO): ProgramExecutor {
        while (true) {
            if (reader.hasMore()) break
            when (val read = reader.read()) {
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
        if (reader.read() != "set") reader.exception("Fixture setter set keyword is missing")

        val variables = mutableMapOf<String, Int>()
        while (true) {
            val variableName = reader.read()
            if (reader.read() != "=") reader.exception("Fixture variable setter is missing =")
            variables[variableName] = parseNumber(reader, reader.read())
            if (reader.peek() == ",") {
                reader.read()
                continue
            } else break
        }

        val channels = variables["channels"] ?: reader.exception("Could not find channels declared").hashCode();

        val currentFixtures = fixtures.size + 1
        when (target) {
            is RangeSelector -> {
                if (!target.testFixture(currentFixtures)) reader.exception(
                    "Cannot declare Fixtures out of order ${target.low} > current:${currentFixtures} < ${target.high}",
                    true
                )

                for (i in target.low..target.high) {
                    this.channels += channels
                    fixtures.add(Fixture(i, channels, this.channels - channels, variables))
                }
            }
            is SingleSelector -> {
                if (!target.testFixture(currentFixtures)) reader.exception(
                    "Cannot declare Fixtures out of order ${target.id} != $currentFixtures",
                    true
                )

                this.channels += channels
                fixtures.add(Fixture(target.id, channels, this.channels - channels, variables))
            }
            is GlobalSelector -> reader.exception("Fixture target is global")
        }
    }

    private fun parseTarget(reader: CodeReader): Selector {
        val read = reader.read()
        return if (read == "*") GlobalSelector
        else if (read.contains("..")) {
            val split = read.split("..")
            RangeSelector(parseNumber(reader, split[0]), parseNumber(reader, split[1]))
        } else SingleSelector(parseNumber(reader, read))
    }

    private fun parseProgram(reader: CodeReader) {
        val programName = reader.read()
        println("=========================== PROGRAM $programName ===========================")
        val subReader = reader.subReader("{", "}")

        val instructions = mutableListOf<Instruction>()
        val operations = mutableListOf<Operation>()
        while (!subReader.hasMore()) {
            val read = subReader.peek()
            println(read)
            if (read.startsWith("w")) {
                subReader.read() // peek
                instructions.add(Instruction(ArrayList(operations), parseNumber(subReader, read.split("w")[1])))
                operations.clear()
            } else {
                operations.add(parseInstruction(subReader))
            }
        }

        if (operations.isNotEmpty()) {
            if (instructions.isEmpty()) throw LucumaParseException("No wait instruction found in $programName")

            instructions[0].operations.addAll(operations)
        }

        programs[programName] = Program(programName, instructions.toTypedArray())
    }

    private fun parseInstruction(reader: CodeReader): Operation {
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
                TransitionOperation(target, value, parseNumber(reader, reader.read()), channels, fixtures)
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
            return ChannelValue(parseNumber(reader, read), parseUByte(reader, reader.read()))
        } else {
            reader.exception("Could not parse value")
        }
        throw LucumaParseException("end")
    }


    private fun parseFadeTime(reader: CodeReader): Int {
        val read = reader.read()
        if (read != "in") reader.exception("Could not find \"in\" syntax in a fade instruction")
        return parseNumber(reader, reader.read())
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

fun main() {
    val io = ConsoleDmxIO()
    try {
        val parser = Parser(Parser::class.java.getResource("/net.oskarstrom.lucuma/test.luc").readText())

        val executor = parser.parse(io)
        executor.launch()
    } finally {
        //io.send(byteArrayOf(0, 0, 0))
    }
}