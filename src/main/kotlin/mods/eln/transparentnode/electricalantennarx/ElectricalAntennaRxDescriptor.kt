package mods.eln.transparentnode.electricalantennarx

import mods.eln.misc.Obj3D
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor

class ElectricalAntennaRxDescriptor(
    name: String,
    obj: Obj3D,
    var nominalVoltage: Double,
    var nominalPower: Double,
    var maxVoltage: Double,
    var maxPower: Double,
    var cableDescriptor: ElectricalCableDescriptor?
) : TransparentNodeDescriptor(
    name,
    ElectricalAntennaRxElement::class.java,
    ElectricalAntennaRxRender::class.java
) {
    var mainPart: Obj3D.Obj3DPart? = null
    init {
        this.mainPart = obj.getPart("main")
        this.obj = obj
    }
}
