package net.oskarstrom.lucuma.runtime.target

import net.oskarstrom.lucuma.Fixture

class BooleanTarget(private val value: Boolean) : Target {
    override fun testFixture(fixture: Fixture): Boolean = value
}