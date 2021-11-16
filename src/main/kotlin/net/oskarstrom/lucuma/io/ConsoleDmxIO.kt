package net.oskarstrom.lucuma.io

import net.oskarstrom.lucuma.Fixture

@ExperimentalUnsignedTypes
class ConsoleDmxIO : DmxIO {
    override fun send(channels: UByteArray, fixtures: List<Fixture>) {
        for (fixture in fixtures) {
            val c = fixture.channelStart
            val r = channels[c]
            val g = channels[c + 1]
            val b = channels[c + 2]
            val invertedColor = "\u001B[38;2;${r.inv()};${g.inv()};${b.inv()}m"
            print("\u001B[48;2;$r;$g;${b}m$invertedColor|        |\u001B[0m")
        }
        println()
    }
}