package mods.eln.sixnode.genericcable

import mods.eln.cable.CableRenderDescriptor
import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.node.NodeBase
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.Resistor
import net.minecraft.world.item.ItemStack

abstract class GenericCableDescriptor(
    name: String?,
    ElementClass: Class<*>,
    RenderClass: Class<*>
) : SixNodeDescriptor(name, ElementClass, RenderClass) {

    var electricalNominalVoltage: Double = 0.0
    var electricalMaximalVoltage: Double = 0.0
    var electricalMaximalCurrent: Double = 0.0
    var electricalNominalPower: Double = 0.0
    var electricalRs: Double = 0.0

    // TODO: remove later
    var thermalRp: Double = 0.0
    var thermalC: Double = 0.0
    var thermalRs: Double = 0.0
    var thermalWarmLimit: Double = 0.0
    var thermalCoolLimit: Double = 0.0

    var render: CableRenderDescriptor? = null

    open fun getNodeMask(): Int {
        return NodeBase.maskElectricalPower
    }

    abstract fun applyTo(electricalLoad: ElectricalLoad, rsFactor: Double)
    abstract fun applyTo(electricalLoad: ElectricalLoad)
    abstract fun applyTo(resistor: Resistor)
    abstract fun applyTo(resistor: Resistor, factor: Double)
    abstract fun applyTo(thermalLoad: ThermalLoad)

    companion object {
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
