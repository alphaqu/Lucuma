package net.oskarstrom.lucuma.insn.value

import net.oskarstrom.lucuma.Fixture

data class HexValue(val values: UByteArray) : Value {

    override fun apply(channels: UByteArray, fixture: Fixture) {
        for (i in values.indices) {
            channels[fixture.channelStart + i] = values[i]
        }
    }

    override fun toString(): String {
        return "#" + values.joinToString("", "", "") { it.toString(16) }
    }
}