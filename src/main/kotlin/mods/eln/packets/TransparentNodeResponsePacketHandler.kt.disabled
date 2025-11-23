package mods.eln.packets

import mods.eln.integration.waila.WailaCache

/**
 * Created by Gregory Maddra on 2016-06-27.
 */
class TransparentNodeResponsePacketHandler : IMessageHandler<TransparentNodeResponsePacket, IMessage> {
    override fun onMessage(message: TransparentNodeResponsePacket?, ctx: MessageContext?): IMessage? {
        val map = message!!.map
        val coord = message.coord
        WailaCache.nodes.put(coord, map)
        return null
    }
}
