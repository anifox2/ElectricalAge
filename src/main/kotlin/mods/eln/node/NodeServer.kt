package mods.eln.node

import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.server.ServerLifecycleHooks

class NodeServer {
    fun init() {
        //	NodeBlockEntity.nodeAddedList.clear();
    }

    fun stop() {
        //	NodeBlockEntity.nodeAddedList.clear();
    }

    var counter = 0
    @SubscribeEvent
    fun tick(event: TickEvent.ServerTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        val server = ServerLifecycleHooks.getCurrentServer()
        if (server != null) {
            for (node in NodeManager.instance?.nodeList ?: emptyList()) {
                if (node.needPublish) {
                    node.publishToAllPlayer()
                }
            }
            for (obj in server.playerList.players) {
                val player = obj as ServerPlayer?
                var openContainerNode: NodeBase? = null
                var container: INodeContainer? = null
                if (player!!.containerMenu != null && player.containerMenu is INodeContainer) {
                    container = player.containerMenu as INodeContainer
                    openContainerNode = container.node
                }
                for (node in NodeManager.instance?.nodeList ?: emptyList()) {
                    if (node === openContainerNode) {
                        if (counter % (1 + container!!.refreshRateDivider) == 0) node.publishToPlayer(player)
                    }
                }
            }
            counter++
        }
    }

    init {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this)
    }
}
