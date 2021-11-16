package net.oskarstrom.lucuma.insn.target

data class SingleTarget(val id: Int): Target {
    override fun testFixture(fixture: Int): Boolean = fixture == id

    override fun toString(): String {
        return id.toString()
    }
}