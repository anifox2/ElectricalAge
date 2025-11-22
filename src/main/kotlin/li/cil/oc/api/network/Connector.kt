package li.cil.oc.api.network

interface Connector : Node {
    fun globalBufferSize(): Double
    fun globalBuffer(): Double
    fun tryChangeBuffer(amount: Double): Boolean
}
