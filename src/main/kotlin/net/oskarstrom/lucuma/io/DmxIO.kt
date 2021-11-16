package net.oskarstrom.lucuma.io

import net.oskarstrom.lucuma.Fixture

interface DmxIO {
    fun send(channels: UByteArray, fixtures: List<Fixture>)
}