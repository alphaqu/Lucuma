package net.oskarstrom.lucuma.io

import net.oskarstrom.lucuma.Fixture

@ExperimentalUnsignedTypes
interface DmxIO {
    fun send(channels: UByteArray, fixtures: List<Fixture>)
}