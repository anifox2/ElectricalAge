package mods.eln.misc

import mods.eln.generic.GenericItemUsingDamageDescriptor

class FerromagneticCoreDescriptor(name: String) : GenericItemUsingDamageDescriptor(name) {
    var cableMultiplicator: Double = 1.0
    var feroPart: Obj3D.Obj3DPart? = null
}
