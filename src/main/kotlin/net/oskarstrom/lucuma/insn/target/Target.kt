package net.oskarstrom.lucuma.insn.target

@FunctionalInterface
interface Target {
    fun testFixture(fixture: Int): Boolean

}