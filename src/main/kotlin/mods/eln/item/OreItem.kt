package mods.eln.item

import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.ore.OreDescriptor

class OreItem(name: String) : GenericItemUsingDamageDescriptor(name) {
    val descriptors = ArrayList<OreDescriptor>()
    fun addDescriptor(id: Int, desc: OreDescriptor) {
        descriptors.add(desc)
    }
}
