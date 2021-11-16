package net.oskarstrom.lucuma.instruction.selector

@ExperimentalUnsignedTypes
data class SingleSelector(val id: Int) : Selector {
    override fun testFixture(fixture: Int): Boolean = fixture == id

    override fun toString(): String {
        return id.toString()
    }
}