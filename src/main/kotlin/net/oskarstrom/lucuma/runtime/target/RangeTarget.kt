package net.oskarstrom.lucuma.runtime.target

import net.oskarstrom.lucuma.Fixture

class RangeTarget(
    val start: Int,
    val stop: Int,
) : Target {
    override fun testFixture(fixture: Fixture): Boolean = fixture.fixtureId in start..stop
}