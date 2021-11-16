package net.oskarstrom.lucuma.insn.target

fun interface Target {
    fun testFixture(fixture: Int): Boolean
}