package net.oskarstrom.lucuma

import net.oskarstrom.lucuma.io.ArduinoDmxIO
import java.util.*

@OptIn(ExperimentalUnsignedTypes::class)
fun main() {
    val io = ArduinoDmxIO(7 + 4, 100)
    try {
        val parser = Parser(Parser::class.java.getResource("/net.oskarstrom.lucuma/test.luc")!!.readText())

        val executor = parser.parse(io)

        println("Hit enter to start")
        val scanner = Scanner(System.`in`)
        scanner.next()
        println("Started")
        executor.launch()
    } finally {

        println("closing")
    }
}