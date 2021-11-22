package net.oskarstrom.lucuma.runtime.value

class ChannelValue(
    private val channel: Int,
    private val value: Value
) : Value {
    override fun apply(oldValue: UByte, channel: Int): UByte {
        return value.apply(oldValue, channel)
    }

    override fun changesChannel(channel: Int): Boolean {
        return channel == this.channel && value.changesChannel(channel)
    }
}