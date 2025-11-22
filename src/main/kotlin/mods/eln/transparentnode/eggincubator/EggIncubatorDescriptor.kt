package mods.eln.transparentnode.eggincubator

import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNodeRender
import mods.eln.misc.Obj3D
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor

class EggIncubatorDescriptor(
    name: String,
    obj: Obj3D,
    val cableDescriptor: ElectricalCableDescriptor,
    val nominalVoltage: Double,
    val nominalPower: Double
) : TransparentNodeDescriptor(
    name,
    EggIncubatorElement::class.java,
    EggIncubatorRender::class.java
) {
}
