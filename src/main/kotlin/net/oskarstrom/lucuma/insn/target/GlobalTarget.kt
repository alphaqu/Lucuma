package net.oskarstrom.lucuma.insn.target

object GlobalTarget: Target {
    override fun testFixture(fixture: Int): Boolean = true

    override fun toString(): String {
        return "*"
    }
}