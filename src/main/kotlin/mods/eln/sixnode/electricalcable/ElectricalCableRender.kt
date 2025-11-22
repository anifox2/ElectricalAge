package mods.eln.sixnode.electricalcable

import mods.eln.cable.CableRender
import mods.eln.cable.CableRenderDescriptor
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.UtilsClient
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.node.six.SixNodeElementRender
import mods.eln.node.six.SixNodeEntity
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL11
import java.io.DataInputStream
import java.io.IOException

class ElectricalCableRender(tileEntity: SixNodeEntity, side: Direction, descriptor: SixNodeDescriptor) :
    SixNodeElementRender(tileEntity, side, descriptor) {

    var descriptor: ElectricalCableDescriptor
    var color = 0

    init {
        this.descriptor = descriptor as ElectricalCableDescriptor
    }

    override fun drawCableAuto(): Boolean {
        return false
    }

    override fun draw() {
        Minecraft.getInstance().profiler.push("ECable")

        UtilsClient.setGlColorFromDye(color, 1.0f)

        UtilsClient.bindTexture(descriptor.render!!.cableTexture)
        glListCall()

        GL11.glColor3f(1f, 1f, 1f)
        Minecraft.getInstance().profiler.pop()
    }

    override fun glListDraw() {
        CableRender.drawCable(descriptor.render!!, connectedSide, CableRender.connectionType(this, side), descriptor.render!!.widthDiv2 / 2.0f, false)
        CableRender.drawNode(descriptor.render!!, connectedSide, CableRender.connectionType(this, side))
    }

    override fun glListEnable(): Boolean {
        return true
    }

    override fun publishUnserialize(stream: DataInputStream) {
        super.publishUnserialize(stream)
        try {
            val b = stream.readByte()
            color = (b.toInt() shr 4) and 0xF
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun getCableRender(lrdu: LRDU): CableRenderDescriptor? {
        return descriptor.render
    }

    override fun getCableDry(lrdu: LRDU?): Int {
        return color
    }
}
