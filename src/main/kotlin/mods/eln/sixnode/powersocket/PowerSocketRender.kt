package mods.eln.sixnode.powersocket

import mods.eln.Eln
import mods.eln.cable.CableRenderDescriptor
import mods.eln.cable.CableRender
import mods.eln.misc.Coordinate
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.LRDUMask
import mods.eln.misc.UtilsClient.setGlColorFromDye
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.node.six.SixNodeElementInventory
import mods.eln.node.six.SixNodeElementRender
import mods.eln.node.six.SixNodeEntity
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.entity.player.Player
import org.lwjgl.opengl.GL11
import java.io.DataInputStream
import java.io.IOException

class PowerSocketRender(tileEntity: SixNodeEntity?, side: Direction?, descriptor: SixNodeDescriptor) :
    SixNodeElementRender(
        tileEntity!!, side!!, descriptor
    ) {
    var descriptor: PowerSocketDescriptor
    var coord: Coordinate
    var channel: String? = null
    var cableRender: CableRenderDescriptor? = null
    override var inventory = SixNodeElementInventory(1, 64, this)
    var paintColor = 15

    init {
        this.descriptor = descriptor as PowerSocketDescriptor
        coord = Coordinate(tileEntity!!)
    }

    override fun drawCables() {
        // Deprecated
    }

    override fun draw() {
        val poseStack = currentPoseStack ?: return
        val buffer = currentBuffer ?: return
        val light = currentLight
        val overlay = currentOverlay

        descriptor.draw(poseStack, buffer, light, overlay, paintColor)
        
        // Draw cables
        val rgb = mods.eln.misc.UtilsClient.getDyeColor(paintColor)
        val texture = Eln.instance!!.lowCurrentCableRender!!.cableTexture
        val consumer = buffer.getBuffer(net.minecraft.client.renderer.RenderType.entitySolid(texture))
        
        for (idx in 0..3) {
            val lrdu = LRDU.fromInt(idx)
            if (connectedSide.mask and (1 shl idx) != 0) {
                CableRender.drawCable(poseStack, consumer, light, overlay, Eln.instance!!.lowCurrentCableRender!!, LRDUMask(1 shl idx), CableRender.connectionType(this, side), Eln.instance!!.lowCurrentCableRender!!.widthDiv2 / 2.0f, false, rgb[0], rgb[1], rgb[2], 1f)
            }
        }
    }

    override fun refresh(deltaT: Float) {}
    override fun getCableRender(lrdu: LRDU): CableRenderDescriptor? {
        return Eln.instance!!.lowCurrentCableRender
    }

    override fun newGuiDraw(side: Direction, player: Player): Screen {
        return PowerSocketGui(this, player, inventory)
    }

    override fun publishUnserialize(stream: DataInputStream) {
        super.publishUnserialize(stream)
        try {
            channel = stream.readUTF()
            paintColor = stream.readInt()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
