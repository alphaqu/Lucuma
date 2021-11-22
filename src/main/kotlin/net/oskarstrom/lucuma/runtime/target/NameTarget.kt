package net.oskarstrom.lucuma.runtime.target

import net.oskarstrom.lucuma.Fixture

class NameTarget(private val name: String) : Target {
    override fun testFixture(fixture: Fixture): Boolean = fixture.variables[name] == name
}