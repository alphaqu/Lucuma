package net.oskarstrom.lucuma.instruction.value

import net.oskarstrom.lucuma.Fixture

@ExperimentalUnsignedTypes
interface Value {
    fun apply(channels: UByteArray, fixture: Fixture)
}