package mods.eln.transparentnode.heatfurnace

import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNodeRender
import mods.eln.misc.Obj3D
import mods.eln.sim.ThermalLoadInitializer

class HeatFurnaceDescriptor(
    name: String,
    val objName: String,
    val val1: Double,
    val val2: Double,
    val val3: Int,
    val val4: Double,
    val thermalLoad: ThermalLoadInitializer,
    val obj: Obj3D? = null
) : TransparentNodeDescriptor(
    name,
    HeatFurnaceElement::class.java,
    HeatFurnaceRender::class.java
) {
}
