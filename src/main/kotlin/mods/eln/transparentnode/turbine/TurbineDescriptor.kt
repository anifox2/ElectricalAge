package mods.eln.transparentnode.turbine

import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.cable.CableRenderDescriptor
import mods.eln.misc.Obj3D
import mods.eln.misc.FunctionTable
import mods.eln.Eln

class TurbineDescriptor(
    name: String,
    val objName: String,
    val cableRender: CableRenderDescriptor?,
    val TtoU: FunctionTable,
    val PoutToPin: FunctionTable,
    val nominalDeltaT: Double,
    val nominalU: Double,
    val nominalP: Double,
    val val1: Double,
    val electricalRs: Double,
    val val2: Double,
    val val3: Double,
    val val4: Double,
    val soundName: String,
    override var obj: Obj3D? = null
) : TransparentNodeDescriptor(
    name,
    TurbineElement::class.java,
    TurbineRender::class.java
) {
    init {
        if (obj == null && objName.isNotEmpty()) {
            obj = Eln.obj.getObj(objName)
        }
    }
}
