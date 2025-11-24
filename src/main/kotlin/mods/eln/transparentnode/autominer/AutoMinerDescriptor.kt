package mods.eln.transparentnode.autominer

import mods.eln.misc.Coordinate
import mods.eln.misc.Obj3D
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.sim.ThermalLoadInitializer
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor

class AutoMinerDescriptor(
    name: String,
    obj: Obj3D,
    var powerLoad: Array<Coordinate>,
    var lightCoord: Coordinate,
    var miningCoord: Coordinate,
    var w: Int,
    var h: Int,
    var l: Int,
    var cableDescriptor: ElectricalCableDescriptor?,
    var speed: Double,
    var energyPerBlock: Double
) : TransparentNodeDescriptor(
    name,
    AutoMinerElement::class.java,
    AutoMinerRender::class.java
) {
    var mainPart: Obj3D.Obj3DPart? = null
    init {
        this.mainPart = obj.getPart("main")
        this.obj = obj
    }
}
