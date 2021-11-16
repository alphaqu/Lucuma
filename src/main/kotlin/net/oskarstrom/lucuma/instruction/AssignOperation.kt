package net.oskarstrom.lucuma.instruction

import net.oskarstrom.lucuma.Fixture
import net.oskarstrom.lucuma.instruction.selector.Selector
import net.oskarstrom.lucuma.instruction.value.Value

@ExperimentalUnsignedTypes
class AssignOperation(private val value: Value, selector: Selector, fixtures: List<Fixture>) : Operation {
    private val targetFixtures: List<Fixture> = findFixtures(selector, fixtures)

    override fun render(channels: UByteArray, speed: Double) {
        for (targetFixture in targetFixtures) {
            value.apply(channels, targetFixture)
        }
    }

}