package net.oskarstrom.lucuma.runtime.value

import kotlin.random.Random
import kotlin.random.nextInt


object RandomValue : Value {
    private val random = Random(System.currentTimeMillis())
    override fun apply(oldValue: UByte, channel: Int): UByte = random.nextInt(0 until 255).toUByte()
    override fun changesChannel(channel: Int): Boolean = true
}