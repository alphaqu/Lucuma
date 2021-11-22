package net.oskarstrom.lucuma.runtime.value

@ExperimentalUnsignedTypes
object BuiltinValues {
    val BLACK = HexValue(ubyteArrayOf(0u, 0u, 0u))
    val SILVER = HexValue(ubyteArrayOf(192u, 192u, 192u))
    val GRAY = HexValue(ubyteArrayOf(128u, 128u, 128u))
    val WHITE = HexValue(ubyteArrayOf(255u, 255u, 255u))
    val MAROON = HexValue(ubyteArrayOf(128u, 0u, 0u))
    val RED = HexValue(ubyteArrayOf(255u, 0u, 0u))
    val PURPLE = HexValue(ubyteArrayOf(128u, 0u, 128u))
    val MAGENTA = HexValue(ubyteArrayOf(255u, 0u, 255u))
    val GREEN = HexValue(ubyteArrayOf(0u, 128u, 0u))
    val LIME = HexValue(ubyteArrayOf(0u, 255u, 0u))
    val OLIVE = HexValue(ubyteArrayOf(128u, 128u, 0u))
    val YELLOW = HexValue(ubyteArrayOf(255u, 255u, 0u))
    val NAVY = HexValue(ubyteArrayOf(0u, 0u, 128u))
    val BLUE = HexValue(ubyteArrayOf(0u, 0u, 255u))
    val TEAL = HexValue(ubyteArrayOf(0u, 128u, 128u))
    val AQUA = HexValue(ubyteArrayOf(0u, 255u, 255u))

    val ON = NumberValue(255u)
    val OFF = NumberValue(0u)

    val RANDOM = RandomValue
}