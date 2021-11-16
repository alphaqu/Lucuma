package net.oskarstrom.lucuma.insn

import net.oskarstrom.lucuma.Fixture
import net.oskarstrom.lucuma.insn.target.Target
import net.oskarstrom.lucuma.insn.value.Value

class AssignInstruction(private val value: Value, target: Target, fixtures: List<Fixture>) : Instruction {
    private val targetFixtures: List<Fixture>

    init {
        this.targetFixtures = findFixtures(target, fixtures)
    }

    override fun render(channels: UByteArray, speed: Double) {
        for (targetFixture in targetFixtures) {
            value.apply(channels, targetFixture)
        }
    }

    override fun getDuration(): Int = 0
}