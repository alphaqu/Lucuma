package net.oskarstrom.lucuma.instruction.selector

@ExperimentalUnsignedTypes
fun interface Selector {
    fun testFixture(fixture: Int): Boolean
}