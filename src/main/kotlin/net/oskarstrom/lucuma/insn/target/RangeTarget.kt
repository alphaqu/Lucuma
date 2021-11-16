package net.oskarstrom.lucuma.insn.target

data class RangeTarget(val low: Int, val high: Int) : Target {
    override fun testFixture(fixture: Int): Boolean = fixture in low..high

    override fun toString(): String {
        return "${low}..$high"
    }
}