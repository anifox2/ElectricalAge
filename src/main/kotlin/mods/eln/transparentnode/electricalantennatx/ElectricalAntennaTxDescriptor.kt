package mods.eln.transparentnode.electricalantennatx

import mods.eln.misc.Obj3D
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor

class ElectricalAntennaTxDescriptor(
    name: String,
    obj: Obj3D,
    var range: Int,
    var efficiency: Double,
    var bandwidth: Double,
    var nominalVoltage: Double,
    var nominalPower: Double,
    var maxVoltage: Double,
    var maxPower: Double,
    var cableDescriptor: ElectricalCableDescriptor?
) : TransparentNodeDescriptor(
    name,
    ElectricalAntennaTxElement::class.java,
    ElectricalAntennaTxRender::class.java
) {
    var mainPart: Obj3D.Obj3DPart? = null
    init {
        this.mainPart = obj.getPart("main")
        this.obj = obj
    }
}
