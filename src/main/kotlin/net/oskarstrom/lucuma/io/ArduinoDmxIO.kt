package net.oskarstrom.lucuma.io

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_BLOCKING
import com.fazecast.jSerialComm.SerialPort.TIMEOUT_WRITE_BLOCKING
import net.oskarstrom.lucuma.Fixture

@ExperimentalUnsignedTypes
class ArduinoDmxIO: DmxIO {
    private val serialPort: SerialPort

    init {
        val comPort = SerialPort.getCommPorts()[0]
        comPort.baudRate = 9600
        comPort.setComPortTimeouts(TIMEOUT_READ_BLOCKING, 1000, 1000)
        comPort.setComPortTimeouts(TIMEOUT_WRITE_BLOCKING, 1000, 1000)
        comPort.openPort(200)
        serialPort = comPort
        println("com port")
    }

    fun send(bytes: ByteArray) {
        serialPort.writeBytes(bytes,bytes.size.toLong())
    }

    fun close() {
        serialPort.closePort()
    }

    override fun send(channels: UByteArray, fixtures: List<Fixture>) {
        serialPort.writeBytes(channels.toByteArray(), channels.size.toLong())
    }
}