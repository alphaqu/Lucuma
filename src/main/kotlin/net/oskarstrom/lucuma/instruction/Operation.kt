package net.oskarstrom.lucuma.instruction

import net.oskarstrom.lucuma.Fixture
import net.oskarstrom.lucuma.instruction.selector.Selector

@ExperimentalUnsignedTypes
interface Operation {
    fun start(oldChannels: UByteArray) {}
    fun render(channels: UByteArray, speed: Double) {}
    fun stop() {}

    fun findFixtures(selector: Selector, fixtures: List<Fixture>): List<Fixture> {
        val fixturesOut = ArrayList<Fixture>()
        for (fixture in fixtures) {
            if (selector.testFixture(fixture.fixtureId)) {
                fixturesOut.add(fixture)
            }
        }
        return fixturesOut
    }
}