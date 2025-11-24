package mods.eln.transparentnode.thermaldissipator

import mods.eln.Eln
import mods.eln.misc.Obj3D
import mods.eln.misc.VoltageLevelColor
import mods.eln.sim.ThermalLoad
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.item.Item

class ThermalDissipatorPassiveDescriptor(
    name: String?,
    override var obj: Obj3D?,
    var warmLimit: Double,
    var coolLimit: Double,
    var nominalP: Double,
    var nominalT: Double,
    nominalTao: Double,
    nominalConnectionDrop: Double
) : TransparentNodeDescriptor(
    name,
    ThermalDissipatorPassiveElement::class.java,
    ThermalDissipatorPassiveRender::class.java
) {
    var thermalRs: Double = 0.0
    var thermalRp: Double = 0.0
    var thermalC: Double = 0.0
    var main: Obj3D.Obj3DPart? = null

    init {
        thermalC = nominalP * nominalTao / nominalT
        thermalRp = nominalT / nominalP
        thermalRs = nominalConnectionDrop / nominalP
        Eln.simulator.checkThermalLoad(thermalRs, thermalRp, thermalC)
        if (obj != null) main = obj!!.getPart("main")
        voltageLevelColor = VoltageLevelColor.Thermal
    }

    override fun setParent(item: Item, damage: Int) {
        super.setParent(item, damage)
        Data.addEnergy(newItemStack())
    }

    fun applyTo(load: ThermalLoad) {
        load.set(thermalRs, thermalRp, thermalC)
    }

    fun draw() {
        main?.draw()
    }
}
