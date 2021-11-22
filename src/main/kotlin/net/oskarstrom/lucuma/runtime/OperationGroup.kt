package net.oskarstrom.lucuma.runtime

import net.oskarstrom.lucuma.runtime.operation.Operation

@ExperimentalUnsignedTypes
data class OperationGroup(val operations: MutableList<Operation>, val delay: Int) {
}