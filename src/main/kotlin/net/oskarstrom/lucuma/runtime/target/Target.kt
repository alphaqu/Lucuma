package net.oskarstrom.lucuma.runtime.target

import net.oskarstrom.lucuma.Fixture

interface Target {
    fun testFixture(fixture: Fixture): Boolean
}