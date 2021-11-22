package net.oskarstrom.lucuma.runtime.value

class NumberValue(
    private val number: UByte
) : Value {
    override fun apply(oldValue: UByte, channel: Int): UByte = number
    override fun changesChannel(channel: Int): Boolean = true
}