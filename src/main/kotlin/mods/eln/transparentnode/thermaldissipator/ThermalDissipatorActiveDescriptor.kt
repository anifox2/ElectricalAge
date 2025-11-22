package mods.eln.transparentnode.thermaldissipator

import mods.eln.Eln
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import mods.eln.misc.Obj3D
import mods.eln.misc.VoltageLevelColor
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ThermalLoad
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.item.Item

class ThermalDissipatorActiveDescriptor(
    name: String?,
    var obj: Obj3D?,
    var nominalElectricalU: Double,
    var electricalNominalP: Double,
    var nominalElectricalCoolingPower: Double,
    var cableDescriptor: ElectricalCableDescriptor,
    var warmLimit: Double,
    var coolLimit: Double,
    var nominalP: Double,
    var nominalT: Double,
    nominalTao: Double,
    nominalConnectionDrop: Double
) : TransparentNodeDescriptor(
    name,
    ThermalDissipatorActiveElement::class.java,
    ThermalDissipatorActiveRender::class.java
) {
    var electricalRp: Double = 0.0
    var electricalToThermalRp: Double = 0.0
    var thermalC: Double = 0.0
    var thermalRp: Double = 0.0
    var thermalRs: Double = 0.0
    var main: Obj3D.Obj3DPart? = null
    var rot: Obj3D.Obj3DPart? = null

    init {
        electricalRp = nominalElectricalU * nominalElectricalU / electricalNominalP
        electricalToThermalRp = nominalT / nominalElectricalCoolingPower
        thermalC = (nominalP + nominalElectricalCoolingPower) * nominalTao / nominalT
        thermalRp = nominalT / nominalP
        thermalRs = nominalConnectionDrop / (nominalP + nominalElectricalCoolingPower)
        Eln.simulator.checkThermalLoad(thermalRs, thermalRp, thermalC)
        
        if (obj != null) {
            main = obj!!.getPart("main")
            rot = obj!!.getPart("rot")
        }
        voltageLevelColor = VoltageLevelColor.Thermal
    }

    override fun setParent(item: Item, damage: Int) {
        super.setParent(item, damage)
        Data.addEnergy(newItemStack())
    }

    fun applyTo(load: ThermalLoad) {
        load.set(thermalRs, thermalRp, thermalC)
    }

    fun applyTo(load: ElectricalLoad) {
        load.setRs(electricalRp)
    }

    fun draw(speed: Float) {
        main?.draw()
        rot?.draw(speed, 1f, 0f, 0f)
    }
}
