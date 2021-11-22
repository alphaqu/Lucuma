package net.oskarstrom.lucuma.io

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_BLOCKING
import com.fazecast.jSerialComm.SerialPort.TIMEOUT_WRITE_BLOCKING
import net.oskarstrom.lucuma.Fixture

@ExperimentalUnsignedTypes
class ArduinoDmxIO(channels: Int, ups: Int) : DmxIO {
    private val serialPort: SerialPort
    private val timeWait = 1000 / ups

    init {
        val comPort = SerialPort.getCommPorts()[0]
        comPort.baudRate = 9600
        comPort.setComPortTimeouts(TIMEOUT_READ_BLOCKING, 1000, 1000)
        comPort.setComPortTimeouts(TIMEOUT_WRITE_BLOCKING, 1000, 1000)
        comPort.openPort(200)
        serialPort = comPort
        val code = ByteArray(1)
        println("Arduino startup")
        while (true) {
            if (serialPort.bytesAvailable() >= 1) {
                serialPort.readBytes(code, 1)
                if (code[0] == (69).toByte()) {
                    println("Arduino initialized")
                    break
                }
            }
        }
    }

    fun send(bytes: ByteArray) {
        serialPort.writeBytes(bytes, bytes.size.toLong())
    }

    override fun close() {
        serialPort.closePort()
    }

    private val sender = ByteArray(channels * 2)
    private val oldChannels = UByteArray(channels)

    override fun send(channels: UByteArray, fixtures: List<Fixture>) {
        var currentPos = 0
        for ((index, value) in channels.withIndex()) {
            if (oldChannels[index] != value) {
                sender[currentPos++] = (index + 1).toByte()
                sender[currentPos++] = value.toByte()
                oldChannels[index] = value
            }
        }
        serialPort.writeBytes(sender, currentPos.toLong())
        Thread.sleep(timeWait.toLong())
        // println(sender.contentToString() + " / $timeWait")
    }
}