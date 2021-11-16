package net.oskarstrom.lucuma.insn.value

import net.oskarstrom.lucuma.Fixture

data class ChannelValue(val channel: Int, val value: UByte) : Value {
    override fun apply(channels: UByteArray, fixture: Fixture) {
        channels[fixture.channelStart + channel] = value
    }

    override fun toString(): String {
        return "${channel}|${value}"
    }
}