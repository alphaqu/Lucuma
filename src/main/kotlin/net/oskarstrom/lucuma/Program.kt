package net.oskarstrom.lucuma

class Program(val name: String, val groups: Array<Group>) {
    override fun toString(): String {
        return "Program(name='$name', instructions=${groups.contentToString()})"
    }
}