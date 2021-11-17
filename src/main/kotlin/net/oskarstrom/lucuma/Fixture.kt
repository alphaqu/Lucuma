package net.oskarstrom.lucuma

data class Fixture(
    val fixtureId: Int,
    val channels: Int,
    val channelStart: Int,
    val variables: MutableMap<String, String>,
)