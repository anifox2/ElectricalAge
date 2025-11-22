package mods.eln.node

import mods.eln.misc.Direction
import net.minecraft.client.gui.Screen
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import java.io.DataInputStream

interface INodeEntity {
    val nodeUuid: String
    fun serverPublishUnserialize(stream: DataInputStream)
    fun serverPacketUnserialize(stream: DataInputStream)

    @SideOnly(Side.CLIENT)
    fun newGuiDraw(side: Direction, player: Player): Screen?
    fun newContainer(side: Direction, player: Player): Container?
}
