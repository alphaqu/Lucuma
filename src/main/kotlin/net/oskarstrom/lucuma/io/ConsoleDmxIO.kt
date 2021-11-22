package net.oskarstrom.lucuma.io

import net.oskarstrom.lucuma.Fixture

@ExperimentalUnsignedTypes
class ConsoleDmxIO : DmxIO {
    override fun send(channels: UByteArray, fixtures: List<Fixture>) {
        for (fixture in fixtures) {
            val c = fixture.channelStart
            val a = channels[c + 6]
            val r = channels[c] * (a / 255u)
            val g = channels[c + 1] * (a / 255u)
            val b = channels[c + 2] * (a / 255u)
            val invertedColor = "\u001B[38;2;${r.inv()};${g.inv()};${b.inv()}m"
            print("\u001B[48;2;$r;$g;${b}m$invertedColor|   $a     |\u001B[0m")
        }
        println()

        Thread.sleep(100)
    }

    override fun close() {
    }
}