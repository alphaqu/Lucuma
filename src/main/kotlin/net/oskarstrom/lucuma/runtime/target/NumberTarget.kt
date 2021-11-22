package net.oskarstrom.lucuma.runtime.target

import net.oskarstrom.lucuma.Fixture

class NumberTarget(val target: Int) : Target {
    override fun testFixture(fixture: Fixture): Boolean = fixture.fixtureId == target
}