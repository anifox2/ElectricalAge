package li.cil.oc.api.network

interface Environment {
    fun node(): Node?
    fun onConnect(node: Node)
    fun onDisconnect(node: Node)
    fun onMessage(message: Message)
}
