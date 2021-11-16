package net.oskarstrom.lucuma

import net.oskarstrom.lucuma.error.LucumaParseException

class CodeReader {
    private val strings: Array<String>
    private var pos = 0
    private val end: Int
    private val start: Int

    private constructor(strings: Array<String>, end: Int, start: Int) {
        this.strings = strings
        this.end = end
        this.start = start
    }

    constructor(code: String) {
        val strings = mutableListOf<String>()

        //FIXME(leocth): what the heck?
        var builder: StringBuilder? = StringBuilder()
        for (c in (code.replace("(/\\*([^*]|[\r\n]|(\\*+([^*/]|[\r\n])))*\\*+/)|(//.*)".toRegex(), "")).toCharArray()) {
            when (split(c)) {
                SplitType.NONE -> {
                    if (builder == null) builder = StringBuilder()
                    builder.append(c);
                }
                SplitType.INCLUSIVE -> {
                    if (builder != null) strings.add(builder.toString())
                    builder = null;
                }
                SplitType.EXCLUSIVE -> {
                    if (builder != null) strings.add(builder.toString())
                    strings.add(c.toString())
                    builder = null;
                }
            }
        }


        this.strings = strings.filter { it.isNotEmpty() }.toTypedArray()
        this.start = 0
        this.end = this.strings.size
    }

    private fun split(ch: Char) = when (ch) {
        '\t', '\r', ' ' -> SplitType.EXCLUSIVE
        '{', '}', '\"', ',', '>', '=', '*', ':', '\n' -> SplitType.INCLUSIVE
        else -> SplitType.NONE
    }

    fun subReader(openingCharacter: String, closingCharacter: String): CodeReader {
        val read = read()
        if (read != openingCharacter) throw LucumaParseException("Character $read is not $openingCharacter")

        var characters = 1
        val start = getPos()
        for (i in start until this.end) {
            val chunk = read()
            if (chunk == openingCharacter) characters++
            else if (chunk == closingCharacter) characters--
            if (characters == 0) break
        }

        if (characters != 0) throw LucumaParseException("Could not find end of brackets at ${strings[end]}")

        return CodeReader(strings, getPos() - 1, start)
    }

    fun read(): String {
        if (end()) exception("${peekNextIndex()} is outside of bounds $end")
        else {
            while (true) {
                val s = strings[getAndIncrementPos()]
                if (s == "\n") continue
                return s
            }
        }
        throw LucumaParseException("end")

    }


    fun peek(): String {
        if (end()) exception("${peekNextIndex()} is outside of bounds $end")
        else {
            var currentPos = getPos()
            while (true) {
                val s = strings[currentPos++]
                if (s == "\n") continue
                return s
            }
        }
        throw LucumaParseException("end")
    }

    private fun peekNextIndex(): Int {
        var currentPos = getPos()
        if (currentPos >= end) return end
        if (currentPos >= strings.size) return strings.size
        while (true) {
            val s = strings[currentPos++]
            if (s == "\n") continue
            return currentPos - 1
        }
    }

    fun hasMore() = peekNextIndex() >= end
    fun end() = peekNextIndex() > end

    fun exception(error: String, line: Boolean = false) {
        val stringBuilder = StringBuilder()
        var start = 0
        for (i in (getPos() - 1) downTo 0) {
            if (strings[i] == "\n") {
                start = i + 1
                break
            }
        }

        for (i in start..strings.size) {
            val s = strings[i]
            stringBuilder.append(s).append(' ')
            if (s == "\n") break
        }

        for (i in start..strings.size) {
            val s = strings[i]
            if (i == getPos() - 1 || line)
                stringBuilder.append("^".repeat(s.length)).append(if (line) '^' else ' ')
            else stringBuilder.append(" ".repeat(s.length)).append(' ')
            if (s == "\n") break
        }

        throw LucumaParseException(" $error \n $stringBuilder")
    }

    private fun getPos() = start + pos

    private fun getAndIncrementPos() = start + pos++

    override fun toString(): String {
        val builder = StringBuilder()
        for (i in start until end) {
            builder.append("${this.strings[i]}|")
        }
        return builder.toString()
    }
}

enum class SplitType {
    NONE,
    INCLUSIVE,
    EXCLUSIVE
}