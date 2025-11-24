package mods.eln.sixnode.electricalsource

import mods.eln.Eln
import mods.eln.cable.CableRenderDescriptor
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.node.six.SixNodeElementRender
import mods.eln.node.six.SixNodeEntity
import mods.eln.gui.ScreenEln
import net.minecraft.world.entity.player.Player
import java.io.DataInputStream
import java.io.IOException

class ElectricalSourceRender(
    blockEntity: SixNodeEntity,
    side: Direction,
    descriptor: SixNodeDescriptor
) : SixNodeElementRender(blockEntity, side, descriptor) {

    var descriptor: ElectricalSourceDescriptor = descriptor as ElectricalSourceDescriptor

    var voltage: Double = 0.0
    var current: Double = 0.0
    var color: Int = 0

    override fun draw() {
        super.draw()

        val poseStack = currentPoseStack ?: return
        val buffer = currentBuffer ?: return
        val light = currentLight
        val overlay = currentOverlay

        front!!.rotatePoseOnX(poseStack)

        val consumer = buffer.getBuffer(net.minecraft.client.renderer.RenderType.solid())

        descriptor.draw(poseStack, consumer, light, overlay, voltage >= (Eln.SVU / 2))
    }

    override fun publishUnserialize(stream: DataInputStream) {
        super.publishUnserialize(stream)
        try {
            voltage = stream.readFloat().toDouble()
            needRedrawCable()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun newGuiDraw(side: Direction, player: Player): ScreenEln? {
        return ElectricalSourceGui(this)
    }

    override fun getCableRender(lrdu: LRDU): CableRenderDescriptor? {
        if (descriptor.signalSource)         return Eln.instance!!.signalCableDescriptor!!.render
        if (voltage < Eln.instance!!.lowVoltageCableDescriptor!!.electricalMaximalVoltage)
            return Eln.instance!!.lowVoltageCableDescriptor!!.render
        if (voltage < Eln.instance!!.meduimVoltageCableDescriptor!!.electricalMaximalVoltage)
            return Eln.instance!!.meduimVoltageCableDescriptor!!.render
        if (voltage < Eln.instance!!.highVoltageCableDescriptor!!.electricalMaximalVoltage)
            return Eln.instance!!.highVoltageCableDescriptor!!.render
        return Eln.instance!!.veryHighVoltageCableDescriptor!!.render
    }
}
