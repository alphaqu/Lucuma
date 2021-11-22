package net.oskarstrom.lucuma.runtime.value

interface Value {
    fun apply(oldValue: UByte, channel: Int): UByte
    fun changesChannel(channel: Int): Boolean
}