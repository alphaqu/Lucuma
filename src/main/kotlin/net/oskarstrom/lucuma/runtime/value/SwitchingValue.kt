package net.oskarstrom.lucuma.runtime.value

class SwitchingValue(private val values: List<Value>) : Value {
    private var currentPos = 0

    override fun apply(oldValue: UByte, channel: Int): UByte =
        values[currentPos++ % values.size].apply(oldValue, channel)

    // TODO check if all values are the same type
    override fun changesChannel(channel: Int): Boolean = values[0].changesChannel(channel)
}