package mods.eln.transparentnode.waterturbine

import mods.eln.Eln
import mods.eln.misc.Coordinate
import mods.eln.misc.Obj3D
import mods.eln.misc.VoltageLevelColor
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.item.Item

class WaterTurbineDescriptor(
    name: String?,
    var obj: Obj3D?,
    var cable: ElectricalCableDescriptor,
    var nominalPower: Double,
    var maxVoltage: Double,
    var waterCoord: Coordinate,
    var soundName: String,
    var nominalVolume: Float
) : TransparentNodeDescriptor(
    name,
    WaterTurbineElement::class.java,
    WaterTurbineRender::class.java
) {
    var wheel: Obj3D.Obj3DPart? = null
    var support: Obj3D.Obj3DPart? = null
    var generator: Obj3D.Obj3DPart? = null
    var speed: Float = 0f

    init {
        if (obj != null) {
            wheel = obj!!.getPart("Wheel")
            support = obj!!.getPart("Support")
            generator = obj!!.getPart("Generator")
            speed = 60f
        }
        voltageLevelColor = VoltageLevelColor.LowVoltage
        Data.addEnergy(getStack())
    }

    fun draw(alpha: Float) {
        support?.draw()
        generator?.draw()
        wheel?.draw(alpha, 1f, 0f, 0f)
    }
}

