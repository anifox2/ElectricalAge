package mods.eln.transparentnode.electricalfurnace

import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNodeRender
import mods.eln.misc.Obj3D
import mods.eln.misc.FunctionTable

class ElectricalFurnaceDescriptor(
    name: String,
    val PfT: FunctionTable,
    val thermalPlostfT: FunctionTable,
    val thermalMass: Double,
    override var obj: Obj3D? = null
) : TransparentNodeDescriptor(
    name,
    ElectricalFurnaceElement::class.java,
    ElectricalFurnaceRender::class.java
) {
}
