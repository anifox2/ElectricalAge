package mods.eln.sixnode.thermalcable

import mods.eln.cable.CableRender
import mods.eln.cable.CableRenderDescriptor
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.UtilsClient
import mods.eln.node.NodeBase
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.node.six.SixNodeElementRender
import mods.eln.node.six.SixNodeEntity
import net.minecraft.client.Minecraft
import java.io.DataInputStream
import java.io.IOException

class ThermalCableRender(tileEntity: SixNodeEntity, side: Direction, descriptor: SixNodeDescriptor) :
    SixNodeElementRender(tileEntity, side, descriptor) {

    var cableDesciptor: ThermalCableDescriptor = descriptor as ThermalCableDescriptor
    var temperature = 0.0
    var color = 0

    override fun drawCableAuto(): Boolean {
        return false
    }

    override fun draw() {
        val poseStack = currentPoseStack ?: return
        val buffer = currentBuffer ?: return
        val light = currentLight
        val overlay = currentOverlay

        Minecraft.getInstance().profiler.push("TCable")

        val rgb = UtilsClient.getDyeColor(color)
        val texture = cableDesciptor.render!!.cableTexture
        val consumer = buffer.getBuffer(net.minecraft.client.renderer.RenderType.entitySolid(texture))

        CableRender.drawCable(poseStack, consumer, light, overlay, cableDesciptor.render!!, connectedSide, CableRender.connectionType(this, side), cableDesciptor.render!!.widthDiv2 / 2.0f, false, rgb[0], rgb[1], rgb[2], 1f)
        CableRender.drawNode(poseStack, consumer, light, overlay, cableDesciptor.render!!, connectedSide, CableRender.connectionType(this, side), rgb[0], rgb[1], rgb[2], 1f)

        Minecraft.getInstance().profiler.pop()
    }

    override fun glListEnable(): Boolean {
        return false
    }

    override fun publishUnserialize(stream: DataInputStream) {
        super.publishUnserialize(stream)
        try {
            val b = stream.readByte().toInt()
            color = (b shr 4) and 0xF
            temperature = stream.readShort() / NodeBase.networkSerializeTFactor
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun getCableRender(lrdu: LRDU): CableRenderDescriptor? {
        return cableDesciptor.render
    }

    override fun getCableDry(lrdu: LRDU?): Int {
        return color
    }
}
