package net.oskarstrom.lucuma.insn

import net.oskarstrom.lucuma.Fixture
import net.oskarstrom.lucuma.insn.target.Target

interface Instruction {
    fun start(oldChannels: UByteArray) {}
    fun render(channels: UByteArray, speed: Double) {}
    fun stop() {}

    fun getDuration(): Int

    fun findFixtures(target: Target, fixtures: List<Fixture>): List<Fixture> {
        val fixturesOut = ArrayList<Fixture>()
        for (fixture in fixtures) {
            if (target.testFixture(fixture.id)) {
                fixturesOut.add(fixture)
            }
        }
        return fixturesOut
    }
}