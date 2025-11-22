package mods.eln.item

import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.misc.Obj3D

class FerromagneticCoreDescriptor(name: String, var model: Obj3D?, var cableMultiplicator: Double) : GenericItemUsingDamageDescriptor(name) {
    var feroPart: Obj3D.Obj3DPart? = null
}
