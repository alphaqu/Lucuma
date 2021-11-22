package net.oskarstrom.lucuma.runtime

import net.oskarstrom.lucuma.Fixture
import net.oskarstrom.lucuma.runtime.target.Target
import net.oskarstrom.lucuma.runtime.value.Value

object RuntimeUtils {

    fun getTargetFixtures(fixtures: List<Fixture>, target: Target): Array<Fixture> {
        val fixturesOut = ArrayList<Fixture>()
        for (fixture in fixtures) {
            if (target.testFixture(fixture)) {
                fixturesOut.add(fixture)
            }
        }
        return fixturesOut.toTypedArray()
    }

    fun iterChannels(fixture: Fixture): IntRange = fixture.channelStart until fixture.channelStart + fixture.channels

    fun getChannels(fixtures: Array<Fixture>, target: Target, value: Value): Array<ChannelData> {
        val channels = ArrayList<ChannelData>()
        for (fixture in fixtures) {
            if (target.testFixture(fixture)) {
                val channelStart = fixture.channelStart
                for (i in 0 until fixture.channels) {
                    if (value.changesChannel(i)) {
                        channels.add(ChannelData(channels.size, i, channelStart + i))
                    }
                }
            }
        }

        return channels.toTypedArray()
    }

    data class ChannelData(val index: Int, val localChannel: Int, val rawChannel: Int)
}