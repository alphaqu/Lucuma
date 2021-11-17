package net.oskarstrom.lucuma.instruction.selector

import net.oskarstrom.lucuma.Fixture

@ExperimentalUnsignedTypes
class IdSelector(
    private val id: String,
    private val fixtures: List<Fixture>
) : Selector {

    override fun testFixture(fixture: Int): Boolean {
        val get = fixtures[fixture - 1].variables["id"]
        return if (get == null) false
        else get == id
    }

}