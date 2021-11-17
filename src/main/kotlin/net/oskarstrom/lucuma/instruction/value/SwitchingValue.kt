package net.oskarstrom.lucuma.instruction.value

import net.oskarstrom.lucuma.Fixture

@ExperimentalUnsignedTypes
class SwitchingValue(private val values: List<Value>, private var currentValue: Int) : Value {
    override fun apply(channels: UByteArray, fixture: Fixture) {
        if (currentValue >= values.size) currentValue = 0
        values[currentValue++].apply(channels, fixture)
    }
}