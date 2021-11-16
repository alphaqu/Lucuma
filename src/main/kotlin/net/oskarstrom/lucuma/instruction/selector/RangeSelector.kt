package net.oskarstrom.lucuma.instruction.selector

@ExperimentalUnsignedTypes
data class RangeSelector(val low: Int, val high: Int) : Selector {
    override fun testFixture(fixture: Int): Boolean = fixture in low..high

    override fun toString(): String {
        return "$low..$high"
    }
}