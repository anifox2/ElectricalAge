package mods.eln.sixnode.genericcable

import mods.eln.cable.CableRenderDescriptor
import mods.eln.cable.CableRenderType
import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.node.NodeBase
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.Resistor
import net.minecraft.world.item.ItemStack
import mods.eln.cable.CableRender
import mods.eln.misc.LRDU
import mods.eln.misc.LRDUMask
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer

abstract class GenericCableDescriptor(
    name: String?,
    ElementClass: Class<*>,
    RenderClass: Class<*>
) : SixNodeDescriptor(name, ElementClass, RenderClass) {

    @JvmField
    var electricalNominalVoltage: Double = 0.0
    @JvmField
    var electricalMaximalVoltage: Double = 0.0
    @JvmField
    var electricalMaximalCurrent: Double = 0.0
    @JvmField
    var electricalNominalPower: Double = 0.0
    @JvmField
    var electricalRs: Double = 0.0

    // TODO: remove later
    @JvmField
    var thermalRp: Double = 0.0
    @JvmField
    var thermalC: Double = 0.0
    @JvmField
    var thermalRs: Double = 0.0
    @JvmField
    var thermalWarmLimit: Double = 0.0
    @JvmField
    var thermalCoolLimit: Double = 0.0

    @JvmField
    var render: CableRenderDescriptor? = null

    override fun draw(poseStack: PoseStack, consumer: VertexConsumer, packedLight: Int, packedOverlay: Int, signal: Boolean) {
        if (render != null) {
            val connection = LRDUMask()
            connection.set(LRDU.Left, true)
            connection.set(LRDU.Right, true)
            val renderType = CableRenderType()
            CableRender.drawCable(poseStack, consumer, packedLight, packedOverlay, render, connection, renderType)
        }
    }

    open fun getNodeMask(): Int {
        return NodeBase.maskElectricalPower
    }

    abstract fun applyTo(electricalLoad: ElectricalLoad, rsFactor: Double)
    abstract fun applyTo(electricalLoad: ElectricalLoad)
    abstract fun applyTo(resistor: Resistor)
    abstract fun applyTo(resistor: Resistor, factor: Double)
    abstract fun applyTo(thermalLoad: ThermalLoad)

    companion object {
        @JvmStatic
        fun getCableRender(cable: ItemStack?): CableRenderDescriptor? {
            if (cable == null) return null
            val desc = GenericItemUsingDamageDescriptor.getDescriptor(cable)
            return if (desc is GenericCableDescriptor) {
                desc.render
            } else {
                null
            }
        }
    }
}
