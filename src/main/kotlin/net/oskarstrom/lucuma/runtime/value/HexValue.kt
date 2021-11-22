package net.oskarstrom.lucuma.runtime.value

@ExperimentalUnsignedTypes
class HexValue(
    private val hexValues: UByteArray
) : Value {
    override fun apply(oldValue: UByte, channel: Int): UByte = hexValues[channel]
    override fun changesChannel(channel: Int): Boolean = channel < hexValues.size;
}