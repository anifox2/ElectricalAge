package mods.eln.sixnode

import mods.eln.cable.CableRender
import mods.eln.cable.CableRenderDescriptor
import mods.eln.i18n.I18N.tr
import mods.eln.misc.*
import mods.eln.node.NodeBase
import mods.eln.node.six.*
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import org.lwjgl.opengl.GL11

class ConduitCableDescriptor(
    name: String,
    val render: CableRenderDescriptor
): SixNodeDescriptor(name, ConduitCableElement::class.java, ConduitCableRender::class.java) {

    // ...existing code...
    override fun appendHoverText(itemStack: net.minecraft.world.item.ItemStack, level: net.minecraft.world.level.Level?, list: MutableList<net.minecraft.network.chat.Component>, flag: net.minecraft.world.item.TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)
        list.add(net.minecraft.network.chat.Component.literal(tr("A conduit to run your cables through")))
    }

    /*
    override fun addRealismContext(list: MutableList<String>): RealisticEnum {
        list.add(tr("Has some caveats:"))
        list.add(tr("  * Thermal Sim is disabled in the conduit"))
        return RealisticEnum.REALISTIC
    }
    */

    fun getNodeMask(): Int {
// ...existing code...
        return NodeBase.maskConduit
    }

    fun bindCableTexture() {
        render.bindCableTexture()
    }
}


class ConduitCableElement(
    sixNode: SixNode?,
    side: Direction?,
    descriptor: SixNodeDescriptor
): SixNodeElement(
    sixNode!!, side!!, descriptor) {

    val descriptor = descriptor as ConduitCableDescriptor

    override fun getElectricalLoad(lrdu: LRDU, mask: Int) = null

    override fun getThermalLoad(lrdu: LRDU, mask: Int) = null

    override fun getConnectionMask(lrdu: LRDU) = descriptor.getNodeMask()

    override fun getWaila(): Map<String, String> {
        val info: MutableMap<String, String> = HashMap()
        info[tr("Contained Cables")] = "0"
        return info
    }

    override fun multiMeterString() = ""

    override fun thermoMeterString() = ""

}

class ConduitCableRender(
    tileEntity: SixNodeEntity?,
    side: Direction?,
    descriptor: SixNodeDescriptor
) : SixNodeElementRender(tileEntity!!, side!!, descriptor) {

    val descriptor = descriptor as ConduitCableDescriptor

    override fun getCableRender(lrdu: LRDU): CableRenderDescriptor {
        return descriptor.render
    }

    override fun glListEnable() = false

    override fun draw() {
        val poseStack = currentPoseStack ?: return
        val buffer = currentBuffer ?: return
        val light = currentLight
        val overlay = currentOverlay

        Minecraft.getInstance().profiler.push("ECable")
        
        val texture = descriptor.render.cableTexture
        val consumer = buffer.getBuffer(net.minecraft.client.renderer.RenderType.entitySolid(texture))

        CableRender.drawCable(poseStack, consumer, light, overlay, descriptor.render, connectedSide, CableRender.connectionType(this, side), descriptor.render.widthDiv2 / 2.0f, false)
        CableRender.drawNode(poseStack, consumer, light, overlay, descriptor.render, connectedSide, CableRender.connectionType(this, side))

        Minecraft.getInstance().profiler.pop()
    }
// ...existing code...

    override fun drawCableAuto() = false
}