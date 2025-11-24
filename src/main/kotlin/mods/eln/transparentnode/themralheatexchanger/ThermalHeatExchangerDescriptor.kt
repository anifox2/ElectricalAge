package mods.eln.transparentnode.themralheatexchanger

import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNodeRender
import mods.eln.misc.Obj3D
import mods.eln.sim.ThermalLoadInitializer

class ThermalHeatExchangerDescriptor(
    name: String,
    val thermalLoad: ThermalLoadInitializer,
    override var obj: Obj3D? = null
) : TransparentNodeDescriptor(
    name,
    ThermalHeatExchangerElement::class.java,
    ThermalHeatExchangerRender::class.java
) {
}
