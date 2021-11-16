package net.oskarstrom.lucuma.instruction.selector

@ExperimentalUnsignedTypes
object GlobalSelector : Selector {
    override fun testFixture(fixture: Int): Boolean = true

    override fun toString(): String {
        return "*"
    }
}