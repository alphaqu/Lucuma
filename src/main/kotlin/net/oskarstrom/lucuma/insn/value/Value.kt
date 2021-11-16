package net.oskarstrom.lucuma.insn.value

import net.oskarstrom.lucuma.Fixture

interface Value {
    fun apply(channels: UByteArray, fixture: Fixture)
}